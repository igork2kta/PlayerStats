package com.playerstats.client;

import com.playerstats.network.ActivateInfiniteIcePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

public class KeyBindings {

    public static void register() {
        EVENT_BUS.register(KeyBindings.class);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        if (KeyMappings.OPEN_STATS_KEY.get().consumeClick()) {
            Minecraft.getInstance().setScreen(new StatsScreen());
        }

        if (KeyMappings.OPEN_ENTITY_STATS_KEY.get().consumeClick()) {
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

        if (KeyMappings.ACTIVATE_ICE_KEY.get().consumeClick()) {
            PacketDistributor.sendToServer(new ActivateInfiniteIcePacket());
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

    private static java.lang.reflect.Field rightClickDelayField = null;
    private static boolean fieldLookupFailed = false;

    private static void reduceRightClickDelay(Minecraft mc) {
        if (fieldLookupFailed) return;
        try {
            if (rightClickDelayField == null) {
                // rightClickDelay no Minecraft 1.21.1 Parchment mappings
                try {
                    rightClickDelayField = Minecraft.class.getDeclaredField("rightClickDelay");
                } catch (NoSuchFieldException e) {
                    rightClickDelayField = Minecraft.class.getDeclaredField("f_91060_"); // SRG fallback
                }
                rightClickDelayField.setAccessible(true);
            }
            int currentDelay = rightClickDelayField.getInt(mc);
            if (currentDelay > 1) {
                rightClickDelayField.setInt(mc, 1); // 1 tick de delay em vez de 4 ticks
            }
        } catch (Exception e) {
            fieldLookupFailed = true;
        }
    }
}
