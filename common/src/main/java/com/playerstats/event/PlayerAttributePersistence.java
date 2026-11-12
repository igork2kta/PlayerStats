package com.playerstats.event;

import com.playerstats.Config;
import com.playerstats.PlayerStats;
import com.playerstats.network.PacketHandler;
import com.playerstats.network.SyncAbilityLevelPacket;
import com.playerstats.network.SyncEntityUpgradesPacket;
import com.playerstats.network.UpdatePointsPacket;
import com.playerstats.network.UpdateUpgradeCountPacket;
import com.playerstats.platform.Services;
import com.playerstats.util.AttributeUtils;
import com.playerstats.util.UniqueAbilitiesUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerAttributePersistence {

    private static final String ATTRIBUTE_UPGRADES_TAG = "PlayerStatsUpgrades";
    private static final String DISABLED_ATTRIBUTES_TAG = "PlayerStatsDisabledAttributes";
    private static final String POINTS_TAG = "PlayerStatsPoints";
    private static final String UPGRADE_COUNT_TAG = "PlayerStatsUpgradeCount";
    private static final String ABILITY_POINTS_TAG = "PlayerAbilityPoints";
    private static final String ABILITY_UPGRADES_TAG = "PlayerAbilityUpgrades";

    public static void handleClone(ServerPlayer original, ServerPlayer player, boolean wasDeath) {
        CompoundTag originalNBT = Services.PLATFORM.getPersistentData(original);
        CompoundTag root = Services.PLATFORM.getPersistentData(player);
        root.put(ATTRIBUTE_UPGRADES_TAG, originalNBT.getCompound(ATTRIBUTE_UPGRADES_TAG));
        root.put(DISABLED_ATTRIBUTES_TAG, originalNBT.getCompound(DISABLED_ATTRIBUTES_TAG));
        root.put(ABILITY_UPGRADES_TAG, originalNBT.getCompound(ABILITY_UPGRADES_TAG));

        if (wasDeath && Config.RESET_ON_DEATH.get()) {
            resetAttributes(player, player, true);
        } else {
            setPoints(player, getPoints(original));
            setAbilityPoints(player, getAbilityPoints(original));
            CompoundTag upgradesTag = root.getCompound(ATTRIBUTE_UPGRADES_TAG);

            for (String key : upgradesTag.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);
                Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
                AttributeInstance instance = AttributeUtils.getAttributeInstance(player, attr);

                if (attr != null && instance != null && !isAttributeDisabled(player, key)) {
                    int upgradeCount = upgradesTag.getInt(key);
                    double increment = AttributeUtils.getIncrement(instance);

                    PlayerStats.LOGGER.info("Setting attribute: {} current value: {} upgrade count: {} increment: {}", attr.getDescriptionId(), instance.getBaseValue(), upgradeCount, increment);

                    double totalIncrement = upgradeCount * increment;
                    applyModifier(instance, attr.getDescriptionId(), totalIncrement);
                }
            }

            CompoundTag abilityTag = root.getCompound(ABILITY_UPGRADES_TAG);
            for (String key : abilityTag.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);
                Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
                AttributeInstance instance = AttributeUtils.getAttributeInstance(player, attr);
                if (attr != null && instance != null) {
                    int savedLevel = abilityTag.getInt(key);
                    if (savedLevel > 0) {
                        applyModifier(instance, attr.getDescriptionId(), 1.0D + savedLevel);
                        PacketHandler.sendToClient(player, new SyncAbilityLevelPacket(key, savedLevel));
                    }
                }
            }
        }

        PacketHandler.sendToClient(player, new UpdateUpgradeCountPacket(getUpgradeCount(player)));
        syncEntityUpgrades(player, player);
    }

    public static void handleLogin(ServerPlayer player) {
        PacketHandler.sendToClient(player, new UpdatePointsPacket(getPoints(player), "attribute"));
        PacketHandler.sendToClient(player, new UpdatePointsPacket(getAbilityPoints(player), "ability"));
        PacketHandler.sendToClient(player, new UpdateUpgradeCountPacket(getUpgradeCount(player)));
        syncEntityUpgrades(player, player);

        CompoundTag abilityTag = Services.PLATFORM.getPersistentData(player).getCompound(ABILITY_UPGRADES_TAG);
        for (String key : abilityTag.getAllKeys()) {
            int savedLevel = abilityTag.getInt(key);
            if (savedLevel > 0) {
                PacketHandler.sendToClient(player, new SyncAbilityLevelPacket(key, savedLevel));
            }
        }
    }

    public static void handleRespawn(ServerPlayer player) {
        syncEntityUpgrades(player, player);
    }

    public static void upgradeAttribute(LivingEntity entity, ServerPlayer player, String attributeId) {
        ResourceLocation id = ResourceLocation.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);

        if (attr != null) {
            AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);

            if (!attributeId.startsWith("playerstats:")) {
                int playerPoints = getPoints(player);
                int playerUpgrades = getUpgradeCount(entity);
                int xpIncrement = Config.XP_COST_INCREMENT.get();
                int xpCost = (playerUpgrades + 1) * xpIncrement;

                boolean consumeXp = Config.CONSUME_XP.get();
                if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                    consumeXp = false;
                }

                if (instance == null) {
                    System.err.println("AttributeInstance is null for: " + id);
                    return;
                }

                if (playerPoints > 0 && (player.experienceLevel >= xpCost || !consumeXp)) {
                    CompoundTag upgrades = Services.PLATFORM.getPersistentData(entity).getCompound(ATTRIBUTE_UPGRADES_TAG);
                    int currentUpgrades = upgrades.getInt(attributeId);
                    double effectiveValue = isAttributeDisabled(entity, attributeId)
                            ? instance.getValue() + (currentUpgrades * AttributeUtils.getIncrement(instance))
                            : instance.getValue();

                    if (AttributeUtils.isMaxed(instance, effectiveValue)) {
                        player.sendSystemMessage(Component.translatable("gui.playerstats.attribute.max_value_reached", AttributeUtils.getAttributeName(attr)));
                        return;
                    }

                    applyUpgrade(entity, attr);
                    setPoints(player, playerPoints - 1);

                    int count = PlayerAttributePersistence.getUpgradeCount(entity);
                    PacketHandler.sendToClient(player, new UpdateUpgradeCountPacket(count));
                    syncEntityUpgrades(entity, player);

                    if (consumeXp) consumeExperience(player, xpCost);
                }
            } else {
                int playerPoints = getAbilityPoints(player);
                boolean isLeveled = UniqueAbilitiesUtils.isLeveledAbility(attributeId);
                int maxLevel = UniqueAbilitiesUtils.getMaxLevel(attributeId);

                if (isLeveled) {
                    double currentVal = instance.getValue();
                    int currentLevel = (int) Math.round(currentVal);
                    if (currentLevel < 0) {
                        currentLevel = 0;
                    }

                    if (currentLevel < maxLevel) {
                        int xpCost = Config.REQUIRED_XP_FOR_ABILITY.get();
                        boolean consumeXp = Config.CONSUME_XP.get();
                        if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                            consumeXp = false;
                        }

                        if (playerPoints > 0 && (player.experienceLevel >= xpCost || !consumeXp)) {
                            int nextLevel = currentLevel + 1;
                            if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, true)) return;

                            applyModifier(instance, attr.getDescriptionId(), 1.0D + nextLevel);
                            saveAbilityLevel(entity, attributeId, nextLevel);
                            PacketHandler.sendToClient(player, new SyncAbilityLevelPacket(attributeId, nextLevel));
                            setAbilityPoints(player, playerPoints - 1);
                            if (consumeXp) consumeExperience(player, xpCost);
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("gui.playerstats.attribute.max_value_reached", AttributeUtils.getAttributeName(attr)));
                    }
                } else {
                    if (instance.getValue() == -1.0D) {
                        int xpCost = Config.REQUIRED_XP_FOR_ABILITY.get();
                        boolean consumeXp = Config.CONSUME_XP.get();
                        if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                            consumeXp = false;
                        }

                        if (playerPoints > 0 && (player.experienceLevel >= xpCost || !consumeXp)) {
                            if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, true)) return;

                            applyModifier(instance, attr.getDescriptionId(), 2);
                            saveAbilityLevel(entity, attributeId, 1);
                            setAbilityPoints(player, playerPoints - 1);
                            if (consumeXp) consumeExperience(player, xpCost);
                        }
                    } else if (instance.getValue() == 0.0D) {
                        if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, true)) return;
                        applyModifier(instance, attr.getDescriptionId(), 2);
                        saveAbilityLevel(entity, attributeId, 1);
                    } else if (instance.getValue() == 1.0D) {
                        if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, false)) return;
                        applyModifier(instance, attr.getDescriptionId(), 1);
                        saveAbilityLevel(entity, attributeId, 0);
                    }
                }
            }
        } else {
            System.err.println("Unknown attribute ID: " + id);
        }
    }

    public static void toggleAbility(LivingEntity entity, ServerPlayer player, String attributeId) {
        ResourceLocation id = ResourceLocation.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
        if (attr == null) return;

        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
        if (instance == null) return;

        boolean isLeveled = UniqueAbilitiesUtils.isLeveledAbility(attributeId);
        if (isLeveled) {
            int savedLevel = getSavedAbilityLevel(entity, attributeId);
            if (savedLevel <= 0) return;

            double currentVal = instance.getValue();
            boolean isActive = currentVal > 0.0D;

            if (isActive) {
                if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, false)) return;
                applyModifier(instance, attr.getDescriptionId(), 1.0D);
            } else {
                if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, true)) return;
                applyModifier(instance, attr.getDescriptionId(), 1.0D + savedLevel);
            }
        } else {
            if (instance.getValue() == 0.0D) {
                if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, true)) return;
                applyModifier(instance, attr.getDescriptionId(), 2.0D);
                saveAbilityLevel(entity, attributeId, 1);
            } else if (instance.getValue() == 1.0D) {
                if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, false)) return;
                applyModifier(instance, attr.getDescriptionId(), 1.0D);
                saveAbilityLevel(entity, attributeId, 0);
            }
        }
    }

    public static void saveAbilityLevel(LivingEntity entity, String attributeId, int level) {
        CompoundTag abilityTag = Services.PLATFORM.getPersistentData(entity).getCompound(ABILITY_UPGRADES_TAG);
        abilityTag.putInt(attributeId, level);
        Services.PLATFORM.getPersistentData(entity).put(ABILITY_UPGRADES_TAG, abilityTag);
    }

    public static int getSavedAbilityLevel(LivingEntity entity, String attributeId) {
        CompoundTag abilityTag = Services.PLATFORM.getPersistentData(entity).getCompound(ABILITY_UPGRADES_TAG);
        return abilityTag.getInt(attributeId);
    }

    public static boolean isAttributeDisabled(LivingEntity entity, String attributeId) {
        CompoundTag disabled = Services.PLATFORM.getPersistentData(entity).getCompound(DISABLED_ATTRIBUTES_TAG);
        return disabled.getBoolean(attributeId);
    }

    public static void toggleAttributeUpgrade(LivingEntity entity, ServerPlayer player, String attributeId) {
        ResourceLocation id = ResourceLocation.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
        if (attr == null) return;

        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
        if (instance == null) return;

        CompoundTag upgrades = Services.PLATFORM.getPersistentData(entity).getCompound(ATTRIBUTE_UPGRADES_TAG);
        int upgradeCount = upgrades.getInt(attributeId);
        if (upgradeCount <= 0) return;

        CompoundTag disabled = Services.PLATFORM.getPersistentData(entity).getCompound(DISABLED_ATTRIBUTES_TAG);
        boolean currentlyDisabled = disabled.getBoolean(attributeId);

        if (currentlyDisabled) {
            disabled.remove(attributeId);
            if (disabled.isEmpty()) {
                Services.PLATFORM.getPersistentData(entity).remove(DISABLED_ATTRIBUTES_TAG);
            } else {
                Services.PLATFORM.getPersistentData(entity).put(DISABLED_ATTRIBUTES_TAG, disabled);
            }
            double increment = AttributeUtils.getIncrement(instance);
            applyModifier(instance, attr.getDescriptionId(), increment * upgradeCount);
        } else {
            disabled.putBoolean(attributeId, true);
            Services.PLATFORM.getPersistentData(entity).put(DISABLED_ATTRIBUTES_TAG, disabled);
            removePlayerStatsModifier(instance, attr.getDescriptionId());
            if (attr == Attributes.MAX_HEALTH && entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }

        syncEntityUpgrades(entity, player);
    }

    public static void syncEntityUpgrades(LivingEntity entity, ServerPlayer player) {
        if (entity == null || player == null) return;

        CompoundTag upgradesTag = Services.PLATFORM.getPersistentData(entity).getCompound(ATTRIBUTE_UPGRADES_TAG);
        Map<String, Integer> upgradesMap = new java.util.HashMap<>();
        for (String key : upgradesTag.getAllKeys()) {
            int val = upgradesTag.getInt(key);
            if (val > 0) {
                upgradesMap.put(key, val);
            }
        }

        CompoundTag disabledTag = Services.PLATFORM.getPersistentData(entity).getCompound(DISABLED_ATTRIBUTES_TAG);
        Set<String> disabledSet = new java.util.HashSet<>();
        for (String key : disabledTag.getAllKeys()) {
            if (disabledTag.getBoolean(key)) {
                disabledSet.add(key);
            }
        }

        PacketHandler.sendToClient(
                player,
                new SyncEntityUpgradesPacket(entity.getId(), upgradesMap, disabledSet)
        );
    }

    public static void applyUpgrade(LivingEntity entity, Attribute attr) {
        ResourceLocation key = BuiltInRegistries.ATTRIBUTE.getKey(attr);
        CompoundTag upgrades = Services.PLATFORM.getPersistentData(entity).getCompound(ATTRIBUTE_UPGRADES_TAG);

        int currentUpgrades = upgrades.getInt(key.toString()) + 1;
        upgrades.putInt(key.toString(), currentUpgrades);
        Services.PLATFORM.getPersistentData(entity).put(ATTRIBUTE_UPGRADES_TAG, upgrades);

        CompoundTag disabled = Services.PLATFORM.getPersistentData(entity).getCompound(DISABLED_ATTRIBUTES_TAG);
        if (disabled.contains(key.toString())) {
            disabled.remove(key.toString());
            if (disabled.isEmpty()) {
                Services.PLATFORM.getPersistentData(entity).remove(DISABLED_ATTRIBUTES_TAG);
            } else {
                Services.PLATFORM.getPersistentData(entity).put(DISABLED_ATTRIBUTES_TAG, disabled);
            }
        }

        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
        if (instance != null) {
            double increment = AttributeUtils.getIncrement(instance);
            applyModifier(instance, attr.getDescriptionId(), increment * currentUpgrades);
            incrementUpgradeCount(entity);
        }
    }

    public static void resetAttributes(LivingEntity entity, ServerPlayer player, boolean resetByDeath) {
        boolean consumeXp = Config.CONSUME_XP.get();
        int requiredXp = Config.REQUIRED_XP_FOR_RESET.get();
        if (player.experienceLevel < requiredXp && player.gameMode.getGameModeForPlayer() != GameType.CREATIVE && !resetByDeath && consumeXp) {
            player.sendSystemMessage(Component.translatable("gui.playerstats.cant_reset", requiredXp));
            return;
        }

        CompoundTag upgrades = Services.PLATFORM.getPersistentData(entity).getCompound(ATTRIBUTE_UPGRADES_TAG);
        int refundedPoints = 0;

        for (String key : upgrades.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
            AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
            if (attr != null && instance != null) {
                int upgradesApplied = upgrades.getInt(key);
                removePlayerStatsModifier(instance, attr.getDescriptionId());
                refundedPoints += upgradesApplied;
            }
        }

        Services.PLATFORM.getPersistentData(entity).remove(ATTRIBUTE_UPGRADES_TAG);
        Services.PLATFORM.getPersistentData(entity).remove(DISABLED_ATTRIBUTES_TAG);
        Services.PLATFORM.getPersistentData(entity).remove(UPGRADE_COUNT_TAG);

        if (!resetByDeath) {
            setPoints(player, getPoints(player) + refundedPoints);
            if (consumeXp) consumeExperience(player, requiredXp);
            player.sendSystemMessage(Component.translatable("gui.playerstats.reset", refundedPoints));
        }

        PacketHandler.sendToClient(player, new UpdateUpgradeCountPacket(0));
        syncEntityUpgrades(entity, player);
    }

    public static void consumeExperience(Player player, int level) {
        int points;
        if (level <= 16) {
            points = level * level + 6 * level;
        } else if (level <= 31) {
            points = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            points = (int) (4.5 * level * level - 162.5 * level + 2220);
        }
        player.giveExperiencePoints(points * -1);
    }

    public static void removePlayerStatsModifier(AttributeInstance instance, String attrId) {
        UUID modifierUuid = UUID.nameUUIDFromBytes(("playerstats:" + attrId).getBytes(StandardCharsets.UTF_8));
        instance.removeModifier(modifierUuid);
        instance.getModifiers().stream()
                .filter(mod -> ("playerstats:" + attrId).equals(mod.getName()))
                .toList()
                .forEach(instance::removeModifier);
    }

    public static void applyModifier(AttributeInstance instance, String attrId, double value) {
        removePlayerStatsModifier(instance, attrId);

        UUID modifierUuid = UUID.nameUUIDFromBytes(("playerstats:" + attrId).getBytes(StandardCharsets.UTF_8));
        AttributeModifier modifier = new AttributeModifier(
                modifierUuid,
                "playerstats:" + attrId,
                value,
                AttributeModifier.Operation.ADDITION
        );

        instance.addPermanentModifier(modifier);
    }

    public static int getPoints(Player player) {
        return Services.PLATFORM.getPersistentData(player).getInt(POINTS_TAG);
    }

    public static void setPoints(Player player, int points) {
        Services.PLATFORM.getPersistentData(player).putInt(POINTS_TAG, points);
        if (player instanceof ServerPlayer sp) {
            PacketHandler.sendToClient(sp, new UpdatePointsPacket(points, "attribute"));
        }
    }

    public static int getAbilityPoints(Player player) {
        return Services.PLATFORM.getPersistentData(player).getInt(ABILITY_POINTS_TAG);
    }

    public static void setAbilityPoints(Player player, int points) {
        Services.PLATFORM.getPersistentData(player).putInt(ABILITY_POINTS_TAG, points);
        if (player instanceof ServerPlayer sp) {
            PacketHandler.sendToClient(sp, new UpdatePointsPacket(points, "ability"));
        }
    }

    public static void addPoints(Player player, int points) {
        CompoundTag tag = Services.PLATFORM.getPersistentData(player);
        int playerPoints = tag.getInt(POINTS_TAG) + points;
        tag.putInt(POINTS_TAG, playerPoints);
        if (player instanceof ServerPlayer sp) {
            PacketHandler.sendToClient(sp, new UpdatePointsPacket(playerPoints, "attribute"));
        }
        player.sendSystemMessage(Component.translatable("event.playerstats.point_given", Component.keybind("key.playerstats.open_stats_screen")));
    }

    public static void addAbilityPoints(Player player, int points) {
        CompoundTag tag = Services.PLATFORM.getPersistentData(player);
        int playerPoints = tag.getInt(ABILITY_POINTS_TAG) + points;
        tag.putInt(ABILITY_POINTS_TAG, playerPoints);
        if (player instanceof ServerPlayer sp) {
            PacketHandler.sendToClient(sp, new UpdatePointsPacket(playerPoints, "ability"));
        }
        player.sendSystemMessage(Component.translatable("event.playerstats.ability_point_given", Component.keybind("key.playerstats.open_stats_screen")));
    }

    public static int getUpgradeCount(LivingEntity entity) {
        return Services.PLATFORM.getPersistentData(entity).getInt(UPGRADE_COUNT_TAG);
    }

    public static void incrementUpgradeCount(LivingEntity entity) {
        CompoundTag tag = Services.PLATFORM.getPersistentData(entity);
        int count = tag.getInt(UPGRADE_COUNT_TAG);
        tag.putInt(UPGRADE_COUNT_TAG, count + 1);
    }
}
