package com.example.rotp_boyiiman.rps;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.example.rotp_boyiiman.AddonMain;
import com.example.rotp_boyiiman.config.BoyIIManConfig;
import com.example.rotp_boyiiman.net.B2MNetwork;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.action.stand.StandEntityBlock;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Данные о краже. Хранятся в NBT самой сущности (getPersistentData), поэтому сохраняются вместе с миром
 * и работают на сервере/мультиплеере.
 *
 * Матчи с одним и тем же человеком можно играть сколько угодно раз:
 *  - выиграл вор   -> у жертвы отнимается ЕЩЁ ОДНА способность ПКМ (до {@link #MAX_STOLEN} штук);
 *  - выиграла цель -> вору приходится вернуть ОДНУ из украденных способностей.
 *
 * Ослабление жертвы и усиление вора пропорциональны числу украденных способностей (каждая = 1/3 силы стенда,
 * максимум {@link #MAX_THIRDS} трети). Во время матча каждый выигранный вором раунд ещё и временно
 * ослабляет цель на треть (до конца матча).
 *
 * Таймеров нет: кража длится, пока жив владелец украденного стенда (жертва).
 * Умерла жертва - всё отпускается. Умер вор - жертве всё возвращается.
 * Вор держит кражу только у одного человека: победа над другим игроком возвращает прошлой жертве всё.
 */
public final class StolenEnergy {
    private static final String TAG = "rotp_boyiiman";
    // жертва
    private static final String V_ACTIONS = "VictimActions", V_THIEF = "VictimThief", V_TEMP = "VictimTemp";
    // вор
    private static final String H_ACTIONS = "HeldActions", H_VICTIM = "HeldVictim";

    /** Сколько способностей вор может держать одновременно (= число слотов украденных способностей). */
    public static final int MAX_STOLEN = 3;
    public static final int MAX_THIRDS = 3;

    /** Блокировка отнятой способности у жертвы: кулдаун, который обновляется каждые 5 тиков. */
    private static final int LOCK_COOLDOWN_TICKS = 60;
    private static final int VALIDATE_PERIOD_TICKS = 100;

    private static final Random RANDOM = new Random();

    private static final UUID VICTIM_MOD = UUID.fromString("5c1d0a52-7a1e-4f0e-9a52-0b2c7d1f0001");
    private static final UUID HELD_MOD = UUID.fromString("5c1d0a52-7a1e-4f0e-9a52-0b2c7d1f0002");

    /** Только клиент: id способностей, украденных локальным игроком, через ';' (приходит пакетом StolenSyncPacket). */
    public static volatile String clientStolenActions = "";

    private StolenEnergy() {}

    private static CompoundNBT data(LivingEntity e) {
        CompoundNBT root = e.getPersistentData();
        if (!root.contains(TAG, 10)) root.put(TAG, new CompoundNBT());
        return root.getCompound(TAG);
    }

    private static CompoundNBT dataOrNull(LivingEntity e) {
        CompoundNBT root = e.getPersistentData();
        return root.contains(TAG, 10) ? root.getCompound(TAG) : null;
    }

    private static void dropIfEmpty(LivingEntity e) {
        CompoundNBT root = e.getPersistentData();
        if (root.contains(TAG, 10) && root.getCompound(TAG).isEmpty()) root.remove(TAG);
    }

    // ------------------------- списки id в NBT -------------------------
    private static List<String> ids(CompoundNBT d, String key) {
        List<String> out = new ArrayList<>();
        if (d == null) return out;
        ListNBT l = d.getList(key, 8);
        for (int i = 0; i < l.size(); i++) out.add(l.getString(i));
        return out;
    }

    private static void putIds(CompoundNBT d, String key, List<String> ids) {
        if (ids.isEmpty()) {
            d.remove(key);
            return;
        }
        ListNBT l = new ListNBT();
        for (String s : ids) l.add(StringNBT.valueOf(s));
        d.put(key, l);
    }

    private static String idOf(StandAction a) {
        return a != null && a.getRegistryName() != null ? a.getRegistryName().toString() : "";
    }

    // ------------------------- жертва -------------------------
    /** Способности, отнятые у этой жертвы. */
    public static List<StandAction> getVictimActions(LivingEntity victim) {
        return resolveAll(ids(dataOrNull(victim), V_ACTIONS));
    }

    public static int getVictimStolenCount(LivingEntity victim) {
        return ids(dataOrNull(victim), V_ACTIONS).size();
    }

    /** Эту жертву уже обворовывает ДРУГОЙ вор (у одной жертвы не может быть двух воров одновременно). */
    public static boolean isRobbedByOther(LivingEntity victim, UUID thief) {
        CompoundNBT d = dataOrNull(victim);
        if (d == null || ids(d, V_ACTIONS).isEmpty() || !d.hasUUID(V_THIEF)) return false;
        return !d.getUUID(V_THIEF).equals(thief);
    }

    /** Временное ослабление цели на время матча: +1 за каждый раунд, выигранный вором. */
    public static void addVictimTemp(LivingEntity victim, int delta, UUID thief) {
        CompoundNBT d = data(victim);
        int n = Math.max(0, Math.min(MAX_THIRDS, d.getInt(V_TEMP) + delta));
        if (n == 0) d.remove(V_TEMP); else d.putInt(V_TEMP, n);
        if (delta > 0 && thief != null && ids(d, V_ACTIONS).isEmpty()) d.putUUID(V_THIEF, thief);
        cleanupVictim(victim, d);
    }

    /** Матч закончился (или отменён): временное ослабление снимается. */
    public static void clearVictimTemp(LivingEntity victim) {
        CompoundNBT d = dataOrNull(victim);
        if (d == null) return;
        d.remove(V_TEMP);
        cleanupVictim(victim, d);
    }

    private static void cleanupVictim(LivingEntity victim, CompoundNBT d) {
        if (ids(d, V_ACTIONS).isEmpty() && d.getInt(V_TEMP) == 0) d.remove(V_THIEF);
        applyStandModifiers(victim, victimThirds(d), heldThirds(d));
        dropIfEmpty(victim);
    }

    private static int victimThirds(CompoundNBT d) {
        if (d == null) return 0;
        return Math.min(MAX_THIRDS, ids(d, V_ACTIONS).size() + d.getInt(V_TEMP));
    }

    private static int heldThirds(CompoundNBT d) {
        if (d == null) return 0;
        return Math.min(MAX_THIRDS, ids(d, H_ACTIONS).size());
    }

    // ------------------------- вор -------------------------
    public static int getHeldCount(LivingEntity thief) {
        return ids(dataOrNull(thief), H_ACTIONS).size();
    }

    public static boolean isHolding(LivingEntity thief) {
        return getHeldCount(thief) > 0;
    }

    /** Можно ли украсть ещё одну способность (есть свободный слот). */
    public static boolean canStealMore(LivingEntity thief, LivingEntity victim) {
        CompoundNBT d = dataOrNull(thief);
        List<String> held = ids(d, H_ACTIONS);
        if (!held.isEmpty() && d.hasUUID(H_VICTIM) && !d.getUUID(H_VICTIM).equals(victim.getUUID())) {
            return true; // прошлая жертва освободится - слоты станут пустыми
        }
        return held.size() < MAX_STOLEN;
    }

    /**
     * Победа вора в матче: отнять у жертвы ещё одну способность.
     * Если вор до этого держал кражу у другого человека - тому всё возвращается.
     */
    public static void stealAction(LivingEntity thief, LivingEntity victim, StandAction action) {
        CompoundNBT old = dataOrNull(thief);
        if (old != null && old.hasUUID(H_VICTIM) && !old.getUUID(H_VICTIM).equals(victim.getUUID())) {
            LivingEntity oldVictim = findEntity(old.getUUID(H_VICTIM));
            if (oldVictim != null) clearVictim(oldVictim);
            clearHeld(thief);
        }

        String id = idOf(action);
        if (id.isEmpty()) return;

        CompoundNBT v = data(victim);
        List<String> vl = ids(v, V_ACTIONS);
        if (!vl.contains(id)) vl.add(id);
        putIds(v, V_ACTIONS, vl);
        v.putUUID(V_THIEF, thief.getUUID());

        CompoundNBT h = data(thief);
        List<String> hl = ids(h, H_ACTIONS);
        if (!hl.contains(id)) hl.add(id);
        putIds(h, H_ACTIONS, hl);
        h.putUUID(H_VICTIM, victim.getUUID());

        // Способность должна быть «открыта» у вора, иначе RotP не даст её выполнить.
        IStandPower.getStandPowerOptional(thief).ifPresent(p -> p.unlockAction(action));

        applyStandModifiers(victim, victimThirds(v), heldThirds(dataOrNull(victim)));
        applyStandModifiers(thief, victimThirds(dataOrNull(thief)), heldThirds(h));
        syncThief(thief);
    }

    /**
     * Победа цели в матче: вор возвращает ОДНУ случайную из украденных способностей.
     * @return вернувшаяся способность или null, если вору нечего возвращать
     */
    public static StandAction returnOneAction(LivingEntity thief, LivingEntity victim) {
        CompoundNBT h = dataOrNull(thief);
        List<String> hl = ids(h, H_ACTIONS);
        if (hl.isEmpty() || h == null || !h.hasUUID(H_VICTIM) || !h.getUUID(H_VICTIM).equals(victim.getUUID())) {
            return null;
        }
        String id = hl.remove(RANDOM.nextInt(hl.size()));
        putIds(h, H_ACTIONS, hl);
        if (hl.isEmpty()) h.remove(H_VICTIM);

        CompoundNBT v = data(victim);
        List<String> vl = ids(v, V_ACTIONS);
        vl.remove(id);
        putIds(v, V_ACTIONS, vl);

        StandAction returned = resolveAction(id);
        if (returned != null) {
            IStandPower.getStandPowerOptional(victim).ifPresent(p -> p.setCooldownTimer(returned, 0));
        }

        cleanupVictim(victim, v);
        applyStandModifiers(thief, victimThirds(dataOrNull(thief)), heldThirds(dataOrNull(thief)));
        dropIfEmpty(thief);
        syncThief(thief);
        return returned;
    }

    // ------------------------- украденные способности -------------------------
    /** Украденная способность в слоте slot (0..MAX_STOLEN-1) или null. Работает и на клиенте, и на сервере. */
    public static StandAction getStolenAction(LivingEntity thief, int slot) {
        if (thief == null || slot < 0) return null;
        List<String> list;
        if (thief.level.isClientSide()) {
            String s = clientStolenActions;
            list = new ArrayList<>();
            if (s != null && !s.isEmpty()) for (String part : s.split(";")) if (!part.isEmpty()) list.add(part);
        } else {
            list = ids(dataOrNull(thief), H_ACTIONS);
        }
        return slot < list.size() ? resolveAction(list.get(slot)) : null;
    }

    /** Id всех украденных способностей через ';' (для пакета клиенту). */
    public static String getStolenActionsJoined(LivingEntity thief) {
        return String.join(";", ids(dataOrNull(thief), H_ACTIONS));
    }

    private static List<StandAction> resolveAll(List<String> ids) {
        List<StandAction> out = new ArrayList<>();
        for (String id : ids) {
            StandAction a = resolveAction(id);
            if (a != null) out.add(a);
        }
        return out;
    }

    private static StandAction resolveAction(String id) {
        if (id == null || id.isEmpty()) return null;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        Object o = JojoCustomRegistries.ACTIONS.getValue(rl);
        return o instanceof StandAction ? (StandAction) o : null;
    }

    /**
     * Случайная способность ПКМ стенда жертвы, кроме блока, действий самого аддона
     * и тех, что у неё уже украдены.
     */
    public static StandAction pickStealableAction(LivingEntity victim) {
        IStandPower p = IStandPower.getStandPowerOptional(victim).orElse(null);
        if (p == null || !p.hasPower() || p.getType() == null) return null;
        StandAction[] bar = p.getType().getDefaultHotbar(ControlScheme.Hotbar.RIGHT_CLICK);
        if (bar == null) return null;

        List<String> already = ids(dataOrNull(victim), V_ACTIONS);
        List<StandAction> pool = new ArrayList<>();
        for (StandAction a : bar) {
            if (a == null || a instanceof StandEntityBlock || a.isShiftVariation()) continue;
            ResourceLocation id = a.getRegistryName();
            if (id == null || AddonMain.MOD_ID.equals(id.getNamespace())) continue;
            if (already.contains(id.toString())) continue;
            if (!a.isUnlocked(p)) continue;
            pool.add(a);
        }
        return pool.isEmpty() ? null : pool.get(RANDOM.nextInt(pool.size()));
    }

    // ------------------------- освобождение -------------------------
    /** Смерть сущности: жертва умерла - вор теряет украденное; вор умер - жертве всё возвращается. */
    public static void onDeath(LivingEntity dead) {
        CompoundNBT d = dataOrNull(dead);
        if (d == null) return;

        if (d.hasUUID(V_THIEF) && !ids(d, V_ACTIONS).isEmpty()) {
            LivingEntity thief = findEntity(d.getUUID(V_THIEF));
            if (thief != null) {
                clearHeld(thief);
                if (thief instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) thief).sendMessage(
                            new TranslationTextComponent("rotp_boyiiman.rps.owner_died"), Util.NIL_UUID);
                }
            }
        }
        if (!ids(d, H_ACTIONS).isEmpty() && d.hasUUID(H_VICTIM)) {
            LivingEntity victim = findEntity(d.getUUID(H_VICTIM));
            if (victim != null) clearVictim(victim);
        }
        dead.getPersistentData().remove(TAG);
        syncThief(dead);
    }

    /** Жертве возвращается всё: способности, сила стенда. */
    private static void clearVictim(LivingEntity victim) {
        CompoundNBT d = dataOrNull(victim);
        if (d == null) return;
        List<StandAction> locked = resolveAll(ids(d, V_ACTIONS));
        d.remove(V_ACTIONS);
        d.remove(V_THIEF);
        d.remove(V_TEMP);
        for (StandAction a : locked) {
            IStandPower.getStandPowerOptional(victim).ifPresent(p -> p.setCooldownTimer(a, 0));
        }
        applyStandModifiers(victim, 0, heldThirds(d));
        dropIfEmpty(victim);
    }

    private static void clearHeld(LivingEntity thief) {
        CompoundNBT d = dataOrNull(thief);
        if (d == null) return;
        d.remove(H_ACTIONS);
        d.remove(H_VICTIM);
        applyStandModifiers(thief, victimThirds(d), 0);
        dropIfEmpty(thief);
        syncThief(thief);
    }

    /** Отправить игроку-вору актуальный список украденных способностей (в т.ч. пустой). */
    public static void syncThief(LivingEntity e) {
        if (e instanceof ServerPlayerEntity) {
            B2MNetwork.syncStolenAction((ServerPlayerEntity) e, getStolenActionsJoined(e));
        }
    }

    // ------------------------- тик (сервер, раз в 5 тиков) -------------------------
    public static void tick(LivingEntity e) {
        CompoundNBT root = e.getPersistentData();
        if (!root.contains(TAG, 10)) return;

        if (e.tickCount % VALIDATE_PERIOD_TICKS == 0) validate(e);

        if (!root.contains(TAG, 10)) return;
        CompoundNBT d = root.getCompound(TAG);

        int v = victimThirds(d);
        int h = heldThirds(d);
        applyStandModifiers(e, v, h);

        // У жертвы отнятые способности недоступны, пока жив владелец.
        for (StandAction locked : resolveAll(ids(d, V_ACTIONS))) {
            IStandPower.getStandPowerOptional(e).ifPresent(p -> p.setCooldownTimer(locked, LOCK_COOLDOWN_TICKS));
        }

        if (h > 0) {
            // Бафф самому владельцу (без каких-либо частиц)
            int amp = Math.max(0, Math.round((float) (h * BoyIIManConfig.STOLEN_POWER_MULTIPLIER.get())) - 1);
            e.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 40, amp, true, false));
        }
        if (d.isEmpty()) root.remove(TAG);
    }

    /**
     * Страховка на случай, если смерть пропущена (владелец был выгружен/офлайн):
     * если вторая сторона найдена, но не помнит эту кражу - значит, она умерла и воскресла.
     */
    private static void validate(LivingEntity e) {
        CompoundNBT d = dataOrNull(e);
        if (d == null) return;

        if (!ids(d, V_ACTIONS).isEmpty() && d.hasUUID(V_THIEF)) {
            LivingEntity thief = findEntity(d.getUUID(V_THIEF));
            if (thief != null && !isHoldingFrom(thief, e.getUUID())) clearVictim(e);
        }
        d = dataOrNull(e);
        if (d != null && !ids(d, H_ACTIONS).isEmpty() && d.hasUUID(H_VICTIM)) {
            LivingEntity victim = findEntity(d.getUUID(H_VICTIM));
            if (victim != null && !isRobbedBy(victim, e.getUUID())) clearHeld(e);
        }
    }

    private static boolean isHoldingFrom(LivingEntity thief, UUID victim) {
        CompoundNBT d = dataOrNull(thief);
        return d != null && !ids(d, H_ACTIONS).isEmpty() && d.hasUUID(H_VICTIM) && d.getUUID(H_VICTIM).equals(victim);
    }

    private static boolean isRobbedBy(LivingEntity victim, UUID thief) {
        CompoundNBT d = dataOrNull(victim);
        return d != null && !ids(d, V_ACTIONS).isEmpty() && d.hasUUID(V_THIEF) && d.getUUID(V_THIEF).equals(thief);
    }

    /** Ослабление стенда жертвы и усиление стенда вора через атрибуты стенд-сущности. */
    private static void applyStandModifiers(LivingEntity owner, int victimThirds, int heldThirds) {
        StandEntity stand = getStandEntity(owner);
        if (stand == null) return;
        double down = -Math.min(victimThirds / 3.0, 0.95);
        double up = heldThirds / 3.0 * BoyIIManConfig.STOLEN_POWER_MULTIPLIER.get();
        Attribute[] attrs = { Attributes.ATTACK_DAMAGE, Attributes.ATTACK_SPEED, Attributes.MOVEMENT_SPEED };
        for (Attribute attr : attrs) {
            ModifiableAttributeInstance inst = stand.getAttribute(attr);
            if (inst == null) continue;
            inst.removeModifier(VICTIM_MOD);
            inst.removeModifier(HELD_MOD);
            if (victimThirds > 0)
                inst.addTransientModifier(new AttributeModifier(VICTIM_MOD, "b2m_victim", down, AttributeModifier.Operation.MULTIPLY_TOTAL));
            if (heldThirds > 0)
                inst.addTransientModifier(new AttributeModifier(HELD_MOD, "b2m_held", up, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    // ------------------------- утилиты -------------------------
    public static LivingEntity findEntity(UUID id) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        for (ServerWorld w : server.getAllLevels()) {
            Entity e = w.getEntity(id);
            if (e instanceof LivingEntity && e.isAlive()) return (LivingEntity) e;
        }
        return null;
    }

    public static StandEntity getStandEntity(LivingEntity e) {
        // Нельзя возвращать null из LazyOptional.map (NPE в Optional.of) - достаём значения через orElse.
        IStandPower power = IStandPower.getStandPowerOptional(e).orElse(null);
        if (power == null) return null;
        Object manifestation = power.getStandManifestation();
        return manifestation instanceof StandEntity ? (StandEntity) manifestation : null;
    }

    public static boolean hasStand(LivingEntity e) {
        return IStandPower.getStandPowerOptional(e).map(IStandPower::hasPower).orElse(false);
    }
}
