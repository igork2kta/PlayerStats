package com.playerstats.client;

import net.minecraft.client.Minecraft;

public class ClientHooks {
    public static void openStatsScreen() {
        Minecraft.getInstance().setScreen(new StatsScreen());
    }
}
