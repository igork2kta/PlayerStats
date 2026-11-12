package com.playerstats.client;

import com.playerstats.network.ActivateInfiniteIcePacket;
import com.playerstats.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class KeyBindings {

    public static void handleClientTick() {
        if (KeyMappings.OPEN_STATS_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new StatsScreen());
        }

        if (KeyMappings.OPEN_ENTITY_STATS_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            HitResult hit = mc.hitResult;

            if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity entity) {
                Minecraft.getInstance().setScreen(new StatsScreen(entity));
                return;
            }
            Player player = Minecraft.getInstance().player;
            if (player != null && player.getVehicle() instanceof LivingEntity vehicle) {
                Minecraft.getInstance().setScreen(new StatsScreen(vehicle));
            }
        }

        if (KeyMappings.ACTIVATE_ICE_KEY.consumeClick()) {
            PacketHandler.sendToServer(new ActivateInfiniteIcePacket());
        }

        // Ritmo acelerado de colocação: se estiver segurando Infinite Ice e segurando o botão direito
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getMainHandItem().is(com.playerstats.items.ModItems.INFINITE_ICE.get())) {
            if (mc.options.keyUse.isDown()) {
                reduceRightClickDelay(mc);
            }
        }
    }

    private static java.lang.reflect.Field rightClickDelayField = null;
    private static boolean fieldLookupFailed = false;

    private static void reduceRightClickDelay(Minecraft mc) {
        if (fieldLookupFailed) return;
        try {
            if (rightClickDelayField == null) {
                try {
                    rightClickDelayField = Minecraft.class.getDeclaredField("rightClickDelay");
                } catch (NoSuchFieldException e) {
                    rightClickDelayField = Minecraft.class.getDeclaredField("f_91060_");
                }
                rightClickDelayField.setAccessible(true);
            }
            int currentDelay = rightClickDelayField.getInt(mc);
            if (currentDelay > 1) {
                rightClickDelayField.setInt(mc, 1);
            }
        } catch (Exception e) {
            fieldLookupFailed = true;
        }
    }
}
