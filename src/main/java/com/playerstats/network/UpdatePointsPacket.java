package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdatePointsPacket(int points, String pointsType) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("playerstats", "update_points");

    public static final Type<UpdatePointsPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, UpdatePointsPacket> CODEC =
            StreamCodec.of(UpdatePointsPacket::encode, UpdatePointsPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, UpdatePointsPacket msg) {
        buf.writeInt(msg.points());
        buf.writeUtf(msg.pointsType);
    }

    private static UpdatePointsPacket decode(FriendlyByteBuf buf) {
        return new UpdatePointsPacket(buf.readInt(),  buf.readUtf());

    }
}
