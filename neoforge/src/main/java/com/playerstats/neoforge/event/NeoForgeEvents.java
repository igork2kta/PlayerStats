package com.playerstats.neoforge.event;

import com.playerstats.client.KeyBindings;
import com.playerstats.event.*;
import com.playerstats.items.AttributeBoostScrollItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class NeoForgeEvents {

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        PlayerPointHandler.handleMobKilled(event.getEntity(), event.getSource());
        if (event.getEntity() instanceof ServerPlayer player) {
            AttributeBoostScrollItem.handlePlayerDeath(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        PlayerAttributePersistence.handleClone(event.getOriginal(), event.getEntity(), event.isWasDeath());
        InfiniteIceHandler.handlePlayerClone(event.getEntity());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerAttributePersistence.handleLogin(player);
            AttributeBoostScrollItem.handlePlayerLoggedIn(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        AttributeBoostScrollItem.handlePlayerLoggedOut(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerAttributePersistence.handleRespawn(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        AttributeBoostScrollItem.handlePlayerTick(event.getEntity());
        InfiniteIceHandler.handlePlayerTick(event.getEntity());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        ScoutVisionHandler.handleLevelTick(event.getLevel());
        SoulReviveHandler.handleLevelTick(event.getLevel());
        InfiniteIceHandler.handleLevelTick(event.getLevel());
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        MilkyAuraHandler.handleEntityTick(event.getEntity());
    }

    @SubscribeEvent
    public static void onHorseTick(EntityTickEvent.Pre event) {
        ModAbilityEvents.handleHorseTick(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        ModAbilityEvents.handleEntityJoin(event.getEntity(), event.getLevel());
    }

    @SubscribeEvent
    public static void onAnimalTame(AnimalTameEvent event) {
        ModAbilityEvents.handleAnimalTame(event.getEntity(), event.getTamer());
    }

    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        ModAbilityEvents.handleMobDrops(event.getEntity(), event.getDrops());
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        VampirismHandler.handleLivingDamage(event.getEntity(), event.getSource(), event.getNewDamage());
        ModAbilityEvents.handleWolfDamage(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        LivingEntity evaluated = TauntHandler.evaluateTarget(event.getEntity(), event.getOriginalAboutToBeSetTarget());
        if (evaluated != event.getOriginalAboutToBeSetTarget()) {
            event.setNewAboutToBeSetTarget(evaluated);
        }
        ModAbilityEvents.handleWolfTargetChange(event.getEntity(), event.getNewAboutToBeSetTarget());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level) {
            if (InfiniteIceHandler.handleBlockBreak(level, event.getPos(), event.getState())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        KeyBindings.handleClientTick();
    }
}
