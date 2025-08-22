package com.playerstats.client;

import com.playerstats.PlayerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
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
        while (OPEN_STATS_KEY.get().consumeClick()) {
            Minecraft.getInstance().setScreen(new StatsScreen());
        }
        while (OPEN_ENTITY_STATS_KEY.get().consumeClick()) {
            PlayerStats.LOGGER.info("Atalho pressionado");
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // Raycast para pegar a entidade que o player está mirando
                HitResult hit = mc.hitResult;
                if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity entity) {
                    Minecraft.getInstance().setScreen(new StatsScreen(entity));
                }
            }
        }

    }
}
