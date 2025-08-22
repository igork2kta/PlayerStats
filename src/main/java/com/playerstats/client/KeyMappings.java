package com.playerstats.client;


import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(
        modid = "playerstats",
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT // <-- ESSENCIAL para evitar carregamento no servidor
)
public class KeyMappings {

    public static final KeyMapping OPEN_STATS_KEY = new KeyMapping(
            "key.playerstats.open_stats",
            GLFW.GLFW_KEY_R,
            "key.categories.playerstats"
    );

    public static final KeyMapping OPEN_ENTITY_STATS_KEY = new KeyMapping(
            "key.playerstats.open_entity_stats", // chave de tradução
            GLFW.GLFW_KEY_H,                // tecla H
            "key.categories.playerstats"    // categoria
    );


    //Aqui o atalho é registrado nos controles do jogo, permitindo alterar, o padrão é R
    @SubscribeEvent
    public static void onKeyMappingRegister(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STATS_KEY);
        event.register(OPEN_ENTITY_STATS_KEY);
    }
}
