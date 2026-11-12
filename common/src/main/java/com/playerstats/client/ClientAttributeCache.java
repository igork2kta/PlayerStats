package com.playerstats.client;

public class ClientAttributeCache {
    public static boolean pointsInitialized = false;
    private static int points;
    private static int upgradeCount;
    private static int abilityPoints;
    private static final java.util.Map<String, Integer> abilityLevels = new java.util.HashMap<>();
    private static final java.util.Map<Integer, java.util.Map<String, Integer>> entityUpgrades = new java.util.HashMap<>();
    private static final java.util.Map<Integer, java.util.Set<String>> entityDisabledAttributes = new java.util.HashMap<>();

    public static void clean(){
       pointsInitialized = false;
       points = 0;
       upgradeCount = 0;
       abilityPoints = 0;
       abilityLevels.clear();
       entityUpgrades.clear();
       entityDisabledAttributes.clear();
    }

    public static void setEntityUpgrades(int entityId, java.util.Map<String, Integer> upgrades, java.util.Set<String> disabled) {
        entityUpgrades.put(entityId, new java.util.HashMap<>(upgrades));
        entityDisabledAttributes.put(entityId, new java.util.HashSet<>(disabled));
    }

    public static int getEntityUpgradeCount(int entityId, String attributeId) {
        java.util.Map<String, Integer> map = entityUpgrades.get(entityId);
        return map != null ? map.getOrDefault(attributeId, 0) : 0;
    }

    public static boolean isAttributeDisabled(int entityId, String attributeId) {
        java.util.Set<String> set = entityDisabledAttributes.get(entityId);
        return set != null && set.contains(attributeId);
    }

    public static void setAbilityLevel(String attributeId, int level) {
        abilityLevels.put(attributeId, level);
    }

    public static int getAbilityLevel(String attributeId) {
        return abilityLevels.getOrDefault(attributeId, 0);
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

    public static void setUpgradeCount(int count) {
        pointsInitialized = true;
        upgradeCount = count;
    }

    public static int getUpgradeCount() {
        return upgradeCount;
    }

}
