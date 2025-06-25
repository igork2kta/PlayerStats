package com.playerstats.client;

public class ClientAttributeCache {
    private static int points;
    private static int upgradeCount;

    public static void setPoints(int pts) {
        points = pts;
    }

    public static int getPoints() {
        return points;
    }

    public static void setUpgradeCount(int count) {
        upgradeCount = count;
    }

    public static int getUpgradeCount() {
        return upgradeCount;
    }

}
