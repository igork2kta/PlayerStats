package com.playerstats.forge.event;

import com.playerstats.command.PlayerStatsCommands;
import com.playerstats.event.*;
import com.playerstats.items.AttributeBoostScrollItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PlayerStatsCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        VampirismHandler.handleLivingDamage(event.getEntity(), event.getSource(), event.getAmount());
        ModAbilityEvents.handleWolfDamage(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        var evaluated = TauntHandler.evaluateTarget(event.getEntity(), event.getNewTarget());
        event.setNewTarget(evaluated);
        ModAbilityEvents.handleWolfTargetChange(event.getEntity(), evaluated);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getOriginal() instanceof ServerPlayer original) {
            PlayerAttributePersistence.handleClone(original, player, event.isWasDeath());
            InfiniteIceHandler.handlePlayerClone(player);
        }
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
    public static void onDeath(LivingDeathEvent event) {
        PlayerPointHandler.handleMobKilled(event.getEntity(), event.getSource());
        if (event.getEntity() instanceof ServerPlayer player) {
            AttributeBoostScrollItem.handlePlayerDeath(player);
        }
    }

    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        ModAbilityEvents.handleMobDrops(event.getEntity(), event.getDrops());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            InfiniteIceHandler.handlePlayerTick(event.player);
            AttributeBoostScrollItem.handlePlayerTick(event.player);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ScoutVisionHandler.handleLevelTick(event.level);
            SoulReviveHandler.handleLevelTick(event.level);
            InfiniteIceHandler.handleLevelTick(event.level);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        MilkyAuraHandler.handleEntityTick(event.getEntity());
        ModAbilityEvents.handleHorseTick(event.getEntity());
    }

    @SubscribeEvent
    public static void onAnimalTame(AnimalTameEvent event) {
        if (event.getEntity() instanceof AbstractHorse horse) {
            ModAbilityEvents.handleHorseTamed(horse, event.getTamer());
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        ModAbilityEvents.handleEntityJoin(event.getEntity(), event.getLevel());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof net.minecraft.world.level.Level level) {
            if (InfiniteIceHandler.handleBlockBreak(level, event.getPos(), event.getState())) {
                event.setCanceled(true);
            }
        }
    }
}
