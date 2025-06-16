package com.playerstats.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdatePointsPacket {
    private final int points;

    public UpdatePointsPacket(int points) {
        this.points = points;
    }

    public static void encode(UpdatePointsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.points);
    }

    public static UpdatePointsPacket decode(FriendlyByteBuf buf) {
        return new UpdatePointsPacket(buf.readInt());
    }

    public static void handle(UpdatePointsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.playerstats.client.ClientAttributeCache.setPoints(msg.points);
        });
        ctx.get().setPacketHandled(true);
    }
}
