package com.playerstats.event;

import com.playerstats.network.PacketHandler;
import com.playerstats.network.UpdatePointsPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "playerstats")
public class PlayerPointHandler {

    private static final Map<UUID, Long> lastDayPointGiven = new HashMap<>();

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;



        var mob = event.getEntity();
        double maxHealth = mob.getMaxHealth();


        // 50% para Wither e Dragão
         if (mob.getType() == EntityType.WITHER || mob.getType() == EntityType.ENDER_DRAGON) {
            if (Math.random() < 0.5) givePoints( player, 1);
        }

        // 30% para Warden
        else if (mob.getType() == EntityType.WARDEN) {
            if (Math.random() < 0.3) givePoints(player, 1);
        }

        // 5% de chance se mob tiver mais de 200 de vida
        else if (maxHealth > 200 && Math.random() < 0.05) {
            givePoints(player, 1);
        }

        // 1% de chance se mob tiver menos de 200 de vida
        else if (maxHealth < 200 && Math.random() < 0.01) {
            givePoints(player, 1);
        }


    }

    private static void givePoints(ServerPlayer player, int amount) {
        long currentDay = player.level().getDayTime() / 24000L;
        UUID uuid = player.getUUID();

        Long lastGiven = lastDayPointGiven.getOrDefault(uuid, -1L);
        if (lastGiven == currentDay) {
            return; // Já ganhou ponto hoje
        }

        // Marca como já recebido hoje
        lastDayPointGiven.put(uuid, currentDay);

        PlayerAttributePersistence.addPoints(player, amount);
        int newPoints = PlayerAttributePersistence.getPoints(player);
        PacketHandler.sendToClient(new UpdatePointsPacket(newPoints), player);
        player.sendSystemMessage(Component.literal("§aVocê ganhou +1 ponto de atributo!"));
    }
}

