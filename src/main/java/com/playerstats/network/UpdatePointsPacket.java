package com.playerstats.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdatePointsPacket {
    private final int points;
    private final String pointsType;
    public UpdatePointsPacket(int points, String pointsType) {
        this.points = points;
        this.pointsType = pointsType;
    }

    public static void encode(UpdatePointsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.points);
        buf.writeUtf(msg.pointsType);
    }

    public static UpdatePointsPacket decode(FriendlyByteBuf buf) {
        return new UpdatePointsPacket(buf.readInt(),  buf.readUtf());
    }

    public static void handle(UpdatePointsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if(msg.pointsType.equals("attribute"))
                com.playerstats.client.ClientAttributeCache.setPoints(msg.points);
            else if(msg.pointsType.equals("ability"))
                com.playerstats.client.ClientAttributeCache.setAbilityPoints(msg.points);
        });
        ctx.get().setPacketHandled(true);
    }
}
