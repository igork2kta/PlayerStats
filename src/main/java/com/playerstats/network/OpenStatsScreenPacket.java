package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenStatsScreenPacket {

    public static void encode(OpenStatsScreenPacket msg, FriendlyByteBuf buf) {}
    public static OpenStatsScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenStatsScreenPacket();
    }

    public static void handle(OpenStatsScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Chama lógica do cliente num lugar seguro
            com.playerstats.client.ClientHooks.openStatsScreen();
        });
        ctx.get().setPacketHandled(true);
    }
}
