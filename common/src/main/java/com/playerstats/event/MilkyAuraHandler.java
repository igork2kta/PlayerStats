package com.playerstats.event;

import com.playerstats.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class MilkyAuraHandler {

    public static void handleEntityTick(LivingEntity entity) {
        if (!(entity instanceof Cow cow)) return;
        if (cow.level().isClientSide() || !cow.isAlive()) return;

        if (cow.tickCount % 10 != 0) return;

        var auraAttr = cow.getAttribute(ModAttributes.MILKY_AURA.get());
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

        for (LivingEntity nearbyEntity : nearby) {
            boolean removed = nearbyEntity.removeAllEffects();

            if (removed && cow.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.EFFECT,
                        nearbyEntity.getX(),
                        nearbyEntity.getY() + (nearbyEntity.getBbHeight() / 2.0),
                        nearbyEntity.getZ(),
                        12,
                        0.25,
                        0.25,
                        0.25,
                        0.05
                );
            }
        }

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
