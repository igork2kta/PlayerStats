package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateUpgradeCountPacket(int upgradeCount) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("playerstats", "update_upgrade_count");

    public static final Type<UpdateUpgradeCountPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, UpdateUpgradeCountPacket> CODEC =
            StreamCodec.of(UpdateUpgradeCountPacket::encode, UpdateUpgradeCountPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, UpdateUpgradeCountPacket msg) {
        buf.writeInt(msg.upgradeCount());
    }

    private static UpdateUpgradeCountPacket decode(FriendlyByteBuf buf) {
        return new UpdateUpgradeCountPacket(buf.readInt());
    }
}
