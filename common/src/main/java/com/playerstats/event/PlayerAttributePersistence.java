package com.playerstats.event;

import com.playerstats.Config;
import com.playerstats.PlayerStats;
import com.playerstats.client.ClientAttributeCache;
import com.playerstats.client.KeyMappings;
import com.playerstats.network.PacketHandler;
import com.playerstats.network.UpdatePointsPacket;
import com.playerstats.network.UpdateUpgradeCountPacket;
import com.playerstats.util.AttributeUtils;
import com.playerstats.util.UniqueAbilitiesUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import com.playerstats.platform.Services;
import net.minecraft.world.entity.Entity;


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
        root.put(ATTRIBUTE_UPGRADES_TAG, originalNBT.getCompound(ATTRIBUTE_UPGRADES_TAG));
        root.put(DISABLED_ATTRIBUTES_TAG, originalNBT.getCompound(DISABLED_ATTRIBUTES_TAG));
        root.put(ABILITY_UPGRADES_TAG, originalNBT.getCompound(ABILITY_UPGRADES_TAG));

        if (Config.RESET_ON_DEATH.get()) {
            resetAttributes(player, (ServerPlayer) player, true);
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

            CompoundTag abilitiesTag = root.getCompound(ABILITY_UPGRADES_TAG);
            for (String key : abilitiesTag.getAllKeys()) {
                int level = abilitiesTag.getInt(key);
                if (level > 0) {
                    ResourceLocation id = ResourceLocation.tryParse(key);
                    Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
                    if (attr != null) {
                        AttributeInstance oldInst = AttributeUtils.getAttributeInstance(original, attr);
                        AttributeInstance instance = AttributeUtils.getAttributeInstance(player, attr);
                        if (instance != null) {
                            if (instance.getBaseValue() != -1.0D) {
                                instance.setBaseValue(-1.0D);
                            }
                            boolean wasDisabled = (oldInst != null && (int) Math.round(oldInst.getValue()) == 0);
                            if (wasDisabled) {
                                applyModifier(instance, attr.getDescriptionId(), 1.0D);
                            } else {
                                if (UniqueAbilitiesUtils.isLeveledAbility(key)) {
                                    applyModifier(instance, attr.getDescriptionId(), 1.0D + level);
                                } else {
                                    applyModifier(instance, attr.getDescriptionId(), 2.0D);
                                }
                            }
                        }
                    }
                }
            }
        }

        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(player.getId(), getUpgradeCount(player)), (ServerPlayer) player);
        syncEntityUpgrades(player, (ServerPlayer) player);
    }

    public static void handleLogin(ServerPlayer player) {
        ClientAttributeCache.clean();
        PacketHandler.sendToClient(new UpdatePointsPacket(getPoints(player), "attribute"), player);
        PacketHandler.sendToClient(new UpdatePointsPacket(getAbilityPoints(player), "ability"), player);
        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(player.getId(), getUpgradeCount(player)), player);
        syncEntityUpgrades(player, player);
    }

    public static void handleRespawn(ServerPlayer player) {
        syncEntityUpgrades(player, player);
    }

    public static void upgradeAttribute(LivingEntity entity, ServerPlayer player, String attributeId){

        ResourceLocation id = ResourceLocation.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);

        if (attr != null) {

            AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);

            if(!attributeId.startsWith("playerstats:")) {
                int playerPoints = getPoints(player);
                int playerUpgrades = getUpgradeCount(entity);
                int xpIncrement = Config.XP_COST_INCREMENT.get();

                int xpCost = (playerUpgrades + 1) * xpIncrement; //Starts with 5 and increment by 5

                boolean consumeXp = Config.CONSUME_XP.get();
                if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)
                    consumeXp = false;//Creative mod doesn't need XP

                if(instance == null) {
                    System.err.println("AttributeInstance is null for: " + id);
                    return;
                }

                if (playerPoints > 0 && (player.experienceLevel >= xpCost || !consumeXp)) {
                    CompoundTag upgrades = getPersistentData(entity).getCompound(ATTRIBUTE_UPGRADES_TAG);
                    int currentUpgrades = upgrades.getInt(attributeId);
                    double effectiveValue = isAttributeDisabled(entity, attributeId)
                            ? instance.getValue() + (currentUpgrades * AttributeUtils.getIncrement(instance))
                            : instance.getValue();

                    if (AttributeUtils.isMaxed(instance, effectiveValue)) {
                        player.sendSystemMessage(Component.translatable("gui.playerstats.attribute.max_value_reached", AttributeUtils.getAttributeName(attr)));
                        return;
                    }


                    // Ao aplicar o upgrade
                    applyUpgrade(entity, attr);
                    setPoints(player, playerPoints - 1);

                    int count = PlayerAttributePersistence.getUpgradeCount(entity);
                    PacketHandler.sendToClient(new UpdateUpgradeCountPacket(entity.getId(), count), player);
                    syncEntityUpgrades(entity, player);

                    if (consumeXp) consumeExperience(player, xpCost);
                } else if (playerPoints <= 0) {
                    player.sendSystemMessage(Component.translatable("gui.playerstats.no_points"));
                } else {
                    player.sendSystemMessage(Component.translatable("gui.playerstats.no_xp", xpCost));
                }
            } else {
                upgradeUniqueAbility(entity, player, attributeId);
            }
        }
        else {
            System.err.println("Unknown attribute ID: " + id);
        }

    }

    public static void upgradeUniqueAbility(LivingEntity entity, ServerPlayer player, String attributeId) {
        ResourceLocation id = ResourceLocation.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);

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
                if (UniqueAbilitiesUtils.isLeveledAbility(attributeId)) {
                    double currentVal = instance.getValue();
                    int currentLevel = (int) Math.round(currentVal);
                    if (currentLevel < 0) {
                        currentLevel = 0;
                    }
                    int maxLevel = UniqueAbilitiesUtils.getMaxLevel(attributeId);

                    if (currentLevel >= maxLevel) {
                        player.sendSystemMessage(Component.translatable("gui.playerstats.ability_max_level"));
                        return;
                    }

                    int nextLevel = currentLevel + 1;
                    if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, true)) return;

                    applyModifier(instance, attr.getDescriptionId(), 1.0D + nextLevel);
                    saveAbilityLevel(entity, attributeId, nextLevel);
                    PacketHandler.sendToClient(new com.playerstats.network.SyncAbilityLevelPacket(entity.getId(), attributeId, nextLevel), player);

                    if (consumeXp) {
                        consumeExperience(player, requiredXp);
                    }
                    setAbilityPoints(player, playerAbilityPoints - 1);

                    player.sendSystemMessage(Component.translatable("gui.playerstats.ability_upgraded_level",
                            Component.translatable("attribute." + attributeId.replace(":", ".")),
                            nextLevel, maxLevel));
                    return;
                }

                if (instance.getBaseValue() != -1.0D) {
                    instance.setBaseValue(-1.0D);
                }
                int val = (int) Math.round(instance.getValue());

                // Não possui, comprando
                if (val <= -1) {
                    if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, true)) return;

                    applyModifier(instance, attr.getDescriptionId(), 2.0D);
                    saveAbilityLevel(entity, attributeId, 1);
                    PacketHandler.sendToClient(new com.playerstats.network.SyncAbilityLevelPacket(entity.getId(), attributeId, 1), player);

                    if (consumeXp) {
                        consumeExperience(player, requiredXp);
                    }
                    setAbilityPoints(player, playerAbilityPoints - 1);
                    player.sendSystemMessage(Component.translatable("gui.playerstats.ability_acquired",
                            Component.translatable("attribute." + attributeId.replace(":", "."))));
                }
                // Possui, ativando
                else if (val == 0) {
                    if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, true)) return;
                    applyModifier(instance, attr.getDescriptionId(), 2.0D);
                    saveAbilityLevel(entity, attributeId, 1);
                    PacketHandler.sendToClient(new com.playerstats.network.SyncAbilityLevelPacket(entity.getId(), attributeId, 1), player);
                    player.sendSystemMessage(Component.translatable("gui.playerstats.ability_activated",
                            Component.translatable("attribute." + attributeId.replace(":", "."))));
                } else {
                    player.sendSystemMessage(Component.translatable("gui.playerstats.ability_already_acquired"));
                }
            }
        }
    }

    public static void toggleAbility(LivingEntity entity, ServerPlayer player, String attributeId) {
        ResourceLocation id = ResourceLocation.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
        if (attr == null) return;

        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
        if (instance == null) return;

        if (instance.getBaseValue() != -1.0D) {
            instance.setBaseValue(-1.0D);
        }

        boolean isLeveled = UniqueAbilitiesUtils.isLeveledAbility(attributeId);
        if (isLeveled) {
            int savedLevel = getSavedAbilityLevel(entity, attributeId);
            if (savedLevel <= 0) return; // ainda não desbloqueou nível algum

            double currentVal = instance.getValue();
            boolean isActive = currentVal > 0.0D;

            if (isActive) {
                // Desativa: modifier = 1.0D (-1.0D base + 1.0D = 0.0D)
                if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, false)) return;
                applyModifier(instance, attr.getDescriptionId(), 1.0D);
                PacketHandler.sendToClient(new com.playerstats.network.SyncAbilityLevelPacket(entity.getId(), attributeId, 0), player);
                player.sendSystemMessage(Component.translatable("gui.playerstats.ability_deactivated",
                        Component.translatable("attribute." + attributeId.replace(":", "."))));
            } else {
                // Reativa: modifier = 1.0D + savedLevel (-1.0D base + 1.0D + savedLevel = savedLevel)
                if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, true)) return;
                applyModifier(instance, attr.getDescriptionId(), 1.0D + savedLevel);
                PacketHandler.sendToClient(new com.playerstats.network.SyncAbilityLevelPacket(entity.getId(), attributeId, savedLevel), player);
                player.sendSystemMessage(Component.translatable("gui.playerstats.ability_activated",
                        Component.translatable("attribute." + attributeId.replace(":", "."))));
            }
            return;
        }

        // Habilidade binária padrão
        int val = (int) Math.round(instance.getValue());
        if (val == 0) {
            if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, true)) return;
            applyModifier(instance, attr.getDescriptionId(), 2.0D);
            saveAbilityLevel(entity, attributeId, 1);
            PacketHandler.sendToClient(new com.playerstats.network.SyncAbilityLevelPacket(entity.getId(), attributeId, 1), player);
            player.sendSystemMessage(Component.translatable("gui.playerstats.ability_activated",
                    Component.translatable("attribute." + attributeId.replace(":", "."))));
        } else if (val == 1) {
            if (!UniqueAbilitiesUtils.validateAbility(entity, player, attributeId, false)) return;
            applyModifier(instance, attr.getDescriptionId(), 1.0D);
            saveAbilityLevel(entity, attributeId, 1);
            PacketHandler.sendToClient(new com.playerstats.network.SyncAbilityLevelPacket(entity.getId(), attributeId, 0), player);
            player.sendSystemMessage(Component.translatable("gui.playerstats.ability_deactivated",
                    Component.translatable("attribute." + attributeId.replace(":", "."))));
        }
    }

    public static void saveAbilityLevel(LivingEntity entity, String attributeId, int level) {
        CompoundTag abilityTag = getPersistentData(entity).getCompound(ABILITY_UPGRADES_TAG);
        abilityTag.putInt(attributeId, level);
        getPersistentData(entity).put(ABILITY_UPGRADES_TAG, abilityTag);
    }

    public static int getSavedAbilityLevel(LivingEntity entity, String attributeId) {
        CompoundTag abilityTag = getPersistentData(entity).getCompound(ABILITY_UPGRADES_TAG);
        return abilityTag.getInt(attributeId);
    }

    public static boolean isAttributeDisabled(LivingEntity entity, String attributeId) {
        CompoundTag disabled = getPersistentData(entity).getCompound(DISABLED_ATTRIBUTES_TAG);
        return disabled.getBoolean(attributeId);
    }

    public static void toggleAttributeUpgrade(LivingEntity entity, ServerPlayer player, String attributeId) {
        ResourceLocation id = ResourceLocation.tryParse(attributeId);
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
        if (attr == null) return;

        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity, attr);
        if (instance == null) return;

        CompoundTag upgrades = getPersistentData(entity).getCompound(ATTRIBUTE_UPGRADES_TAG);
        int upgradeCount = upgrades.getInt(attributeId);
        if (upgradeCount <= 0) return;

        CompoundTag disabled = getPersistentData(entity).getCompound(DISABLED_ATTRIBUTES_TAG);
        boolean currentlyDisabled = disabled.getBoolean(attributeId);

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

        CompoundTag upgradesTag = getPersistentData(entity).getCompound(ATTRIBUTE_UPGRADES_TAG);
        java.util.Map<String, Integer> upgradesMap = new java.util.HashMap<>();
        for (String key : upgradesTag.getAllKeys()) {
            int val = upgradesTag.getInt(key);
            if (val > 0) {
                upgradesMap.put(key, val);
            }
        }

        CompoundTag disabledTag = getPersistentData(entity).getCompound(DISABLED_ATTRIBUTES_TAG);
        java.util.Set<String> disabledSet = new java.util.HashSet<>();
        for (String key : disabledTag.getAllKeys()) {
            if (disabledTag.getBoolean(key)) {
                disabledSet.add(key);
            }
        }

        PacketHandler.sendToClient(
                new com.playerstats.network.SyncEntityUpgradesPacket(entity.getId(), getUpgradeCount(entity), upgradesMap, disabledSet),
                player
        );

        CompoundTag abilityTag = getPersistentData(entity).getCompound(ABILITY_UPGRADES_TAG);
        for (String key : abilityTag.getAllKeys()) {
            int savedLevel = abilityTag.getInt(key);
            ResourceLocation id = ResourceLocation.tryParse(key);
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
            int levelToSync = savedLevel;
            if (attr != null) {
                AttributeInstance inst = AttributeUtils.getAttributeInstance(entity, attr);
                if (inst != null && inst.getValue() == 0.0D) {
                    levelToSync = 0;
                }
            }
            PacketHandler.sendToClient(
                    new com.playerstats.network.SyncAbilityLevelPacket(entity.getId(), key, levelToSync),
                    player
            );
        }
    }

    public static void applyUpgrade(LivingEntity entity, Attribute attr) {
        ResourceLocation key = BuiltInRegistries.ATTRIBUTE.getKey(attr);
        CompoundTag upgrades = getPersistentData(entity).getCompound(ATTRIBUTE_UPGRADES_TAG);

        int currentUpgrades = upgrades.getInt(key.toString()) +1;
        upgrades.putInt(key.toString(), currentUpgrades);
        getPersistentData(entity).put(ATTRIBUTE_UPGRADES_TAG, upgrades);

        // Reativa o atributo caso estivesse desativado ao receber um novo upgrade
        CompoundTag disabled = getPersistentData(entity).getCompound(DISABLED_ATTRIBUTES_TAG);
        if (disabled.contains(key.toString())) {
            disabled.remove(key.toString());
            if (disabled.isEmpty()) {
                getPersistentData(entity).remove(DISABLED_ATTRIBUTES_TAG);
            } else {
                getPersistentData(entity).put(DISABLED_ATTRIBUTES_TAG, disabled);
            }
        }

        AttributeInstance instance = AttributeUtils.getAttributeInstance(entity,attr);
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

        CompoundTag upgrades = getPersistentData(entity).getCompound(ATTRIBUTE_UPGRADES_TAG);
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


        getPersistentData(entity).remove(ATTRIBUTE_UPGRADES_TAG);
        getPersistentData(entity).remove(DISABLED_ATTRIBUTES_TAG);
        getPersistentData(entity).remove(UPGRADE_COUNT_TAG);

        if(!resetByDeath) {

            setPoints(player, getPoints(player) + refundedPoints);

            if(consumeXp) consumeExperience(player, requiredXp);
            player.sendSystemMessage(Component.translatable("gui.playerstats.reset", refundedPoints));
        }

        PacketHandler.sendToClient(new UpdateUpgradeCountPacket(entity.getId(), 0), player);
        syncEntityUpgrades(entity, player);

    }

    public static void consumeExperience(Player player, int level){
        int points;
        if (level <= 16) {
            points = level * level + 6 * level;
        } else if (level <= 31) {
            points = (int)(2.5 * level * level - 40.5 * level + 360);
        } else {
            points = (int)(4.5 * level * level - 162.5 * level + 2220);
        }
        player.giveExperiencePoints(points * -1);
    }

    public static void removePlayerStatsModifier(AttributeInstance instance, String attrId) {
        String sanitizedPath = AttributeUtils.sanitizeToPath(attrId);
        instance.getModifiers().stream()
                .filter(mod -> mod.id().getNamespace().equals("playerstats")
                        && (mod.id().getPath().equals(attrId) || mod.id().getPath().equals(sanitizedPath)))
                .toList()
                .forEach(instance::removeModifier);
    }

    public static void applyModifier(AttributeInstance instance, String attrId, double value) {
        String sanitizedPath = AttributeUtils.sanitizeToPath(attrId);

        // Remover qualquer modificador antigo com o mesmo nome (suporta legados e sanitizados)
        removePlayerStatsModifier(instance, attrId);

        // Cria um ResourceLocation unico e valido para esse atributo
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath("playerstats", sanitizedPath);

        AttributeModifier modifier = new AttributeModifier(
                modifierId,
                value,
                AttributeModifier.Operation.ADD_VALUE
        );

        instance.addPermanentModifier(modifier);
    }

    public static int getPoints(Player player) {
        return getPersistentData(player).getInt(POINTS_TAG);
    }

    public static void setPoints(Player player, int points) {
        getPersistentData(player).putInt(POINTS_TAG, points);
        PacketHandler.sendToClient(new UpdatePointsPacket(points, "attribute"), (ServerPlayer) player);
    }

    public static int getAbilityPoints(Player player) {
        return getPersistentData(player).getInt(ABILITY_POINTS_TAG);
    }

    public static void setAbilityPoints(Player player, int points) {
        getPersistentData(player).putInt(ABILITY_POINTS_TAG, points);
        PacketHandler.sendToClient(new UpdatePointsPacket(points, "ability"), (ServerPlayer) player);
    }

    public static void addPoints(Player player, int points) {
        CompoundTag tag = getPersistentData(player);
        int playerPoints = tag.getInt(POINTS_TAG) + points;
        tag.putInt(POINTS_TAG, playerPoints);
        PacketHandler.sendToClient(new UpdatePointsPacket(playerPoints, "attribute"), (ServerPlayer) player);
        player.sendSystemMessage(Component.translatable("event.playerstats.ability_point_given", Component.keybind("key.playerstats.open_stats_screen")));
    }

    public static void addAbilityPoints(Player player, int points) {
        CompoundTag tag = getPersistentData(player);
        int playerPoints = tag.getInt(ABILITY_POINTS_TAG) + points;
        tag.putInt(ABILITY_POINTS_TAG, playerPoints);
        PacketHandler.sendToClient(new UpdatePointsPacket(playerPoints, "ability"), (ServerPlayer) player);
        player.sendSystemMessage(Component.translatable("event.playerstats.ability_point_given", Component.keybind("key.playerstats.open_stats_screen")));
    }

    public static int getUpgradeCount(LivingEntity entity) {
        return getPersistentData(entity).getInt(UPGRADE_COUNT_TAG);
    }

    public static void incrementUpgradeCount(LivingEntity entity) {
        CompoundTag tag = getPersistentData(entity);
        int count = tag.getInt(UPGRADE_COUNT_TAG);
        tag.putInt(UPGRADE_COUNT_TAG, count + 1);
    }
}