package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Solicita ao servidor o estado atual de melhorias e atributos desativados de uma entidade.
 */
public record RequestEntityUpgradesPacket(int entityId) implements CustomPacketPayload {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath("playerstats", "request_entity_upgrades");

    public static final Type<RequestEntityUpgradesPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, RequestEntityUpgradesPacket> CODEC =
            StreamCodec.of(RequestEntityUpgradesPacket::encode, RequestEntityUpgradesPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, RequestEntityUpgradesPacket msg) {
        buf.writeInt(msg.entityId);
    }

    private static RequestEntityUpgradesPacket decode(FriendlyByteBuf buf) {
        return new RequestEntityUpgradesPacket(buf.readInt());
    }
}
