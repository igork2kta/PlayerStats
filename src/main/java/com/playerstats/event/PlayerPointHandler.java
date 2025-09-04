package com.playerstats.event;

import com.playerstats.Config;
import com.playerstats.PlayerStats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



@Mod.EventBusSubscriber(modid = "playerstats")
public class PlayerPointHandler {

    private static final Map<UUID, Long> lastDayPointGiven = new HashMap<>();


    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity mob = event.getEntity();

        double baseChance = 0.0;


        if (isProgressiveBossesInstalled()) {
            if (String.valueOf(mob.getType()).contains("wither") && !String.valueOf(mob.getType()).contains("skeleton")) {
                if (Config.DEBUG_MODE.get()) {
                    PlayerStats.LOGGER.info("Wither morto via Progressive Bosses!");
                }
                baseChance = 0.5;

                processChance(player, baseChance);
                return;
            }
        }

        // Lógica padrão (sem Progressive Bosses)
        if (mob.getType() == EntityType.WITHER || mob.getType() == EntityType.ENDER_DRAGON) {
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Wither/Ender dragon morto!");
            }
            baseChance = 0.5;
        }
        else if (mob.getType() == EntityType.WARDEN) {
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Warden morto!");
            }
            baseChance = 0.3;
        }
        else if (mob.getType() == EntityType.ELDER_GUARDIAN) {
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Elder guardian morto!");
            }
            baseChance = 0.3;
        }
        /*
        else if (String.valueOf(mob.getType()).contains("cerberus")) {
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Cerberus morto!");
            }
            baseChance = 0.15;
        }

         */
        else if (mob.getMaxHealth() > 200.0f) {
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Mob com mais de 200 de vida morto: {}", mob.getType());
            }
            baseChance = 0.05;
        }

        if (Config.cachedCustomMobChances.containsKey(mob.getType().getDescriptionId())) {
            baseChance = Config.cachedCustomMobChances.get(mob.getType().getDescriptionId());
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Mob customizado morto: {}, chance configurada: {}", mob.getType(), baseChance);
            }
        }

        if (baseChance == 0.0){
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Mob morto sem chances de pontos: {}", mob.getType());
            }
            return;
        }

        processChance(player, baseChance);
    }

    private static void processChance(ServerPlayer player, double baseChance) {
        if (Config.DEBUG_MODE.get()) {
            PlayerStats.LOGGER.info("Chances de ponto: {}", baseChance);
        }

        if (player.level().random.nextDouble() < baseChance) {
            givePoints(player, 1);
        }
    }

    private static void givePoints(ServerPlayer player, int amount) {
        long currentDay = player.level().getDayTime() / 24000L;
        UUID uuid = player.getUUID();

        Long lastGiven = lastDayPointGiven.getOrDefault(uuid, -1L);
        if (lastGiven == currentDay) {
            if(Config.DEBUG_MODE.get()){
                PlayerStats.LOGGER.info("Player {} já recebeu pontos hoje, ignorando.", player.getName());
            }
            return; // Já ganhou ponto hoje
        }

        // Marca como já recebido hoje
        lastDayPointGiven.put(uuid, currentDay);

        PlayerAttributePersistence.addPoints(player, amount);
    }

    public static boolean isProgressiveBossesInstalled(){
        if (ModList.get().isLoaded("progressivebosses")){
            if (Config.DEBUG_MODE.get()) {
                PlayerStats.LOGGER.info("Detectado mod Progressive Bosses!");
            }
            return true;
        }
        else return false;
    }

    public static boolean isBloodMoon(Level world) {
        if (ModList.get().isLoaded("majruszsdifficulty")) {
            try {
                Class<?> helperClass = Class.forName("com.majruszsdifficulty.events.BloodMoonHelper");
                Method method = helperClass.getDeclaredMethod("isBloodMoon", Level.class);
                return (boolean) method.invoke(null, world);
            } catch (Exception ignored) {}
        }
        return false;


        /*
        // Se for Blood Moon e o mod estiver presente, aumenta em 20%
        if (isBloodMoon(player.level())) {
            player.sendSystemMessage(Component.literal("Blood moon man!"));
            baseChance *= 1.20;
        }
*/
    }
}

