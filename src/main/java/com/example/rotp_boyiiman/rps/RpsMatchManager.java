package com.example.rotp_boyiiman.rps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.example.rotp_boyiiman.config.BoyIIManConfig;
import com.example.rotp_boyiiman.init.InitSounds;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Util;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

/**
 * Вся серверная логика матча Камень-Ножницы-Бумага.
 * Приглашение - кликабельный чат (/b2m accept | decline), сама игра - меню
 * Камень-Ножницы-Бумага из основного мода (RockPaperScissorsGame + его пакеты).
 */
public final class RpsMatchManager {

    private static final int WINS_NEEDED = 3;
    private static final Random RANDOM = new Random();

    /** Ключ — UUID каждого из двух участников (матч лежит под обоими ключами). */
    private static final Map<UUID, RpsMatch> MATCHES = new HashMap<>();

    private RpsMatchManager() {}

    // =================================================================
    // Вызов / принятие / отказ
    // =================================================================

    public static void challenge(ServerPlayerEntity challenger, LivingEntity target) {
        challenge(challenger, target, false);
    }

    /**
     * @param forced true - игра начинается сразу, без согласия цели (скилл максимальной решимости)
     */
    public static void challenge(ServerPlayerEntity challenger, LivingEntity target, boolean forced) {
        if (!BoyIIManConfig.ENABLE_THEFT.get()) {
            tell(challenger, "theft_disabled");
            return;
        }

        if (target == challenger) return;

        if (MATCHES.containsKey(challenger.getUUID())
                || MATCHES.containsKey(target.getUUID())) {
            tell(challenger, "busy");
            return;
        }

        // С одним и тем же человеком можно играть сколько угодно раз;
        // нельзя только обворовывать того, кого уже обворовывает кто-то другой.
        if (StolenEnergy.isRobbedByOther(target, challenger.getUUID())) {
            tell(challenger, "already_robbed");
            return;
        }

        if (target instanceof PlayerEntity && !BoyIIManConfig.ALLOW_PLAYERS.get()) {
            tell(challenger, "no_players");
            return;
        }

        if (!(target instanceof PlayerEntity) && !BoyIIManConfig.ALLOW_MOBS.get()) {
            tell(challenger, "no_mobs");
            return;
        }

        if (isBoss(target) && !BoyIIManConfig.ALLOW_BOSSES.get()) {
            tell(challenger, "no_boss");
            return;
        }

        double range = BoyIIManConfig.CHALLENGE_RANGE.get();

        if (challenger.distanceToSqr(target) > range * range) {
            tell(challenger, "too_far");
            return;
        }

        if (!StolenEnergy.hasStand(target)) {
            tell(challenger, "no_stand");
            return;
        }

        long now = challenger.level.getGameTime();
        RpsMatch m = new RpsMatch(challenger.getUUID(), target.getUUID(), now);

        MATCHES.put(m.challenger, m);
        MATCHES.put(m.target, m);

        if (target instanceof ServerPlayerEntity && !forced) {
            // Игрок должен принять вызов.
            tell(challenger, "invite_sent", target.getDisplayName());

            ((ServerPlayerEntity) target).sendMessage(
                    new TranslationTextComponent(
                            "rotp_boyiiman.rps.invite",
                            challenger.getDisplayName()
                    )
                            .append(new StringTextComponent(" "))
                            .append(button(
                                    new TranslationTextComponent("rotp_boyiiman.rps.btn.accept"),
                                    "/b2m accept",
                                    TextFormatting.GREEN
                            ))
                            .append(new StringTextComponent(" "))
                            .append(button(
                                    new TranslationTextComponent("rotp_boyiiman.rps.btn.decline"),
                                    "/b2m decline",
                                    TextFormatting.RED
                            )),
                    Util.NIL_UUID
            );
        } else {
            // Моб принимает автоматически; при принудительном вызове - и игрок тоже.
            if (forced && target instanceof ServerPlayerEntity) {
                tell(target, "forced", challenger.getDisplayName());
            }
            start(m);
        }
    }

    public static void accept(ServerPlayerEntity p) {
        RpsMatch m = MATCHES.get(p.getUUID());

        if (m == null
                || m.phase != RpsMatch.Phase.INVITED
                || !m.target.equals(p.getUUID())) {
            tell(p, "no_invite");
            return;
        }

        start(m);
    }

    public static void decline(ServerPlayerEntity p) {
        RpsMatch m = MATCHES.get(p.getUUID());

        if (m == null
                || m.phase != RpsMatch.Phase.INVITED
                || !m.target.equals(p.getUUID())) {
            tell(p, "no_invite");
            return;
        }

        cancel(m, "declined");
    }

