package com.playerstats.event;

import com.playerstats.Config;
import com.playerstats.PlayerStats;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerPointHandler {

    private static final Map<UUID, Long> lastDayPointGiven = new HashMap<>();

    public static void handleMobKilled(LivingEntity mob, DamageSource source) {
        if (source == null || !(source.getEntity() instanceof ServerPlayer player)) return;

        int totalKills = player.getStats().getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));

        if (totalKills > 0 && (totalKills == 100 || totalKills % 1000 == 0)) {
            player.sendSystemMessage(Component.translatable("event.playerstats.mobs_killed", totalKills));
            givePoint(player);
        }

        double baseChance = 0.0;

        if (mob.getType() == EntityType.WITHER) {
            baseChance = Config.WITHER_CHANCE.get();
        } else if (mob.getType() == EntityType.ENDER_DRAGON) {
            baseChance = Config.ENDER_DRAGON_CHANCE.get();
        } else if (mob.getType() == EntityType.WARDEN) {
            baseChance = Config.WARDEN_CHANCE.get();
        } else if (mob.getType() == EntityType.ELDER_GUARDIAN) {
            baseChance = Config.ELDER_GUARDIAN_CHANCE.get();
        } else if (mob.getMaxHealth() >= Config.HIGH_HEALTH.get()) {
            baseChance = Config.HIGH_HEALTH_CHANCE.get();
        }

        String mobId = mob.getType().getDescriptionId();
        if (Config.getCustomMobChances().containsKey(mobId)) {
            baseChance = Config.getCustomMobChances().get(mobId);
        }

        if (baseChance > 0.0) {
            processChance(player, baseChance);
        }
    }

    private static void processChance(ServerPlayer player, double baseChance) {
        if (Config.DEBUG_MODE.get()) {
            PlayerStats.LOGGER.info("Point chance: {}", baseChance);
            player.sendSystemMessage(Component.literal(String.format("PlayerStatsDebug: Point chance: %s", baseChance)));
        }

        if (player.level().random.nextDouble() < baseChance) {
            givePoint(player);
        }
    }

    private static void givePoint(ServerPlayer player) {
        long currentDay = player.level().getDayTime() / 24000L;
        UUID uuid = player.getUUID();

        Long lastGiven = lastDayPointGiven.getOrDefault(uuid, -1L);
        if (lastGiven == currentDay) {
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Player {} already received points today, ignoring.", player.getName().getString());
                player.sendSystemMessage(Component.literal(String.format("PlayerStatsDebug: Player %s already received points today, ignoring.", player.getName().getString())));
            }
            return;
        }

        lastDayPointGiven.put(uuid, currentDay);
        PlayerAttributePersistence.addPoints(player, 1);
    }
}
