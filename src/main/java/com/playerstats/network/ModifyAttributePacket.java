package com.playerstats.network;

import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.GameType;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ModifyAttributePacket {
    private final String attributeId;
    private final double increment;

    public ModifyAttributePacket(String attributeId, double increment) {
        this.attributeId = attributeId;
        this.increment = increment;
    }

    public static void encode(ModifyAttributePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.attributeId);
        buf.writeDouble(msg.increment);
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
                    int points = PlayerAttributePersistence.getPoints(player);
                    int xpLevel = player.experienceLevel;
                    int upgradesFeitos = PlayerAttributePersistence.getUpgradeCount(player);
                    int xpCusto = (upgradesFeitos + 1) * 5; // começa com 5

                    if(player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) xpLevel = xpCusto;

                    if (instance != null && points > 0 && xpLevel >= xpCusto) {

                        double newValue = instance.getBaseValue() + msg.increment;

                        //Rastreamento de alterações
                        //AttributeTracker.recordChange(player, attr, msg.amount);
                        System.out.println("Set " + id + " to " + newValue + " for " + player.getName().getString());

                        PlayerAttributePersistence.applyUpgrade(player, instance.getAttribute());
                        PlayerAttributePersistence.setPoints(player, points - 1);

                        instance.setBaseValue(newValue);

                        int newPoints = PlayerAttributePersistence.getPoints(player);
                        PacketHandler.sendToClient(new UpdatePointsPacket(newPoints), player);


                        //PlayerAttributePersistence.incrementUpgradeCount(player); // ✅ incrementa contagem

                        int count = PlayerAttributePersistence.getUpgradeCount(player);
                        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(count), player);
                        player.giveExperienceLevels(-xpCusto); // ✅ remove níveis

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