    private static void start(RpsMatch m) {
        LivingEntity c = find(m.challenger);
        LivingEntity t = find(m.target);

        if (c == null || t == null || !(c.level instanceof ServerWorld)) {
            cancel(m, "cancelled");
            return;
        }

        m.phase = RpsMatch.Phase.PLAYING;
        m.lastActivity = c.level.getGameTime();

        // Игра и меню - из основного мода: gameStarted шлёт обоим игрокам пакет, который открывает экран.
        m.game = new RockPaperScissorsGame(c, t);
        setCurrentGame(c, m.game);
        setCurrentGame(t, m.game);

        tellBoth(m, "started");
        m.game.gameStarted((ServerWorld) c.level);
    }

    private static void setCurrentGame(LivingEntity e, RockPaperScissorsGame game) {
        if (e instanceof ServerPlayerEntity) {
            e.getCapability(PlayerUtilCapProvider.CAPABILITY)
                    .ifPresent(cap -> cap.setCurrentRockPaperScissorsGame(game));
        }
    }

    // =================================================================
    // Ход матча (раз в тик): ходы мобов, выигранные раунды, конец игры
    // =================================================================

    private static void updateMatch(RpsMatch m) {
        LivingEntity c = find(m.challenger);
        LivingEntity t = find(m.target);

        if (c == null || t == null) {
            cancel(m, "cancelled");
            return;
        }

        RockPaperScissorsGame game = m.game;

        if (game.playerLeft()) {
            // Кто-то закрыл меню / вышел из игры.
            cancel(m, "cancelled");
            return;
        }

        long now = c.level.getGameTime();

        // Мобы (не игроки) выбирают ход случайно, как только раунд начался.
        pickForMob(game, c);
        pickForMob(game, t);

        if (game.getRound() != m.lastRound) {
            m.lastRound = game.getRound();
            m.lastActivity = now;
        }

        // Выигранные раунды -> кража трети энергии стенда цели.
        int cWins = game.getPlayer(c).getScore();
        int tWins = game.getPlayer(t).getScore();

        while (m.challengerWins < cWins) {
            m.challengerWins++;
            StolenEnergy.addVictimTemp(t, 1, c.getUUID());
            theftEffects(c, t);

            tell(c, "round_won");
            tell(c, "steal_thief");
            Voice.say(c, InitSounds.OWNER_ROUND_WIN.get());

            tell(t, "round_lost");
            tell(t, "steal_victim", c.getDisplayName());
        }

        while (m.targetWins < tWins) {
            m.targetWins++;
            tell(t, "round_won");
            tell(c, "round_lost");
            Voice.say(c, InitSounds.OWNER_ROUND_LOSE.get());
        }

        if (!game.isGameOver()) {
            return;
        }

        // Временное ослабление на время матча снимается в любом случае.
        StolenEnergy.clearVictimTemp(t);

        if (m.challenger.equals(game.getWinner())) {
            // Победа вора: у цели отнимается ЕЩЁ ОДНА способность ПКМ (кроме блока и уже украденных).
            tell(c, "match_won");
            tell(t, "match_lost");
            Voice.say(c, InitSounds.OWNER_MATCH_WIN.get());

            if (!BoyIIManConfig.ENABLE_ABILITY_STEAL.get()) {
                // кража способностей выключена в конфиге - матч ничего не даёт
            } else if (!StolenEnergy.canStealMore(c, t)) {
                tell(c, "stolen_full", StolenEnergy.MAX_STOLEN);
            } else {
                StandAction stolen = StolenEnergy.pickStealableAction(t);
                if (stolen != null) {
                    StolenEnergy.stealAction(c, t, stolen);
                    theftEffects(c, t);
                    Voice.say(c, InitSounds.OWNER_STEAL.get());
                    ITextComponent name = actionName(stolen);
                    tell(c, "ability_stolen_thief", name);
                    tell(t, "ability_stolen_victim", name, c.getDisplayName());
                } else {
                    tell(c, "no_ability");
                }
            }
        } else {
            // Победа цели: вор возвращает ОДНУ из украденных у неё способностей.
            tell(t, "match_won");
            tell(c, "match_lost");
            Voice.say(c, InitSounds.OWNER_MATCH_LOSE.get());

            StandAction returned = StolenEnergy.returnOneAction(c, t);
            if (returned != null) {
                ITextComponent name = actionName(returned);
                tell(t, "ability_returned_victim", name);
                tell(c, "ability_returned_thief", name);
            } else {
                tell(t, "nothing_to_return");
            }
        }

        end(m);
    }

    private static void pickForMob(RockPaperScissorsGame game, LivingEntity e) {
        if (e instanceof PlayerEntity || game.getPlayer(e).getCurrentPick() != null) {
            return;
        }

        RockPaperScissorsGame.Pick[] picks = RockPaperScissorsGame.Pick.values();
        game.makeAPick(e, picks[RANDOM.nextInt(picks.length)], false);
    }

