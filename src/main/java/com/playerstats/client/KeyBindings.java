package com.playerstats.client;

import com.playerstats.network.OpenStatsScreenPacket;
import com.playerstats.network.PacketHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final KeyMapping OPEN_STATS_KEY = new KeyMapping(
            "key.playerstats.open_stats",
            GLFW.GLFW_KEY_R,
            "key.categories.misc"
    );

    public static void register() {
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

//    @SubscribeEvent
//    public static void onClientTick(TickEvent.ClientTickEvent event) {
//        if (event.phase == TickEvent.Phase.END && OPEN_STATS_KEY.consumeClick()) {
//            // Aqui vamos abrir a tela depois de implementar a rede
//            System.out.println("Tecla de atributos pressionada!");
//        }
//    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase == TickEvent.Phase.END && OPEN_STATS_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new StatsScreen());
        }
    }

    @SubscribeEvent
    public static void onKeyMappingRegister(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STATS_KEY);
    }
}
