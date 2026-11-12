package com.playerstats.event;

import com.playerstats.ModAttributes;
import com.playerstats.platform.Services;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TauntHandler {

    private static final Map<UUID, Long> tauntActiveUntil = new HashMap<>();
    private static final Map<UUID, Long> tauntCooldownUntil = new HashMap<>();

    private static final long DURATION_TICKS = 20 * 30;   // 30s ativo
    private static final long COOLDOWN_TICKS = 20 * 60;   // 60s de cooldown

    public static LivingEntity evaluateTarget(LivingEntity attacker, LivingEntity originalTarget) {
        if (attacker == null) return originalTarget;

        CompoundTag attackerData = Services.PLATFORM.getPersistentData(attacker);

        // Se o golem estiver tentando atacar o próprio dono
        if (attacker instanceof IronGolem golem && originalTarget != null) {
            if (attackerData.hasUUID("Owner")) {
                UUID ownerId = attackerData.getUUID("Owner");
                if (originalTarget.getUUID().equals(ownerId)) {
                    return null;
                }
            }
        }

        // só vale se for hostil mirando em um player
        if (!(originalTarget instanceof ServerPlayer)) return originalTarget;

        Level level = attacker.level();

        // Evita problemas caso o dono bata no seu mob com taunt
        if (attackerData.contains("Owner")) {
            if (originalTarget.getUUID().equals(attackerData.getUUID("Owner"))) {
                return null;
            }
        }

        // --- GOLEMS ---
        List<IronGolem> golems = level.getEntitiesOfClass(IronGolem.class,
                attacker.getBoundingBox().inflate(16.0D),
                g -> {
                    var attr = g.getAttribute(ModAttributes.TAUNT);
                    return g.isAlive() && attr != null && attr.getValue() == 1.0D;
                });

        for (IronGolem golem : golems) {
            if (attacker == golem) continue;

            if (activateTaunt(golem)) {
                triggerTauntParticles(golem, level);
            }

            if (isTauntActive(golem)) {
                golem.setTarget(attacker);
                return golem;
            }
        }

        // --- LOBOS ---
        List<Wolf> wolves = level.getEntitiesOfClass(Wolf.class,
                attacker.getBoundingBox().inflate(16.0D),
                w -> {
                    var attr = w.getAttribute(ModAttributes.TAUNT);
                    return w.isAlive() && attr != null && attr.getValue() == 1.0D;
                });

        for (Wolf wolf : wolves) {
            if (attacker == wolf) break;

            if (activateTaunt(wolf)) {
                triggerTauntParticles(wolf, level);
            }

            if (isTauntActive(wolf)) {
                wolf.setTarget(attacker);
                return wolf;
            }
        }

        return originalTarget;
    }

    private static boolean activateTaunt(LivingEntity entity) {
        long now = entity.level().getGameTime();

        if (tauntCooldownUntil.getOrDefault(entity.getUUID(), 0L) > now) {
            return false;
        }

        tauntActiveUntil.put(entity.getUUID(), now + DURATION_TICKS);
        tauntCooldownUntil.put(entity.getUUID(), now + DURATION_TICKS + COOLDOWN_TICKS);
        return true;
    }

    private static boolean isTauntActive(LivingEntity entity) {
        long now = entity.level().getGameTime();
        return tauntActiveUntil.getOrDefault(entity.getUUID(), 0L) > now;
    }

    private static void triggerTauntParticles(LivingEntity entity, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ANGRY_VILLAGER,
                    entity.getX(), entity.getY() + 1.0, entity.getZ(),
                    10, 0.5, 0.5, 0.5, 0.05
            );
        }
    }
}
