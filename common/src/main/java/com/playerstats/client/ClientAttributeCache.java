package com.playerstats.client;

import net.minecraft.client.Minecraft;

public class ClientAttributeCache {
    public static boolean pointsInitialized = false;
    private static int points;
    private static int upgradeCount;
    private static int abilityPoints;
    private static final java.util.Map<String, Integer> abilityLevels = new java.util.HashMap<>();
    private static final java.util.Map<Integer, java.util.Map<String, Integer>> entityAbilityLevels = new java.util.HashMap<>();
    private static final java.util.Map<Integer, java.util.Set<String>> entityDisabledAbilities = new java.util.HashMap<>();
    private static final java.util.Set<String> disabledAbilities = new java.util.HashSet<>();
    private static final java.util.Map<Integer, Integer> entityUpgradeCounts = new java.util.HashMap<>();
    private static final java.util.Map<Integer, java.util.Map<String, Integer>> entityUpgrades = new java.util.HashMap<>();
    private static final java.util.Map<Integer, java.util.Set<String>> entityDisabledAttributes = new java.util.HashMap<>();

    public static void clean() {
        pointsInitialized = false;
        points = 0;
        upgradeCount = 0;
        abilityPoints = 0;
        abilityLevels.clear();
        entityAbilityLevels.clear();
        entityDisabledAbilities.clear();
        disabledAbilities.clear();
        entityUpgradeCounts.clear();
        entityUpgrades.clear();
        entityDisabledAttributes.clear();
    }

    public static void setEntityUpgrades(int entityId, int upgradeCount, java.util.Map<String, Integer> upgrades, java.util.Set<String> disabled) {
        entityUpgradeCounts.put(entityId, upgradeCount);
        entityUpgrades.put(entityId, new java.util.HashMap<>(upgrades));
        entityDisabledAttributes.put(entityId, new java.util.HashSet<>(disabled));
    }

    public static void setEntityUpgrades(int entityId, java.util.Map<String, Integer> upgrades, java.util.Set<String> disabled) {
        setEntityUpgrades(entityId, entityUpgradeCounts.getOrDefault(entityId, 0), upgrades, disabled);
    }

    public static int getEntityUpgradeCount(int entityId, String attributeId) {
        java.util.Map<String, Integer> map = entityUpgrades.get(entityId);
        return map != null ? map.getOrDefault(attributeId, 0) : 0;
    }

    public static boolean isAttributeDisabled(int entityId, String attributeId) {
        java.util.Set<String> set = entityDisabledAttributes.get(entityId);
        return set != null && set.contains(attributeId);
    }

    public static boolean hasEntitySynced(int entityId) {
        return entityUpgrades.containsKey(entityId);
    }

    public static void setAbilityDisabled(int entityId, String attributeId, boolean disabled) {
        java.util.Set<String> set = entityDisabledAbilities.computeIfAbsent(entityId, k -> new java.util.HashSet<>());
        String descKey = "attribute." + attributeId.replace(':', '.');
        if (disabled) {
            set.add(attributeId);
            set.add(descKey);
        } else {
            set.remove(attributeId);
            set.remove(descKey);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == entityId) {
            if (disabled) {
                disabledAbilities.add(attributeId);
                disabledAbilities.add(descKey);
            } else {
                disabledAbilities.remove(attributeId);
                disabledAbilities.remove(descKey);
            }
        }
    }

    public static boolean isAbilityDisabled(int entityId, String key) {
        if (key == null) return false;
        java.util.Set<String> set = entityDisabledAbilities.get(entityId);
        String descKey = "attribute." + key.replace(':', '.');
        if (set != null && (set.contains(key) || set.contains(descKey))) {
            return true;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == entityId) {
            return isAbilityDisabled(key);
        }
        return false;
    }

    public static boolean isAbilityDisabled(String key) {
        if (key == null) return false;
        if (disabledAbilities.contains(key)) return true;
        String asDesc = "attribute." + key.replace(':', '.');
        return disabledAbilities.contains(asDesc);
    }

