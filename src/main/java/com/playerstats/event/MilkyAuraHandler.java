package com.playerstats.event;

import com.playerstats.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

@EventBusSubscriber(modid = "playerstats")
public class MilkyAuraHandler {

    private static final double AURA_RADIUS = 2.0D;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Cow cow)) return;
        if (cow.level().isClientSide() || !cow.isAlive()) return;

        // Executa a cada 10 ticks (0.5 segundo) para performance
        if (cow.tickCount % 10 != 0) return;

        var auraAttr = cow.getAttribute(ModAttributes.MILKY_AURA.getDelegate());
        if (auraAttr == null || auraAttr.getValue() != 1.0D) return;

        // Área de efeito de 2 blocos ao redor da vaca
        AABB auraBox = cow.getBoundingBox().inflate(AURA_RADIUS);
        List<LivingEntity> nearby = cow.level().getEntitiesOfClass(
                LivingEntity.class,
                auraBox,
                e -> e.isAlive() && !e.getActiveEffects().isEmpty()
        );

        for (LivingEntity entity : nearby) {
            boolean removed = entity.removeAllEffects();

            if (removed && cow.level() instanceof ServerLevel serverLevel) {
                // Partículas de poção/cura saindo da entidade curada
                serverLevel.sendParticles(
                        ParticleTypes.EFFECT,
                        entity.getX(),
                        entity.getY() + (entity.getBbHeight() / 2.0),
                        entity.getZ(),
                        12,
                        0.25,
                        0.25,
                        0.25,
                        0.05
                );
            }
        }

        // Partículas ambientais sutis ao redor da vaca enquanto a aura estiver ativa
        if (cow.tickCount % 30 == 0 && cow.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EFFECT,
                    cow.getX(),
                    cow.getY() + (cow.getBbHeight() / 2.0),
                    cow.getZ(),
                    3,
                    0.4,
                    0.2,
                    0.4,
                    0.02
            );
        }
    }
}

