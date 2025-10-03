package com.playerstats.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

        // Executa no final do tick
        if(event.phase == TickEvent.Phase.END){
            if (KeyMappings.OPEN_STATS_KEY.consumeClick()) {
                Minecraft.getInstance().setScreen(new StatsScreen());
            }

            if (KeyMappings.OPEN_ENTITY_STATS_KEY.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                HitResult hit = mc.hitResult;

                if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity entity) {
                    Minecraft.getInstance().setScreen(new StatsScreen(entity));
                    return;
                }
                //Se o player estiver montado sem mirar em ninguem, acessa os atributos da entidade montada
                Player player = Minecraft.getInstance().player;
                assert player != null;
                if(player.getVehicle() != null){
                    Minecraft.getInstance().setScreen(new StatsScreen((LivingEntity) player.getVehicle()));
                }
            }
        }

    }
}
