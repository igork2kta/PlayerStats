package com.playerstats.network;

import net.minecraft.network.FriendlyByteBuf;

public class ActivateInfiniteIcePacket {

    public ActivateInfiniteIcePacket() {
    }

    public static void encode(ActivateInfiniteIcePacket msg, FriendlyByteBuf buf) {
    }

    public static ActivateInfiniteIcePacket decode(FriendlyByteBuf buf) {
        return new ActivateInfiniteIcePacket();
    }
}
