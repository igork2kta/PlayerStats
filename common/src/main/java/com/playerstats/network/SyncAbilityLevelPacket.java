package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncAbilityLevelPacket(int entityId, String attributeId, int level) implements CustomPacketPayload {

    public SyncAbilityLevelPacket(String attributeId, int level) {
        this(-1, attributeId, level);
    }

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath("playerstats", "sync_ability_level");

    public static final Type<SyncAbilityLevelPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncAbilityLevelPacket> CODEC =
            StreamCodec.of(SyncAbilityLevelPacket::encode, SyncAbilityLevelPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, SyncAbilityLevelPacket msg) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.attributeId);
        buf.writeInt(msg.level);
    }

    private static SyncAbilityLevelPacket decode(FriendlyByteBuf buf) {
        return new SyncAbilityLevelPacket(buf.readInt(), buf.readUtf(), buf.readInt());
    }
}