    public static void setAbilityLevel(int entityId, String attributeId, int level) {
        String descKey = "attribute." + attributeId.replace(':', '.');
        if (level > 0) {
            setAbilityDisabled(entityId, attributeId, false);
            java.util.Map<String, Integer> map = entityAbilityLevels.computeIfAbsent(entityId, k -> new java.util.HashMap<>());
            map.put(attributeId, level);
            map.put(descKey, level);

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getId() == entityId) {
                abilityLevels.put(attributeId, level);
                abilityLevels.put(descKey, level);
            }
        } else if (level == 0) {
            setAbilityDisabled(entityId, attributeId, true);
        } else {
            setAbilityDisabled(entityId, attributeId, false);
            java.util.Map<String, Integer> map = entityAbilityLevels.get(entityId);
            if (map != null) {
                map.remove(attributeId);
                map.remove(descKey);
            }
            abilityLevels.remove(attributeId);
            abilityLevels.remove(descKey);
        }
    }

    public static void setAbilityLevel(String attributeId, int level) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            setAbilityLevel(mc.player.getId(), attributeId, level);
        } else {
            String descKey = "attribute." + attributeId.replace(':', '.');
            if (level > 0) {
                setAbilityDisabled(-1, attributeId, false);
                abilityLevels.put(attributeId, level);
                abilityLevels.put(descKey, level);
            } else if (level == 0) {
                setAbilityDisabled(-1, attributeId, true);
            } else {
                setAbilityDisabled(-1, attributeId, false);
                abilityLevels.remove(attributeId);
                abilityLevels.remove(descKey);
            }
        }
    }

    public static int getAbilityLevel(int entityId, String key) {
        if (key == null) return 0;
        java.util.Map<String, Integer> map = entityAbilityLevels.get(entityId);
        if (map != null) {
            Integer val = map.get(key);
            if (val != null) return val;
            if (key.startsWith("attribute.")) {
                String converted = key.substring("attribute.".length()).replaceFirst("\\.", ":");
                val = map.get(converted);
                if (val != null) return val;
            }
            String asDesc = "attribute." + key.replace(':', '.');
            if (map.containsKey(asDesc)) return map.get(asDesc);
        }
        return getAbilityLevel(key);
    }

    public static int getAbilityLevel(String key) {
        if (key == null) return 0;
        Integer val = abilityLevels.get(key);
        if (val != null) return val;
        if (key.startsWith("attribute.")) {
            String converted = key.substring("attribute.".length()).replaceFirst("\\.", ":");
            val = abilityLevels.get(converted);
            if (val != null) return val;
        }
        String asDesc = "attribute." + key.replace(':', '.');
        return abilityLevels.getOrDefault(asDesc, 0);
    }

    public static void setPoints(int pts) {
        pointsInitialized = true;
        points = pts;
    }

    public static void setAbilityPoints(int pts) {
        pointsInitialized = true;
        abilityPoints = pts;
    }

    public static int getPoints() {
        return points;
    }

    public static int getAbilityPoints() {
        return abilityPoints;
    }

    public static void setUpgradeCount(int entityId, int count) {
        pointsInitialized = true;
        entityUpgradeCounts.put(entityId, count);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == entityId) {
            upgradeCount = count;
        }
    }

    public static int getUpgradeCount(int entityId) {
        return entityUpgradeCounts.getOrDefault(entityId, 0);
    }

    public static void setUpgradeCount(int count) {
        pointsInitialized = true;
        upgradeCount = count;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            entityUpgradeCounts.put(mc.player.getId(), count);
        }
    }

    public static int getUpgradeCount() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && entityUpgradeCounts.containsKey(mc.player.getId())) {
            return entityUpgradeCounts.get(mc.player.getId());
        }
        return upgradeCount;
    }
}
