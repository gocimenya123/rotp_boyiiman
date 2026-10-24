package com.example.rotp_boyiiman.event;

import com.example.rotp_boyiiman.AddonMain;
import com.example.rotp_boyiiman.entity.BoyIIManEntity;
import com.example.rotp_boyiiman.init.InitSounds;
import com.example.rotp_boyiiman.rps.B2MCommand;
import com.example.rotp_boyiiman.rps.Voice;
import com.example.rotp_boyiiman.rps.RpsMatchManager;
import com.example.rotp_boyiiman.rps.StolenEnergy;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

/** Серверные события (шина Forge). */
@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID)
public class B2MEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        B2MCommand.register(event.getDispatcher());
    }

    /** Эффекты украденной энергии: раз в 5 тиков, только сервер. */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity e = event.getEntityLiving();
        if (e.level.isClientSide() || e.tickCount % 5 != 0) return;
        StolenEnergy.tick(e);
    }

    /** Умер владелец стенда (жертва) - вор теряет украденное; умер вор - жертве всё возвращается. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity e = event.getEntityLiving();
        if (!e.level.isClientSide()) StolenEnergy.onDeath(e);
    }

    /** Фраза владельца при призыве стенда. */
    @SubscribeEvent
    public static void onStandSummoned(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide() || !(event.getEntity() instanceof BoyIIManEntity)) return;
        Voice.say(((BoyIIManEntity) event.getEntity()).getUser(), InitSounds.OWNER_SUMMON.get());
    }

    /** Клиенту вора нужно знать украденную способность после входа/респавна. */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        StolenEnergy.syncThief(event.getPlayer());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        StolenEnergy.syncThief(event.getPlayer());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RpsMatchManager.tick(net.minecraftforge.fml.server.ServerLifecycleHooks.getCurrentServer());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) RpsMatchManager.onLogout((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public static void onServerStopped(FMLServerStoppedEvent event) {
        RpsMatchManager.clearAll();
    }
}
