package com.playerstats.network;

import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
public class ModifyAttributePacket {
    private final int entityId;
    private final String attributeId;

    public ModifyAttributePacket(int entityId, String attributeId) {
        this.entityId = entityId;
        this.attributeId = attributeId;
    }

    public static void encode(ModifyAttributePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.attributeId);
    }

    public static ModifyAttributePacket decode(FriendlyByteBuf buf) {
        return new ModifyAttributePacket(buf.readInt(), buf.readUtf());
    }

    public static void handle(ModifyAttributePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                var level = player.level();
                var entity = level.getEntity(msg.entityId);

                if (entity instanceof LivingEntity living) {
                    // Aqui você faz a alteração de atributo
                    var attribute = BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(msg.attributeId));
                    if (attribute != null) {
                        var instance = living.getAttribute(attribute);
                        if (instance != null) {

                                PlayerAttributePersistence.upgradeAttribute(living, player, msg.attributeId);

                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}