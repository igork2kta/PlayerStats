package com.playerstats.client;

public class ClientAttributeCache {
    public static boolean pointsInitialized = false;
    private static int points;
    private static int upgradeCount;
    private static int abilityPoints;
    private static final java.util.Map<String, Integer> abilityLevels = new java.util.HashMap<>();

    public static void clean(){
       pointsInitialized = false;
       points = 0;
       upgradeCount = 0;
       abilityPoints = 0;
       abilityLevels.clear();
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
