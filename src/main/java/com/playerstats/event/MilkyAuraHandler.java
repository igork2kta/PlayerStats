package com.playerstats.event;

import com.playerstats.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

@EventBusSubscriber(modid = "playerstats")
public class MilkyAuraHandler {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Cow cow)) return;
        if (cow.level().isClientSide() || !cow.isAlive()) return;

        // Executa a cada 10 ticks (0.5 segundo) para performance
        if (cow.tickCount % 10 != 0) return;

        var auraAttr = cow.getAttribute(ModAttributes.MILKY_AURA.getDelegate());
        if (auraAttr == null) return;

        double levelValue = auraAttr.getValue();
        int level = (int) Math.round(levelValue);
        if (level <= 0) return;

        // Escala o raio da aura de acordo com o nÃ­vel: Nv 1 = 2b, Nv 2 = 4b, Nv 3 = 6b
        double auraRadius = level * 2.0D;

        // Ãrea de efeito ao redor da vaca
        AABB auraBox = cow.getBoundingBox().inflate(auraRadius);
        List<LivingEntity> nearby = cow.level().getEntitiesOfClass(
                LivingEntity.class,
                auraBox,
                e -> e.isAlive() && !e.getActiveEffects().isEmpty()
        );

        for (LivingEntity entity : nearby) {
            boolean removed = entity.removeAllEffects();

            if (removed && cow.level() instanceof ServerLevel serverLevel) {
                // PartÃ­culas de poÃ§Ã£o/cura saindo da entidade curada
                serverLevel.sendParticles(
                        SpellParticleOption.create(ParticleTypes.EFFECT, 0xFFFFFF, 1.0F),
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

        // PartÃ­culas ambientais sutis ao redor da vaca enquanto a aura estiver ativa
        if (cow.tickCount % 30 == 0 && cow.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    SpellParticleOption.create(ParticleTypes.EFFECT, 0xFFFFFF, 1.0F),
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

