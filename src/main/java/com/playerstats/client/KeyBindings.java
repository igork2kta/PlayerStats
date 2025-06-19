package com.playerstats.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyBindings {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END &&
                KeyMappingsRegistrar.OPEN_STATS_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new StatsScreen());
        }
    }
}
