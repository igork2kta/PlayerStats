package com.playerstats.network;

import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResetAttributesPacket {
    public ResetAttributesPacket() {}
    public ResetAttributesPacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            PlayerAttributePersistence.resetAttributes(player, false);
        });
        ctx.get().setPacketHandled(true);
    }
}

