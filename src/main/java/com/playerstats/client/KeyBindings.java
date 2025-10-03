package com.playerstats.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import static com.playerstats.client.KeyMappings.OPEN_ENTITY_STATS_KEY;
import static com.playerstats.client.KeyMappings.OPEN_STATS_KEY;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class KeyBindings {

    public static void register() {
        EVENT_BUS.register(KeyBindings.class);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        if (KeyMappings.OPEN_STATS_KEY.get().consumeClick()) {
            Minecraft.getInstance().setScreen(new StatsScreen());
        }

        if (KeyMappings.OPEN_ENTITY_STATS_KEY.get().consumeClick()) {
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
