package com.playerstats.fabric;

import com.playerstats.ModAttributes;
import com.playerstats.PlayerStats;
import com.playerstats.command.PlayerStatsCommands;
import com.playerstats.event.*;
import com.playerstats.items.AttributeBoostScrollItem;
import com.playerstats.items.ModItems;
import com.playerstats.network.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;

public class PlayerStatsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModItems.init();
        ModAttributes.init();
        PlayerStats.init();

        registerServerReceivers();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                PlayerStatsCommands.register(dispatcher));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(ModItems.UPGRADE_RUNE.get());
            content.accept(ModItems.ATTRIBUTE_BOOST_SCROLL.get());
            content.accept(ModItems.ABILITY_CRYSTAL.get());
            content.accept(ModItems.SOUL_FRAGMENT.get());
            content.accept(ModItems.SOUL_STONE.get());
        });

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin()) {
                PlayerStats.modifyLootTable(id, tableBuilder::pool);
            }
        });

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

    private void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(PacketHandler.ACTIVATE_INFINITE_ICE,
                (server, player, handler, buf, responseSender) -> {
                    ActivateInfiniteIcePacket pkt = ActivateInfiniteIcePacket.decode(buf);
                    server.execute(() -> PacketHandler.handleActivateInfiniteIce(pkt, player));
                });

        ServerPlayNetworking.registerGlobalReceiver(PacketHandler.MODIFY_ATTRIBUTE,
                (server, player, handler, buf, responseSender) -> {
                    ModifyAttributePacket pkt = ModifyAttributePacket.decode(buf);
                    server.execute(() -> PacketHandler.handleModifyAttribute(pkt, player));
                });

        ServerPlayNetworking.registerGlobalReceiver(PacketHandler.REQUEST_ENTITY_UPGRADES,
                (server, player, handler, buf, responseSender) -> {
                    RequestEntityUpgradesPacket pkt = RequestEntityUpgradesPacket.decode(buf);
                    server.execute(() -> PacketHandler.handleRequestEntityUpgrades(pkt, player));
                });

        ServerPlayNetworking.registerGlobalReceiver(PacketHandler.RESET_ATTRIBUTES,
                (server, player, handler, buf, responseSender) -> {
                    ResetAttributesPacket pkt = ResetAttributesPacket.decode(buf);
                    server.execute(() -> PacketHandler.handleResetAttributes(pkt, player));
                });
    }
}
