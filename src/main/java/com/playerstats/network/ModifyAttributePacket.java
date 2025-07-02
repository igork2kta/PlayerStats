package com.playerstats.network;

import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ModifyAttributePacket {
    private final String attributeId;

    public ModifyAttributePacket(String attributeId) {
        this.attributeId = attributeId;

    }

    public static void encode(ModifyAttributePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.attributeId);
    }

    public static ModifyAttributePacket decode(FriendlyByteBuf buf) {
        return new ModifyAttributePacket(buf.readUtf());
    }



    public static void handle(ModifyAttributePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PlayerAttributePersistence.upgradeAttribute(player, msg.attributeId);
            }

        });
        ctx.get().setPacketHandled(true);
    }
}
