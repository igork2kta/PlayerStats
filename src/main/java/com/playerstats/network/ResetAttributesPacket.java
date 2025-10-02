package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ResetAttributesPacket(int entityId) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("playerstats", "reset_attribute");

    public static final Type<ResetAttributesPacket> TYPE = new Type<>(ID);

    // Codec para serializar/deserializar
    public static final StreamCodec<FriendlyByteBuf, ResetAttributesPacket> CODEC =
            StreamCodec.of(ResetAttributesPacket::encode, ResetAttributesPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, ResetAttributesPacket msg) {
        buf.writeInt(msg.entityId);
    }

    private static ResetAttributesPacket decode(FriendlyByteBuf buf) {
        return new ResetAttributesPacket(buf.readInt());
    }
}
