package com.playerstats.client;

public class ClientAttributeCache {
    public static boolean pointsInitialized = false;
    private static int points;
    private static int upgradeCount;

    public static void clean(){
       pointsInitialized = false;
       points = 0;
       upgradeCount = 0;
    }

    public static void setPoints(int pts) {
        pointsInitialized = true;
        points = pts;
    }

    public static int getPoints() {
        return points;
    }

    public static void setUpgradeCount(int count) {
        pointsInitialized = true;
        upgradeCount = count;
    }

    public static int getUpgradeCount() {
        return upgradeCount;
    }

}
