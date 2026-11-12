package com.playerstats.fabric;

import com.playerstats.ModAttributes;
import com.playerstats.PlayerStats;
import com.playerstats.command.PlayerStatsCommands;
import com.playerstats.event.*;
import com.playerstats.items.AttributeBoostScrollItem;
import com.playerstats.items.ModItems;
import com.playerstats.network.*;
import com.playerstats.util.ModDataComponents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;

public class PlayerStatsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Carrega registros e inicialização comum
        ModItems.init();
        ModDataComponents.init();
        ModAttributes.init();
        PlayerStats.init();

        // Registra tipos de payloads
        registerPayloadTypes();

        // Registra receivers no servidor
        registerServerReceivers();

        // Comandos
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                PlayerStatsCommands.register(dispatcher));

        // Itens criativos
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(ModItems.UPGRADE_RUNE.get());
            content.accept(ModItems.ATTRIBUTE_BOOST_SCROLL.get());
            content.accept(ModItems.ABILITY_CRYSTAL.get());
            content.accept(ModItems.SOUL_FRAGMENT.get());
            content.accept(ModItems.SOUL_STONE.get());
        });

        // Loot tables
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) ->
                PlayerStats.modifyLootTable(key.location(), tableBuilder::withPool));

        // Eventos do jogador e conexões
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerAttributePersistence.handleLogin(handler.player);
            AttributeBoostScrollItem.handlePlayerLoggedIn(handler.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                AttributeBoostScrollItem.handlePlayerLoggedOut(handler.player));

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PlayerAttributePersistence.handleClone(oldPlayer, newPlayer, !alive);
            InfiniteIceHandler.handlePlayerClone(newPlayer);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                PlayerAttributePersistence.handleRespawn(newPlayer));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            PlayerPointHandler.handleMobKilled(entity, damageSource);
            if (entity instanceof ServerPlayer player) {
                AttributeBoostScrollItem.handlePlayerDeath(player);
            }
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            VampirismHandler.handleLivingDamage(entity, source, amount);
            ModAbilityEvents.handleWolfDamage(entity, source);
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                ScoutVisionHandler.handleLevelTick(level);
                SoulReviveHandler.handleLevelTick(level);
                InfiniteIceHandler.handleLevelTick(level);

                for (ServerPlayer player : level.players()) {
                    AttributeBoostScrollItem.handlePlayerTick(player);
                    InfiniteIceHandler.handlePlayerTick(player);
                }
            }
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) ->
                ModAbilityEvents.handleEntityJoin(entity, world));

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) ->
                !InfiniteIceHandler.handleBlockBreak(world, pos, state));
    }

    private void registerPayloadTypes() {
        // C2S
        PayloadTypeRegistry.playC2S().register(ModifyAttributePacket.TYPE, ModifyAttributePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestEntityUpgradesPacket.TYPE, RequestEntityUpgradesPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ResetAttributesPacket.TYPE, ResetAttributesPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ActivateInfiniteIcePacket.TYPE, ActivateInfiniteIcePacket.CODEC);

        // S2C
        PayloadTypeRegistry.playS2C().register(UpdatePointsPacket.TYPE, UpdatePointsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateUpgradeCountPacket.TYPE, UpdateUpgradeCountPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncAbilityLevelPacket.TYPE, SyncAbilityLevelPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEntityUpgradesPacket.TYPE, SyncEntityUpgradesPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BoostsSyncPacket.TYPE, BoostsSyncPacket.CODEC);
    }

    private void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ModifyAttributePacket.TYPE,
                (pkt, context) -> context.server().execute(() -> PacketHandler.handleModifyAttribute(pkt, context.player())));

        ServerPlayNetworking.registerGlobalReceiver(RequestEntityUpgradesPacket.TYPE,
                (pkt, context) -> context.server().execute(() -> PacketHandler.handleRequestEntityUpgrades(pkt, context.player())));

        ServerPlayNetworking.registerGlobalReceiver(ResetAttributesPacket.TYPE,
                (pkt, context) -> context.server().execute(() -> PacketHandler.handleResetAttributes(pkt, context.player())));

        ServerPlayNetworking.registerGlobalReceiver(ActivateInfiniteIcePacket.TYPE,
                (pkt, context) -> context.server().execute(() -> PacketHandler.handleActivateInfiniteIce(pkt, context.player())));
    }
}