    // =================================================================
    // Завершение / отмена / тик
    // =================================================================

    private static void end(RpsMatch m) {
        MATCHES.remove(m.challenger);
        MATCHES.remove(m.target);

        if (m.game != null) {
            setCurrentGame(find(m.challenger), null);
            setCurrentGame(find(m.target), null);
        }
    }

    /**
     * Отмена: если шла игра, украденное в этом матче возвращается жертве.
     */
    private static void cancel(RpsMatch m, String reasonKey) {
        if (m.phase == RpsMatch.Phase.PLAYING) {
            LivingEntity t = find(m.target);

            if (t != null) {
                StolenEnergy.clearVictimTemp(t);
            }

            // Закрыть меню у игроков (если игра ещё не закончена).
            if (m.game != null && !m.game.isGameOver() && !m.game.playerLeft()) {
                LivingEntity any = t != null ? t : find(m.challenger);

                if (any != null) {
                    try {
                        m.game.leaveGame(any);
                    } catch (RuntimeException ignored) {
                        // игрок уже вышел - закрывать нечего
                    }
                }
            }
        }

        tellBoth(m, reasonKey);
        end(m);
    }

    /** Вызывать каждый тик сервера. */
    public static void tick(MinecraftServer server) {
        if (MATCHES.isEmpty()) {
            return;
        }

        boolean slowTick = server.getTickCount() % 20 == 0;
        long timeout = BoyIIManConfig.INVITE_TIMEOUT_SECONDS.get() * 20L;
        double maxDist = BoyIIManConfig.CHALLENGE_RANGE.get() * 2.0;

        List<RpsMatch> list = new ArrayList<>(new HashSet<>(MATCHES.values()));

        for (RpsMatch m : list) {
            if (!MATCHES.containsValue(m)) {
                continue; // уже завершён в этом же тике
            }

            LivingEntity c = find(m.challenger);
            LivingEntity t = find(m.target);

            if (c == null || t == null) {
                cancel(m, "cancelled");
                continue;
            }

            if (m.phase == RpsMatch.Phase.PLAYING) {
                updateMatch(m);

                if (!MATCHES.containsValue(m)) {
                    continue;
                }
            }

            if (!slowTick) {
                continue;
            }

            if (c.level != t.level || c.distanceToSqr(t) > maxDist * maxDist) {
                cancel(m, "too_far");
                continue;
            }

            if (c.level.getGameTime() - m.lastActivity > timeout) {
                cancel(m, "timeout");
            }
        }
    }

    /**
     * Выход игрока: матч отменяется, украденное в нём возвращается.
     */
    public static void onLogout(ServerPlayerEntity p) {
        RpsMatch m = MATCHES.get(p.getUUID());

        if (m != null) {
            cancel(m, "cancelled");
        }
    }

    public static void clearAll() {
        MATCHES.clear();
    }

    // =================================================================
    // Визуальные эффекты / сообщения
    // =================================================================

    /** Эффект кражи - только звук (частиц нет). */
    private static void theftEffects(LivingEntity thief, LivingEntity victim) {
        if (!(victim.level instanceof ServerWorld)) {
            return;
        }

        ((ServerWorld) victim.level).playSound(
                null,
                victim.blockPosition(),
                InitSounds.B2M_STEAL.get(),
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );
    }

    private static IFormattableTextComponent button(
            ITextComponent label,
            String command,
            TextFormatting color
    ) {
        return new StringTextComponent("")
                .append(label)
                .withStyle(s -> s
                        .withColor(color)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                command
                        ))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new StringTextComponent(command)
                        ))
                );
    }

    private static void tell(Entity e, String key, Object... args) {
        send(e, new TranslationTextComponent(
                "rotp_boyiiman.rps." + key,
                args
        ));
    }

    private static void tellBoth(RpsMatch m, String key) {
        tell(find(m.challenger), key);
        tell(find(m.target), key);
    }

    private static void send(Entity e, ITextComponent text) {
        if (e instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) e).sendMessage(text, Util.NIL_UUID);
        }
    }

    // =================================================================
    // Вспомогательные методы
    // =================================================================

    private static boolean isBoss(LivingEntity e) {
        return e instanceof WitherEntity || e instanceof EnderDragonEntity;
    }

    private static LivingEntity find(UUID id) {
        return StolenEnergy.findEntity(id);
    }

    /** Название способности для сообщений (стандартный ключ RotP: action.<namespace>.<path>). */
    private static ITextComponent actionName(StandAction action) {
        ResourceLocation id = action.getRegistryName();
        return new TranslationTextComponent(id == null ? "" : "action." + id.getNamespace() + "." + id.getPath());
    }
}
