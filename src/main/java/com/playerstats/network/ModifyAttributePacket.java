package com.playerstats.network;

import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.util.AttributeUtils;
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
    private final boolean isToggle;

    public ModifyAttributePacket(int entityId, String attributeId) {
        this(entityId, attributeId, false);
    }

    public ModifyAttributePacket(int entityId, String attributeId, boolean isToggle) {
        this.entityId = entityId;
        this.attributeId = attributeId;
        this.isToggle = isToggle;
    }

    public static void encode(ModifyAttributePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.attributeId);
        buf.writeBoolean(msg.isToggle);
    }

    public static ModifyAttributePacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        String attrId = buf.readUtf();
        boolean isToggle = buf.isReadable() && buf.readBoolean();
        return new ModifyAttributePacket(entityId, attrId, isToggle);
    }

    public static void handle(ModifyAttributePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                var level = player.level();
                var entity = level.getEntity(msg.entityId);

                if (entity instanceof LivingEntity living) {
                    var attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.tryParse(msg.attributeId));
                    if (attribute != null) {
                        var instance = AttributeUtils.getAttributeInstance(living, attribute);
                        if (instance != null) {
                            if (msg.isToggle) {
                                PlayerAttributePersistence.toggleAbility(living, player, msg.attributeId);
                            } else {
                                PlayerAttributePersistence.upgradeAttribute(living, player, msg.attributeId);
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}