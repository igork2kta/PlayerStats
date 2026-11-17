package com.playerstats.fabric.client;

import com.playerstats.client.KeyBindings;
import com.playerstats.client.KeyMappings;
import com.playerstats.network.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class PlayerStatsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Registra atalhos de teclado
        KeyMappingHelper.registerKeyMapping(KeyMappings.OPEN_STATS_KEY);
        KeyMappingHelper.registerKeyMapping(KeyMappings.OPEN_ENTITY_STATS_KEY);
        KeyMappingHelper.registerKeyMapping(KeyMappings.ACTIVATE_ICE_KEY);

        // Client Tick para checar pressionamento das teclas
        ClientTickEvents.END_CLIENT_TICK.register(client -> KeyBindings.handleClientTick());

        // Receivers de pacotes vindos do servidor
        ClientPlayNetworking.registerGlobalReceiver(UpdatePointsPacket.TYPE,
                (pkt, context) -> context.client().execute(() -> PacketHandler.handleUpdatePoints(pkt)));

        ClientPlayNetworking.registerGlobalReceiver(UpdateUpgradeCountPacket.TYPE,
                (pkt, context) -> context.client().execute(() -> PacketHandler.handleUpdateUpgradeCount(pkt)));

        ClientPlayNetworking.registerGlobalReceiver(SyncAbilityLevelPacket.TYPE,
                (pkt, context) -> context.client().execute(() -> PacketHandler.handleSyncAbilityLevel(pkt)));

        ClientPlayNetworking.registerGlobalReceiver(SyncEntityUpgradesPacket.TYPE,
                (pkt, context) -> context.client().execute(() -> PacketHandler.handleSyncEntityUpgrades(pkt)));

        ClientPlayNetworking.registerGlobalReceiver(BoostsSyncPacket.TYPE,
                (pkt, context) -> context.client().execute(() -> PacketHandler.handleBoostsSync(pkt)));
    }
}
