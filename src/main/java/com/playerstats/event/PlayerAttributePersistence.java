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
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "playerstats")
public class PlayerAttributePersistence {

    private static final String ATTRIBUTE_UPGRADES_TAG = "PlayerStatsUpgrades";
    private static final String POINTS_TAG = "PlayerStatsPoints";
    private static final String UPGRADE_COUNT_TAG = "PlayerStatsUpgradeCount";

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag originalNBT = event.getOriginal().getPersistentData();
        if (originalNBT.contains(ATTRIBUTE_UPGRADES_TAG)) {
            event.getEntity().getPersistentData().put(ATTRIBUTE_UPGRADES_TAG, originalNBT.getCompound(ATTRIBUTE_UPGRADES_TAG));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        /*
        CompoundTag root = player.getPersistentData();

        ensurePointsInitialized(player);

        CompoundTag upgradesTag = root.getCompound(ATTRIBUTE_UPGRADES_TAG);
        for (String key : upgradesTag.getAllKeys()) {
            ResourceLocation id = new ResourceLocation(key);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
            AttributeInstance instance = player.getAttribute(attr);
            if (attr != null && instance != null) {
                int upgradeCount = upgradesTag.getInt(key);
                double increment = AttributeUtils.getIncrement(attr.getDescriptionId());
                instance.setBaseValue(instance.getBaseValue() + (upgradeCount * increment));
            }
        }
*/
        PacketHandler.sendToClient(new UpdatePointsPacket(getPoints(player)), (ServerPlayer) player);
        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(getUpgradeCount(player)), (ServerPlayer) player);
    }

    public static void applyUpgrade(Player player, Attribute attr) {
        ResourceLocation key = BuiltInRegistries.ATTRIBUTE.getKey(attr);
        CompoundTag upgrades = player.getPersistentData().getCompound(ATTRIBUTE_UPGRADES_TAG);



        int currentUpgrades = upgrades.getInt(key.toString());
        upgrades.putInt(key.toString(), currentUpgrades + 1);
        player.getPersistentData().put(ATTRIBUTE_UPGRADES_TAG, upgrades);

        AttributeInstance instance = player.getAttribute(attr);
        if (instance != null) {
            double increment = AttributeUtils.getIncrement(attr.getDescriptionId());
            instance.setBaseValue(instance.getBaseValue() + increment);
        }

        decrementPoints(player);
        incrementUpgradeCount(player);

    }

    public static void resetAttributes(ServerPlayer player) {
        if (player.experienceLevel < 50 && player.gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
            player.sendSystemMessage(Component.translatable("gui.playerstats.cant_reset"));
            return;
        }

        CompoundTag upgrades = player.getPersistentData().getCompound(ATTRIBUTE_UPGRADES_TAG);
        int refundedPoints = 0;

        for (String key : upgrades.getAllKeys()) {
            ResourceLocation id = new ResourceLocation(key);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
            AttributeInstance instance = player.getAttribute(attr);
            if (attr != null && instance != null) {
                int upgradesApplied = upgrades.getInt(key);
                double increment = AttributeUtils.getIncrement(attr.getDescriptionId());
                instance.setBaseValue(instance.getBaseValue() - (upgradesApplied * increment));
                refundedPoints += upgradesApplied;
            }
        }

        player.getPersistentData().remove(ATTRIBUTE_UPGRADES_TAG);
        player.getPersistentData().remove(UPGRADE_COUNT_TAG);

        setPoints(player, getPoints(player) + refundedPoints);
        player.giveExperienceLevels(-50);

        player.sendSystemMessage(Component.translatable("gui.playerstats.reset", refundedPoints));
        PacketHandler.sendToClient(new UpdatePointsPacket(getPoints(player)), player);
        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(0), player);
    }

    public static void ensurePointsInitialized(Player player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(POINTS_TAG)) {
            setPoints(player, 0);
        }
    }

    public static int getPoints(Player player) {
        return player.getPersistentData().getInt(POINTS_TAG);
    }

    public static void setPoints(Player player, int points) {
        player.getPersistentData().putInt(POINTS_TAG, points);
    }

    public static void addPoints(Player player, int points) {
        CompoundTag tag = player.getPersistentData();
        int playerPoints = tag.getInt(POINTS_TAG);
        tag.putInt(POINTS_TAG, playerPoints + points);
    }

    public static void decrementPoints(Player player) {
        CompoundTag tag = player.getPersistentData();
        int current = tag.getInt(POINTS_TAG);
        tag.putInt(POINTS_TAG, Math.max(0, current - 1));
    }

    public static int getUpgradeCount(Player player) {
        return player.getPersistentData().getInt(UPGRADE_COUNT_TAG);
    }

    public static void incrementUpgradeCount(Player player) {
        CompoundTag tag = player.getPersistentData();
        int count = tag.getInt(UPGRADE_COUNT_TAG);
        tag.putInt(UPGRADE_COUNT_TAG, count + 1);
    }
}
