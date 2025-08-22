package com.playerstats.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
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
                KeyMappings.OPEN_STATS_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new StatsScreen());
        }
        // Executa no final do tick
        if (event.phase == TickEvent.Phase.END && KeyMappings.OPEN_ENTITY_STATS_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            HitResult hit = mc.hitResult;

            if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity entity) {
                Minecraft.getInstance().setScreen(new StatsScreen(entity));
            }
        }
    }
}
