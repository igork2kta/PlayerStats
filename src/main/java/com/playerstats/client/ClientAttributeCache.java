package com.playerstats.client;

public class ClientAttributeCache {
    private static int points;

    public static void setPoints(int pts) {
        points = pts;
    }

    public static int getPoints() {
        return points;
    }

}
