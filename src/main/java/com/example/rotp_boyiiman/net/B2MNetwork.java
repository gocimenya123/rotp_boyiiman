package com.example.rotp_boyiiman.net;

import com.example.rotp_boyiiman.AddonMain;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/** Один канал и один пакет: синхронизация украденной способности с клиентом вора. */
public final class B2MNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AddonMain.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private static boolean registered;

    public static void init() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(0, StolenSyncPacket.class,
                StolenSyncPacket::encode, StolenSyncPacket::decode, StolenSyncPacket::handle);
    }

    public static void syncStolenAction(ServerPlayerEntity player, String actionId) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new StolenSyncPacket(actionId));
    }

    private B2MNetwork() {}
}
