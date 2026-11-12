package com.playerstats.fabric.client;

import com.playerstats.client.KeyBindings;
import com.playerstats.client.KeyMappings;
import com.playerstats.network.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class PlayerStatsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(KeyMappings.OPEN_STATS_KEY);
        KeyBindingHelper.registerKeyBinding(KeyMappings.OPEN_ENTITY_STATS_KEY);
        KeyBindingHelper.registerKeyBinding(KeyMappings.ACTIVATE_ICE_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> KeyBindings.handleClientTick());

        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.UPDATE_POINTS,
                (client, handler, buf, responseSender) -> {
                    UpdatePointsPacket pkt = UpdatePointsPacket.decode(buf);
                    client.execute(() -> PacketHandler.handleUpdatePoints(pkt));
                });

        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.UPDATE_UPGRADE_COUNT,
                (client, handler, buf, responseSender) -> {
                    UpdateUpgradeCountPacket pkt = UpdateUpgradeCountPacket.decode(buf);
                    client.execute(() -> PacketHandler.handleUpdateUpgradeCount(pkt));
                });

        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.SYNC_ABILITY_LEVEL,
                (client, handler, buf, responseSender) -> {
                    SyncAbilityLevelPacket pkt = SyncAbilityLevelPacket.decode(buf);
                    client.execute(() -> PacketHandler.handleSyncAbilityLevel(pkt));
                });

        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.SYNC_ENTITY_UPGRADES,
                (client, handler, buf, responseSender) -> {
                    SyncEntityUpgradesPacket pkt = SyncEntityUpgradesPacket.decode(buf);
                    client.execute(() -> PacketHandler.handleSyncEntityUpgrades(pkt));
                });

        ClientPlayNetworking.registerGlobalReceiver(PacketHandler.BOOSTS_SYNC,
                (client, handler, buf, responseSender) -> {
                    BoostsSyncPacket pkt = BoostsSyncPacket.decode(buf);
                    client.execute(() -> PacketHandler.handleBoostsSync(pkt));
                });
    }
}
