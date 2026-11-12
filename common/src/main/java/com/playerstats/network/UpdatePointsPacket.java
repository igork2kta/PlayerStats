package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;

public class UpdatePointsPacket {
    private final int points;
    private final String pointsType;

    public UpdatePointsPacket(int points, String pointsType) {
        this.points = points;
        this.pointsType = pointsType;
    }

    public int getPoints() { return points; }
    public String getPointsType() { return pointsType; }

    public static void encode(UpdatePointsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.points);
        buf.writeUtf(msg.pointsType);
    }

    public static UpdatePointsPacket decode(FriendlyByteBuf buf) {
        return new UpdatePointsPacket(buf.readInt(), buf.readUtf());
    }
}
