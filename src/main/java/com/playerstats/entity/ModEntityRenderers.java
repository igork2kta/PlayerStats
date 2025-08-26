package com.playerstats.entity;

import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "playerstats", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Reutiliza o HorseRenderer vanilla
        event.registerEntityRenderer(ModEntities.DUAL_SADDLE_HORSE.get(),
                (context) -> new HorseRenderer(context));
    }
}