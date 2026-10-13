package com.playerstats.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyBindings {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        // Executa no final do tick
        if(event.phase == TickEvent.Phase.END){
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
                //Se o player estiver montado sem mirar em ninguem, acessa os atributos da entidade montada
                Player player = Minecraft.getInstance().player;
                assert player != null;
                if(player.getVehicle() != null){
                    Minecraft.getInstance().setScreen(new StatsScreen((LivingEntity) player.getVehicle()));
                }
            }

            if (KeyMappings.ACTIVATE_ICE_KEY.consumeClick()) {
                com.playerstats.network.PacketHandler.sendToServer(new com.playerstats.network.ActivateInfiniteIcePacket());
            }

            // Ritmo acelerado de colocação: se estiver segurando Infinite Ice e segurando o botão direito,
            // reduz o delay de uso padrão do Minecraft (vanilla usa 4 ticks)
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getMainHandItem().is(com.playerstats.items.ModItems.INFINITE_ICE.get())) {
                if (mc.options.keyUse.isDown()) {
                    reduceRightClickDelay(mc);
                }
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
