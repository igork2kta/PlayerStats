package com.playerstats.client;

public class ClientAttributeCache {
    public static boolean pointsInitialized = false;
    private static int points;
    private static int upgradeCount;
    private static int abilityPoints;

    public static void clean(){
       pointsInitialized = false;
       points = 0;
       upgradeCount = 0;
       abilityPoints = 0;
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
