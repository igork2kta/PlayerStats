package com.playerstats.network;

import com.playerstats.event.InfiniteIceHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ActivateInfiniteIcePacket {

    public ActivateInfiniteIcePacket() {
    }

    public static void encode(ActivateInfiniteIcePacket msg, FriendlyByteBuf buf) {
    }

    public static ActivateInfiniteIcePacket decode(FriendlyByteBuf buf) {
        return new ActivateInfiniteIcePacket();
    }

    public static void handle(ActivateInfiniteIcePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                InfiniteIceHandler.activateAbility(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

