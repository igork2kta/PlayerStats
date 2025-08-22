package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ModifyAttributePacket(int entityId, String attributeId) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("playerstats", "modify_attribute");

    public static final Type<ModifyAttributePacket> TYPE = new Type<>(ID);

    // Codec para serializar/deserializar
    public static final StreamCodec<FriendlyByteBuf, ModifyAttributePacket> CODEC =
            StreamCodec.of(ModifyAttributePacket::encode, ModifyAttributePacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, ModifyAttributePacket msg) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.attributeId);
    }

    private static ModifyAttributePacket decode(FriendlyByteBuf buf) {
        return new ModifyAttributePacket(buf.readInt(), buf.readUtf());
    }
}
