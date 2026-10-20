package com.playerstats.event;

import com.playerstats.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "playerstats")
public class MilkyAuraHandler {

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Cow cow)) return;
        if (cow.level().isClientSide() || !cow.isAlive()) return;

        // Executa a cada 10 ticks (0.5 segundo) para performance
        if (cow.tickCount % 10 != 0) return;

        var auraAttr = cow.getAttribute(ModAttributes.MILKY_AURA.get());
        if (auraAttr == null) return;

        double levelValue = auraAttr.getValue();
        int level = (int) Math.round(levelValue);
        if (level <= 0) return;

        // Escala o raio da aura de acordo com o nível: Nv 1 = 2b, Nv 2 = 4b, Nv 3 = 6b
        double auraRadius = level * 2.0D;

        // Área de efeito ao redor da vaca
        AABB auraBox = cow.getBoundingBox().inflate(auraRadius);
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

