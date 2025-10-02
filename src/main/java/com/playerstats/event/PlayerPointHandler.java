package com.playerstats.event;

import com.playerstats.Config;
import com.playerstats.PlayerStats;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



@EventBusSubscriber(modid = "playerstats")
public class PlayerPointHandler {

    private static final Map<UUID, Long> lastDayPointGiven = new HashMap<>();


    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        int totalKills = player.getStats().getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));

        if(totalKills > 0 && (totalKills == 100 || totalKills%1000 == 0)) {
            player.sendSystemMessage(Component.translatable("event.playerstats.mobs_killed", totalKills));
            givePoint(player);
        }

        LivingEntity mob = event.getEntity();

        double baseChance = 0.0;

/*
        if (isProgressiveBossesInstalled()) {
            if (String.valueOf(mob.getType()).contains("wither") && !String.valueOf(mob.getType()).contains("skeleton")) {

                baseChance = Config.WITHER_CHANCE.get();

                if (Config.DEBUG_MODE.get()) {
                    PlayerStats.LOGGER.info("Wither killed via Progressive Bosses!");
                    player.sendSystemMessage(Component.literal("PlayerStatsDebug: Wither killed via Progressive Bosses!"));
                }

                processChance(player, baseChance);
                return;
            }
        }
*/
        // Lógica padrão (sem Progressive Bosses)
        if (mob.getType() == EntityType.WITHER) {

            baseChance = Config.WITHER_CHANCE.get();

            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Wither killed!");
                player.sendSystemMessage(Component.literal("PlayerStatsDebug: Wither killed!"));
            }
        }

        else if (mob.getType() == EntityType.ENDER_DRAGON) {

            baseChance = Config.ENDER_DRAGON_CHANCE.get();

            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Ender dragon killed!");
                player.sendSystemMessage(Component.literal("PlayerStatsDebug: Ender dragon killed!"));
            }

        }

        else if (mob.getType() == EntityType.WARDEN) {
            baseChance = Config.WARDEN_CHANCE.get();

            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Warden killed!");
                player.sendSystemMessage(Component.literal("PlayerStatsDebug: Warden killed!"));
            }
        }

        else if (mob.getType() == EntityType.ELDER_GUARDIAN) {
            baseChance = Config.ELDER_GUARDIAN_CHANCE.get();

            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Elder guardian killed!");
                player.sendSystemMessage(Component.literal("PlayerStatsDebug: Elder guardian killed!"));
            }

        }

        else if (mob.getMaxHealth() >= Config.HIGH_HEALTH.get()) {

            baseChance = Config.HIGH_HEALTH_CHANCE.get();

            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("High health mob killed {}", mob.getType());
                player.sendSystemMessage(Component.literal(String.format("PlayerStatsDebug: High health mob killed %s", mob.getType())));
            }

        }

        if (Config.cachedCustomMobChances.containsKey(mob.getType().getDescriptionId())) {
            baseChance = Config.cachedCustomMobChances.get(mob.getType().getDescriptionId());
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Custom mob killed: {}", mob.getType());
                player.sendSystemMessage(Component.literal(String.format("PlayerStatsDebug: Custom mob killed: %s", mob.getType())));
            }
        }

        if (baseChance == 0.0){
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Mob killed with no point chance: {}", mob.getType());
                player.sendSystemMessage(Component.literal(String.format("PlayerStatsDebug: Mob killed with no point chance: %s", mob.getType())));
            }
            return;
        }

        processChance(player, baseChance);


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
            if(Config.DEBUG_MODE.get()){
                PlayerStats.LOGGER.info("Player {} already received points today, ignoring.", player.getName().getString());
                player.sendSystemMessage(Component.literal(String.format("PlayerStatsDebug: Player %s already received points today, ignoring.", player.getName().getString())));
            }
            return; // Já ganhou ponto hoje
        }

        // Marca como já recebido hoje
        lastDayPointGiven.put(uuid, currentDay);

        PlayerAttributePersistence.addPoints(player, 1);
    }
/*
    public static boolean isProgressiveBossesInstalled(){
        if (ModList.get().isLoaded("progressivebosses")){
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Progressive Bosses mod detected!");
            }
            return true;
        }
        else return false;
    }*/
}

