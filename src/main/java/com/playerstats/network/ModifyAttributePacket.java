package com.playerstats.network;

import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ModifyAttributePacket {
    private final String attributeId;
    private final double amount;

    public ModifyAttributePacket(String attributeId, double amount) {
        this.attributeId = attributeId;
        this.amount = amount;
    }

    public static void encode(ModifyAttributePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.attributeId);
        buf.writeDouble(msg.amount);
    }

    public static ModifyAttributePacket decode(FriendlyByteBuf buf) {
        return new ModifyAttributePacket(buf.readUtf(), buf.readDouble());
    }

    public static void handle(ModifyAttributePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ResourceLocation id = new ResourceLocation(msg.attributeId);
                Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);

                if (attr != null) {
                    AttributeInstance instance = player.getAttribute(attr);
                    if (instance != null) {
                        double newValue = instance.getBaseValue() + msg.amount;
                        instance.setBaseValue(newValue);
                        System.out.println("Set " + id + " to " + newValue + " for " + player.getName().getString());
                        PlayerAttributePersistence.saveAttribute(player, instance.getAttribute(), newValue);
                    } else {
                        System.err.println("AttributeInstance is null for: " + id);
                    }
                } else {
                    System.err.println("Unknown attribute ID: " + id);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
