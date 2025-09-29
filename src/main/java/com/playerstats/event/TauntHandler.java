package com.playerstats.event;

import com.playerstats.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "playerstats")
public class TauntHandler {

    private static final Map<UUID, Long> tauntActiveUntil = new HashMap<>();
    private static final Map<UUID, Long> tauntCooldownUntil = new HashMap<>();

    private static final long DURATION_TICKS = 20 * 30;   // 30s ativo
    private static final long COOLDOWN_TICKS = 20 * 60;   // 60s de cooldown

    @SubscribeEvent
    public static void onTargetSet(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof IronGolem golem)) return;

        // Se o alvo sendo definido é o dono, cancela
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget != null && golem.getPersistentData().hasUUID("Owner")) {
            UUID ownerId = golem.getPersistentData().getUUID("Owner");
            if (newTarget.getUUID().equals(ownerId)) {
                event.setNewAboutToBeSetTarget(null); // remove o alvo
            }
        }
    }

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntity();
        LivingEntity originalTarget = event.getOriginalAboutToBeSetTarget();

        // só vale se for hostil mirando em um player
        if (!(originalTarget instanceof ServerPlayer)) return;

        Level level = attacker.level();

        // Evita problemas caso o dono bata no seu mob com taunt
        if(attacker.getPersistentData().contains("Owner")){
            if (originalTarget.getUUID().equals(attacker.getPersistentData().getUUID("Owner"))){
                event.setCanceled(true);
                return;
            }
        }

        // --- GOLEMS ---
        List<IronGolem> golems = level.getEntitiesOfClass(IronGolem.class,
                attacker.getBoundingBox().inflate(16.0D),
                g -> g.isAlive()
                        && g.getAttribute(ModAttributes.TAUNT.getDelegate()) != null
                        && g.getAttribute(ModAttributes.TAUNT.getDelegate()).getValue() == 1.0D);

        for (IronGolem golem : golems) {
            if (attacker == golem) continue;

            // Ativa automaticamente se possível
            if (activateTaunt(golem)) {
                triggerTauntParticles(golem, level);
            }

            if (isTauntActive(golem)) {
                event.setNewAboutToBeSetTarget(golem);
                golem.setTarget(attacker);
                return; // prioridade no primeiro encontrado
            }
        }

        // --- LOBOS ---
        List<Wolf> wolves = level.getEntitiesOfClass(Wolf.class,
                attacker.getBoundingBox().inflate(16.0D),
                w -> w.isAlive()
                        && w.getAttribute(ModAttributes.TAUNT.getDelegate()) != null
                        && w.getAttribute(ModAttributes.TAUNT.getDelegate()).getValue() == 1.0D);

        for (Wolf wolf : wolves) {
            if (attacker == wolf) break;

            if (activateTaunt(wolf)) {
                triggerTauntParticles(wolf, level);
            }

            if (isTauntActive(wolf)) {
                event.setNewAboutToBeSetTarget(wolf);
                wolf.setTarget(attacker);
                return;
            }
        }
    }

    // Ativa o taunt automaticamente
    private static boolean activateTaunt(LivingEntity entity) {
        long now = entity.level().getGameTime();

        if (tauntCooldownUntil.getOrDefault(entity.getUUID(), 0L) > now) {
            return false; // ainda em cooldown
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
