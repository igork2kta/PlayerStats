package com.playerstats.event;

import com.playerstats.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class MilkyAuraHandler {

    public static void handleEntityTick(Entity rawEntity) {
        if (!(rawEntity instanceof Cow cow)) return;
        if (cow.level().isClientSide() || !cow.isAlive()) return;

        // Executa a cada 10 ticks (0.5 segundo) para performance
        if (cow.tickCount % 10 != 0) return;

        var auraAttr = cow.getAttribute(ModAttributes.MILKY_AURA);
        if (auraAttr == null) return;

        double levelValue = auraAttr.getValue();
        int level = (int) Math.round(levelValue);
        if (level <= 0) return;

        double auraRadius = level * 2.0D;

        AABB auraBox = cow.getBoundingBox().inflate(auraRadius);
        List<LivingEntity> nearby = cow.level().getEntitiesOfClass(
                LivingEntity.class,
                auraBox,
                e -> e.isAlive() && !e.getActiveEffects().isEmpty()
        );

        for (LivingEntity entity : nearby) {
            boolean removed = entity.removeAllEffects();

            if (removed && cow.level() instanceof ServerLevel serverLevel) {
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

                serverLevel.playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        SoundEvents.COW_MILK,
                        SoundSource.NEUTRAL,
                        0.8f,
                        1.1f
                );
            }
        }
    }
}
