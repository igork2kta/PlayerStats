package com.playerstats.event;

import com.playerstats.network.PacketHandler;
import com.playerstats.network.UpdatePointsPacket;
import com.playerstats.network.UpdateUpgradeCountPacket;
import com.playerstats.util.AttributeUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "playerstats")
public class PlayerAttributePersistence {

    private static final String ATTRIBUTES_TAG = "PlayerStatsAttributes";
    private static final String UPGRADE_COUNT_TAG = "PlayerStatsUpgradeCount";

    // 1. Clona os atributos personalizados entre mortes
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag originalNBT = event.getOriginal().getPersistentData();
        if (originalNBT.contains(ATTRIBUTES_TAG)) {
            event.getEntity().getPersistentData().put(ATTRIBUTES_TAG, originalNBT.getCompound(ATTRIBUTES_TAG));
        }
    }

    // 2. Aplica os atributos quando o jogador entra no mundo
    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        CompoundTag tag = player.getPersistentData().getCompound(ATTRIBUTES_TAG);

        // Inicializa pontos se não existir
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(POINTS_TAG)) {
            setPoints(player, 0); // Exemplo: começa com 10 pontos
        }

        for (String key : tag.getAllKeys()) {
            ResourceLocation id = new ResourceLocation(key);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
            if (attr != null) {
                AttributeInstance instance = player.getAttribute(attr);
                if (instance != null) {
                    instance.setBaseValue(tag.getDouble(key));
                }
            }
        }
    }

    // 3. Metodo utilitário para ser chamado sempre que um atributo for modificado
    public static void saveAttribute(Player player, Attribute attr, double newValue) {
        CompoundTag root = player.getPersistentData();

        CompoundTag attributesTag = root.getCompound(ATTRIBUTES_TAG);

        String key = BuiltInRegistries.ATTRIBUTE.getKey(attr).toString();

        // Se ainda não existe o valor original, salva ele
        if (!attributesTag.contains(key + "_original")) {
            AttributeInstance instance = player.getAttribute(attr);
            if (instance != null) {
                attributesTag.putDouble(key + "_original", instance.getBaseValue());
            }
        }

        // Salva o valor atual do atributo
        attributesTag.putDouble(key + "_current", newValue);

        root.put(ATTRIBUTES_TAG, attributesTag);
    }

    public static void ensurePointsInitialized(Player player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(POINTS_TAG)) {
            setPoints(player, 0); // valor padrão inicial
        }
    }


    private static final String POINTS_TAG = "PlayerStatsPoints";

    public static int getPoints(Player player) {
        CompoundTag tag = player.getPersistentData();
        return tag.getInt(POINTS_TAG);
    }

    public static void setPoints(Player player, int points) {
        CompoundTag tag = player.getPersistentData();
        tag.putInt(POINTS_TAG, points);
    }

    public static void addPoints(Player player, int points) {
        CompoundTag tag = player.getPersistentData();
        int playerPoints = tag.getInt(POINTS_TAG);
        tag.putInt(POINTS_TAG, playerPoints + points);
    }

    public static void resetAttributes(Player player) {

        // Verifica se o jogador tem pelo menos 50 níveis
        if (player.experienceLevel < 50) {
            player.sendSystemMessage(Component.translatable("gui.playerstats.cant_reset"));
            return;
        }


        CompoundTag root = player.getPersistentData();
        CompoundTag tag = root.getCompound(ATTRIBUTES_TAG);

        int refundedPoints = 0;

        for (String key : tag.getAllKeys()) {
            if (key.endsWith("_original")) {
                String attrKey = key.replace("_original", "");
                ResourceLocation id = new ResourceLocation(attrKey);
                Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
                if (attr != null) {
                    AttributeInstance instance = player.getAttribute(attr);
                    if (instance != null) {
                        double originalValue = tag.getDouble(key);
                        double currentValue = instance.getBaseValue();
                        instance.setBaseValue(originalValue);

                        double increment = AttributeUtils.getIncrement(AttributeUtils.getAttributeName(attr));
                        int spentPoints = (int) Math.round((currentValue - originalValue)/increment);
                        if (spentPoints > 0) {
                            refundedPoints += spentPoints;
                        }
                    }
                }
            }
        }

        root.remove(UPGRADE_COUNT_TAG);

        root.remove(ATTRIBUTES_TAG);

        int currentPoints = getPoints(player);
        setPoints(player, currentPoints + refundedPoints);

        // Remove 50 níveis do jogador
        player.giveExperienceLevels(-50);

        player.sendSystemMessage(Component.literal("§eAtributos resetados. Pontos devolvidos: " + refundedPoints));

        // Atualiza o cache de pontos (ou manda pacote pro cliente)
        PacketHandler.sendToClient(new UpdatePointsPacket(getPoints(player)), (ServerPlayer) player);
        int count = PlayerAttributePersistence.getUpgradeCount(player);
        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(count), (ServerPlayer) player);
    }

    public static int getUpgradeCount(Player player) {
        CompoundTag tag = player.getPersistentData();
        return tag.getInt(UPGRADE_COUNT_TAG);
    }

    public static void incrementUpgradeCount(Player player) {
        CompoundTag tag = player.getPersistentData();
        int count = tag.getInt(UPGRADE_COUNT_TAG);
        tag.putInt(UPGRADE_COUNT_TAG, count + 1);
    }
}
