package com.playerstats.event;

import com.playerstats.Config;
import com.playerstats.PlayerStats;
import com.playerstats.client.ClientAttributeCache;
import com.playerstats.client.KeyMappings;
import com.playerstats.network.PacketHandler;
import com.playerstats.network.UpdatePointsPacket;
import com.playerstats.network.UpdateUpgradeCountPacket;
import com.playerstats.platform.Services;
import com.playerstats.util.AttributeUtils;
import com.playerstats.util.UniqueAbilitiesUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class PlayerAttributePersistence {

    private static final String ATTRIBUTE_UPGRADES_TAG = "PlayerStatsUpgrades";
    private static final String DISABLED_ATTRIBUTES_TAG = "PlayerStatsDisabledAttributes";
    private static final String ABILITY_UPGRADES_TAG = "PlayerStatsAbilityUpgrades";
    private static final String POINTS_TAG = "PlayerStatsPoints";
    private static final String UPGRADE_COUNT_TAG = "PlayerStatsUpgradeCount";
    private static final String ABILITY_POINTS_TAG = "PlayerAbilityPoints";

    private static CompoundTag getPersistentData(Entity entity) {
        return Services.PLATFORM.getPersistentData(entity);
    }

    public static void handleClone(Player original, Player player, boolean wasDeath) {
        CompoundTag originalNBT = getPersistentData(original);

        ClientAttributeCache.clean();
        CompoundTag root = getPersistentData(player);
        root.put(ATTRIBUTE_UPGRADES_TAG, originalNBT.getCompoundOrEmpty(ATTRIBUTE_UPGRADES_TAG));
        root.put(DISABLED_ATTRIBUTES_TAG, originalNBT.getCompoundOrEmpty(DISABLED_ATTRIBUTES_TAG));

        if (Config.RESET_ON_DEATH.get()) {
            resetAttributes(player, (ServerPlayer) player, true);
        } else {
            setPoints(player, getPoints(original));
            setAbilityPoints(player, getAbilityPoints(original));
            CompoundTag upgradesTag = root.getCompoundOrEmpty(ATTRIBUTE_UPGRADES_TAG);

            for (String key : upgradesTag.keySet()) {
                Identifier id = Identifier.tryParse(key);
                Attribute attr = BuiltInRegistries.ATTRIBUTE.getValue(id);
                AttributeInstance instance = AttributeUtils.getAttributeInstance(player, attr);

                if (attr != null && instance != null && !isAttributeDisabled(player, key)) {
                    int upgradeCount = upgradesTag.getIntOr(key, 0);
                    double increment = AttributeUtils.getIncrement(instance);

                    PlayerStats.LOGGER.info("Setting attribute: {} current value: {} upgrade count: {} increment: {}", attr.getDescriptionId(), instance.getBaseValue(), upgradeCount, increment);

                    double totalIncrement = upgradeCount * increment;
                    applyModifier(instance, attr.getDescriptionId(), totalIncrement);
                }
            }
        }

        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(getUpgradeCount(player)), (ServerPlayer) player);
        syncEntityUpgrades(player, (ServerPlayer) player);
    }

    public static void handleLogin(ServerPlayer player) {
        ClientAttributeCache.clean();
        PacketHandler.sendToClient(new UpdatePointsPacket(getPoints(player), "attribute"), player);
        PacketHandler.sendToClient(new UpdatePointsPacket(getAbilityPoints(player), "ability"), player);
        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(getUpgradeCount(player)), player);
        syncEntityUpgrades(player, player);
    }

    public static void handleRespawn(ServerPlayer player) {
        syncEntityUpgrades(player, player);
    }

    public static void upgradeAttribute(LivingEntity entity, ServerPlayer player, String attributeId) {
        Identifier id = Identifier.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.getValue(id);

        if (attr != null) {
            AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);

            if (!attributeId.startsWith("playerstats:")) {
                int playerPoints = getPoints(player);
                int playerUpgrades = getUpgradeCount(entity);
                int xpIncrement = Config.XP_COST_INCREMENT.get();

                int xpCost = (playerUpgrades + 1) * xpIncrement;

                boolean consumeXp = Config.CONSUME_XP.get();
                if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)
                    consumeXp = false;

                if (instance == null) {
                    System.err.println("AttributeInstance is null for: " + id);
                    return;
                }

                if (playerPoints > 0 && (player.experienceLevel >= xpCost || !consumeXp)) {
                    CompoundTag upgrades = getPersistentData(entity).getCompoundOrEmpty(ATTRIBUTE_UPGRADES_TAG);
                    int currentUpgrades = upgrades.getIntOr(attributeId, 0);
                    double effectiveValue = isAttributeDisabled(entity, attributeId)
                            ? instance.getValue() + (currentUpgrades * AttributeUtils.getIncrement(instance))
                            : instance.getValue();

                    if (AttributeUtils.isMaxed(instance, effectiveValue)) {
                        player.sendSystemMessage(Component.translatable("gui.playerstats.attribute.max_value_reached", AttributeUtils.getAttributeName(attr)));
                        return;
                    }

                    if (consumeXp) {
                        consumeExperience(player, xpCost);
                    }

                    setPoints(player, playerPoints - 1);
                    applyUpgrade(entity, attr);

                    PacketHandler.sendToClient(new UpdateUpgradeCountPacket(getUpgradeCount(entity)), player);
                } else if (playerPoints <= 0) {
                    player.sendSystemMessage(Component.translatable("gui.playerstats.no_points"));
                } else {
                    player.sendSystemMessage(Component.translatable("gui.playerstats.no_xp", xpCost));
                }
            } else {
                upgradeUniqueAbility(entity, player, attributeId);
            }
        }
    }

    public static void upgradeUniqueAbility(LivingEntity entity, ServerPlayer player, String attributeId) {
        Identifier id = Identifier.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.getValue(id);

        int playerAbilityPoints = getAbilityPoints(player);
        int requiredXp = Config.REQUIRED_XP_FOR_ABILITY.get();
        boolean consumeXp = Config.CONSUME_XP.get();

        if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)
            consumeXp = false;

        if (playerAbilityPoints <= 0) {
            player.sendSystemMessage(Component.translatable("gui.playerstats.no_ability_points"));
            return;
        }

        if (player.experienceLevel < requiredXp && consumeXp) {
            player.sendSystemMessage(Component.translatable("gui.playerstats.no_xp", requiredXp));
            return;
        }

        if (attr != null) {
            AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
            if (instance != null) {
                double currentValue = instance.getValue();

                if (UniqueAbilitiesUtils.isLeveledAbility(attributeId)) {
                    int currentLevel = (int) Math.round(currentValue);
                    int maxLevel = UniqueAbilitiesUtils.getMaxLevel(attributeId);

                    if (currentLevel >= maxLevel) {
                        player.sendSystemMessage(Component.translatable("gui.playerstats.ability_max_level"));
                        return;
                    }

                    int newLevel = currentLevel <= 0 ? 1 : currentLevel + 1;
                    instance.setBaseValue((double) newLevel);

                    saveAbilityLevel(entity, attributeId, newLevel);
                    PacketHandler.sendToClient(new com.playerstats.network.SyncAbilityLevelPacket(attributeId, newLevel), player);

                    if (consumeXp) {
                        consumeExperience(player, requiredXp);
                    }
                    setAbilityPoints(player, playerAbilityPoints - 1);

                    player.sendSystemMessage(Component.translatable("gui.playerstats.ability_upgraded_level",
                            Component.translatable("attribute." + attributeId.replace(":", ".")),
                            newLevel, maxLevel));
                    return;
                }

                if (currentValue < 1.0D) {
                    instance.setBaseValue(1.0D);

                    if (consumeXp) {
                        consumeExperience(player, requiredXp);
                    }
                    setAbilityPoints(player, playerAbilityPoints - 1);
                    player.sendSystemMessage(Component.translatable("gui.playerstats.ability_acquired",
                            Component.translatable("attribute." + attributeId.replace(":", "."))));
                } else {
                    player.sendSystemMessage(Component.translatable("gui.playerstats.ability_already_acquired"));
                }
            }
        }
    }

    public static void toggleAbility(LivingEntity entity, ServerPlayer player, String attributeId) {
        Identifier id = Identifier.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.getValue(id);

        if (attr == null) return;
        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
        if (instance == null) return;

        double currentValue = instance.getValue();

        if (UniqueAbilitiesUtils.isLeveledAbility(attributeId)) {
            int savedLevel = getSavedAbilityLevel(entity, attributeId);

            if (currentValue <= 0.0D) {
                int restoreLevel = savedLevel > 0 ? savedLevel : 1;
                instance.setBaseValue((double) restoreLevel);
                player.sendSystemMessage(Component.translatable("gui.playerstats.ability_activated",
                        Component.translatable("attribute." + attributeId.replace(":", "."))));
            } else {
                int activeLevel = (int) Math.round(currentValue);
                saveAbilityLevel(entity, attributeId, activeLevel);
                instance.setBaseValue(-1.0D);
                player.sendSystemMessage(Component.translatable("gui.playerstats.ability_deactivated",
                        Component.translatable("attribute." + attributeId.replace(":", "."))));
            }
            return;
        }

        if (currentValue == 1.0D) {
            instance.setBaseValue(-1.0D);
            player.sendSystemMessage(Component.translatable("gui.playerstats.ability_deactivated",
                    Component.translatable("attribute." + attributeId.replace(":", "."))));
        } else if (currentValue == -1.0D) {
            instance.setBaseValue(1.0D);
            player.sendSystemMessage(Component.translatable("gui.playerstats.ability_activated",
                    Component.translatable("attribute." + attributeId.replace(":", "."))));
        }
    }

    public static void saveAbilityLevel(LivingEntity entity, String attributeId, int level) {
        CompoundTag abilityTag = getPersistentData(entity).getCompoundOrEmpty(ABILITY_UPGRADES_TAG);
        abilityTag.putInt(attributeId, level);
        getPersistentData(entity).put(ABILITY_UPGRADES_TAG, abilityTag);
    }

    public static int getSavedAbilityLevel(LivingEntity entity, String attributeId) {
        CompoundTag abilityTag = getPersistentData(entity).getCompoundOrEmpty(ABILITY_UPGRADES_TAG);
        return abilityTag.getIntOr(attributeId, 0);
    }

    public static boolean isAttributeDisabled(LivingEntity entity, String attributeId) {
        CompoundTag disabled = getPersistentData(entity).getCompoundOrEmpty(DISABLED_ATTRIBUTES_TAG);
        return disabled.getBooleanOr(attributeId, false);
    }

    public static void toggleAttributeUpgrade(LivingEntity entity, ServerPlayer player, String attributeId) {
        Identifier id = Identifier.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.getValue(id);
        if (attr == null) return;

        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
        if (instance == null) return;

        CompoundTag upgrades = getPersistentData(entity).getCompoundOrEmpty(ATTRIBUTE_UPGRADES_TAG);
        int upgradeCount = upgrades.getIntOr(attributeId, 0);
        if (upgradeCount <= 0) return;

        CompoundTag disabled = getPersistentData(entity).getCompoundOrEmpty(DISABLED_ATTRIBUTES_TAG);
        boolean currentlyDisabled = disabled.getBooleanOr(attributeId, false);

        if (currentlyDisabled) {
            disabled.remove(attributeId);
            if (disabled.isEmpty()) {
                getPersistentData(entity).remove(DISABLED_ATTRIBUTES_TAG);
            } else {
                getPersistentData(entity).put(DISABLED_ATTRIBUTES_TAG, disabled);
            }
            double increment = AttributeUtils.getIncrement(instance);
            applyModifier(instance, attr.getDescriptionId(), increment * upgradeCount);
        } else {
            disabled.putBoolean(attributeId, true);
            getPersistentData(entity).put(DISABLED_ATTRIBUTES_TAG, disabled);
            removePlayerStatsModifier(instance, attr.getDescriptionId());
            if (attr == Attributes.MAX_HEALTH.value() && entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }

        syncEntityUpgrades(entity, player);
    }

    public static void syncEntityUpgrades(LivingEntity entity, ServerPlayer player) {
        if (entity == null || player == null) return;

        CompoundTag upgradesTag = getPersistentData(entity).getCompoundOrEmpty(ATTRIBUTE_UPGRADES_TAG);
        java.util.Map<String, Integer> upgradesMap = new java.util.HashMap<>();
        for (String key : upgradesTag.keySet()) {
            int val = upgradesTag.getIntOr(key, 0);
            if (val > 0) {
                upgradesMap.put(key, val);
            }
        }

        CompoundTag disabledTag = getPersistentData(entity).getCompoundOrEmpty(DISABLED_ATTRIBUTES_TAG);
        java.util.Set<String> disabledSet = new java.util.HashSet<>();
        for (String key : disabledTag.keySet()) {
            if (disabledTag.getBooleanOr(key, false)) {
                disabledSet.add(key);
            }
        }

        PacketHandler.sendToClient(
                new com.playerstats.network.SyncEntityUpgradesPacket(entity.getId(), upgradesMap, disabledSet),
                player
        );
    }

    public static void applyUpgrade(LivingEntity entity, Attribute attr) {
        Identifier key = BuiltInRegistries.ATTRIBUTE.getKey(attr);
        CompoundTag upgrades = getPersistentData(entity).getCompoundOrEmpty(ATTRIBUTE_UPGRADES_TAG);

        int currentUpgrades = upgrades.getIntOr(key.toString(), 0) + 1;
        upgrades.putInt(key.toString(), currentUpgrades);
        getPersistentData(entity).put(ATTRIBUTE_UPGRADES_TAG, upgrades);

        CompoundTag disabled = getPersistentData(entity).getCompoundOrEmpty(DISABLED_ATTRIBUTES_TAG);
        if (disabled.contains(key.toString())) {
            disabled.remove(key.toString());
            if (disabled.isEmpty()) {
                getPersistentData(entity).remove(DISABLED_ATTRIBUTES_TAG);
            } else {
                getPersistentData(entity).put(DISABLED_ATTRIBUTES_TAG, disabled);
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

        CompoundTag upgrades = getPersistentData(entity).getCompoundOrEmpty(ATTRIBUTE_UPGRADES_TAG);
        int refundedPoints = 0;

        for (String key : upgrades.keySet()) {
            Identifier id = Identifier.tryParse(key);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.getValue(id);
            AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
            if (attr != null && instance != null) {
                int upgradesApplied = upgrades.getIntOr(key, 0);
                removePlayerStatsModifier(instance, attr.getDescriptionId());
                refundedPoints += upgradesApplied;
            }
        }

        getPersistentData(entity).remove(ATTRIBUTE_UPGRADES_TAG);
        getPersistentData(entity).remove(DISABLED_ATTRIBUTES_TAG);
        getPersistentData(entity).remove(UPGRADE_COUNT_TAG);

        if (!resetByDeath) {
            setPoints(player, getPoints(player) + refundedPoints);
            if (consumeXp) consumeExperience(player, requiredXp);
            player.sendSystemMessage(Component.translatable("gui.playerstats.reset", refundedPoints));
        }

        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(0), player);
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
        String sanitizedPath = AttributeUtils.sanitizeToPath(attrId);
        instance.getModifiers().stream()
                .filter(mod -> mod.id().getNamespace().equals("playerstats")
                        && (mod.id().getPath().equals(attrId) || mod.id().getPath().equals(sanitizedPath)))
                .findFirst()
                .ifPresent(mod -> instance.removeModifier(mod.id()));
    }

    public static void applyModifier(AttributeInstance instance, String attrId, double value) {
        removePlayerStatsModifier(instance, attrId);

        String sanitizedPath = AttributeUtils.sanitizeToPath(attrId);
        Identifier modifierId = Identifier.fromNamespaceAndPath("playerstats", sanitizedPath);

        AttributeModifier modifier = new AttributeModifier(
                modifierId,
                value,
                AttributeModifier.Operation.ADD_VALUE
        );

        instance.addPermanentModifier(modifier);
    }

    public static int getPoints(Player player) {
        return getPersistentData(player).getIntOr(POINTS_TAG, 0);
    }

    public static void setPoints(Player player, int points) {
        getPersistentData(player).putInt(POINTS_TAG, points);
        PacketHandler.sendToClient(new UpdatePointsPacket(points, "attribute"), (ServerPlayer) player);
    }

    public static int getAbilityPoints(Player player) {
        return getPersistentData(player).getIntOr(ABILITY_POINTS_TAG, 0);
    }

    public static void setAbilityPoints(Player player, int points) {
        getPersistentData(player).putInt(ABILITY_POINTS_TAG, points);
        PacketHandler.sendToClient(new UpdatePointsPacket(points, "ability"), (ServerPlayer) player);
    }

    public static void addPoints(Player player, int points) {
        CompoundTag tag = getPersistentData(player);
        int playerPoints = tag.getIntOr(POINTS_TAG, 0) + points;
        tag.putInt(POINTS_TAG, playerPoints);
        PacketHandler.sendToClient(new UpdatePointsPacket(playerPoints, "attribute"), (ServerPlayer) player);
        if (Services.PLATFORM.isPhysicalClient())
            player.sendSystemMessage(Component.translatable("event.playerstats.ability_point_given", KeyMappings.OPEN_STATS_KEY.getTranslatedKeyMessage()));
    }

    public static void addAbilityPoints(Player player, int points) {
        CompoundTag tag = getPersistentData(player);
        int playerPoints = tag.getIntOr(ABILITY_POINTS_TAG, 0) + points;
        tag.putInt(ABILITY_POINTS_TAG, playerPoints);
        PacketHandler.sendToClient(new UpdatePointsPacket(playerPoints, "ability"), (ServerPlayer) player);
        if (Services.PLATFORM.isPhysicalClient())
            player.sendSystemMessage(Component.translatable("event.playerstats.ability_point_given", KeyMappings.OPEN_STATS_KEY.getTranslatedKeyMessage()));
    }

    public static int getUpgradeCount(LivingEntity entity) {
        return getPersistentData(entity).getIntOr(UPGRADE_COUNT_TAG, 0);
    }

    public static void incrementUpgradeCount(LivingEntity entity) {
        CompoundTag tag = getPersistentData(entity);
        int count = tag.getIntOr(UPGRADE_COUNT_TAG, 0);
        tag.putInt(UPGRADE_COUNT_TAG, count + 1);
    }
}
