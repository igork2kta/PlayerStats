package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ActivateInfiniteIcePacket() implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("playerstats", "activate_infinite_ice");
    public static final Type<ActivateInfiniteIcePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ActivateInfiniteIcePacket> CODEC =
            StreamCodec.of((buf, val) -> {}, buf -> new ActivateInfiniteIcePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

