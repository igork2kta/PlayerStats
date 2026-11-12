package com.playerstats.network;

import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Solicita ao servidor o estado atual de melhorias e atributos desativados de uma entidade.
 */
public class RequestEntityUpgradesPacket {
    private final int entityId;

    public RequestEntityUpgradesPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(RequestEntityUpgradesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static RequestEntityUpgradesPacket decode(FriendlyByteBuf buf) {
        return new RequestEntityUpgradesPacket(buf.readInt());
    }

    public static void handle(RequestEntityUpgradesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                var entity = player.level().getEntity(msg.entityId);
                if (entity instanceof LivingEntity living) {
                    PlayerAttributePersistence.syncEntityUpgrades(living, player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
