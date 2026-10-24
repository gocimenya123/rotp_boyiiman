package com.example.rotp_boyiiman.net;

import java.util.function.Supplier;

import com.example.rotp_boyiiman.rps.StolenEnergy;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/** Сервер -> вор: id украденных способностей через ';' (пустая строка - ничего не украдено). Нужен клиенту для HUD и клика. */
public class StolenSyncPacket {
    private final String actionId;

    public StolenSyncPacket(String actionId) {
        this.actionId = actionId == null ? "" : actionId;
    }

    public static void encode(StolenSyncPacket msg, PacketBuffer buf) {
        buf.writeUtf(msg.actionId);
    }

    public static StolenSyncPacket decode(PacketBuffer buf) {
        return new StolenSyncPacket(buf.readUtf(1024));
    }

    public static void handle(StolenSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> StolenEnergy.clientStolenActions = msg.actionId);
        ctx.get().setPacketHandled(true);
    }
}
