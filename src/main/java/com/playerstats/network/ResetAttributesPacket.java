package com.playerstats.network;

import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResetAttributesPacket {
    private final int entityId;

    public ResetAttributesPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(ResetAttributesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static ResetAttributesPacket decode(FriendlyByteBuf buf) {
        return new ResetAttributesPacket(buf.readInt());
    }

    public static void handle(ResetAttributesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                var level = player.level();
                var entity = level.getEntity(msg.entityId);

                if (entity instanceof LivingEntity living) {
                    PlayerAttributePersistence.resetAttributes(living, player, false);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}