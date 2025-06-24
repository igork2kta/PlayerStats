package com.playerstats.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
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
    public static void saveAttribute(Player player, Attribute attr, double value) {
        CompoundTag tag = player.getPersistentData().getCompound(ATTRIBUTES_TAG);
        tag.putDouble(BuiltInRegistries.ATTRIBUTE.getKey(attr).toString(), value);
        player.getPersistentData().put(ATTRIBUTES_TAG, tag);
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
}
