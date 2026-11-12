package com.playerstats.event;

import com.playerstats.ModAttributes;
import com.playerstats.items.ModItems;
import com.playerstats.platform.Services;
import com.playerstats.util.AttributeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class InfiniteIceHandler {

    public static final String NBT_ICE_TIMER = "InfiniteIceTimer";
    public static final String NBT_ICE_COOLDOWN = "InfiniteIceCooldown";

    public static final int DURATION_TICKS = 60 * 20; // 1 minuto (1200 ticks)
    public static final int COOLDOWN_TICKS = DURATION_TICKS * 3; // 3 minutos (3600 ticks)

    public static void activateAbility(ServerPlayer player) {
        double abilityValue = AttributeUtils.getAttributeValue(player, ModAttributes.INFINITE_ICE);
        if (abilityValue < 1.0D) {
            player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_locked")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        CompoundTag data = Services.PLATFORM.getPersistentData(player);

        int currentTimer = data.getInt(NBT_ICE_TIMER);
        if (currentTimer > 0) {
            int remainingSeconds = (currentTimer + 19) / 20;
            player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_already_active", remainingSeconds)
                    .withStyle(ChatFormatting.GOLD));
            return;
        }

        int currentCooldown = data.getInt(NBT_ICE_COOLDOWN);
        if (currentCooldown > 0) {
            int remainingSeconds = (currentCooldown + 19) / 20;
            player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_cooldown", remainingSeconds)
                    .withStyle(ChatFormatting.RED));
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        Inventory inventory = player.getInventory();

        if (!heldItem.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            if (!inventory.add(heldItem)) {
                player.setItemInHand(InteractionHand.MAIN_HAND, heldItem);
                player.sendSystemMessage(Component.translatable("gui.playerstats.ice_inventory_full")
                        .withStyle(ChatFormatting.RED));
                return;
            }
        }

        ItemStack iceStack = new ItemStack(ModItems.INFINITE_ICE.get(), 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, iceStack);

        data.putInt(NBT_ICE_TIMER, DURATION_TICKS);

        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
        player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_activated")
                .withStyle(ChatFormatting.AQUA));
    }

    public static void handlePlayerTick(Player player) {
        if (player.level().isClientSide) return;

        CompoundTag data = Services.PLATFORM.getPersistentData(player);

        int cooldown = data.getInt(NBT_ICE_COOLDOWN);
        if (cooldown > 0) {
            data.putInt(NBT_ICE_COOLDOWN, cooldown - 1);
        }

        int timer = data.getInt(NBT_ICE_TIMER);
        if (timer > 0) {
            timer--;
            data.putInt(NBT_ICE_TIMER, timer);

            if (timer == 0) {
                removeInfiniteIceItem(player);
                data.putInt(NBT_ICE_COOLDOWN, COOLDOWN_TICKS);

                player.level().playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_expired")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> iceBlocks = new HashMap<>();

    public static void registerPlacedIce(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        long expireTick = level.getGameTime() + 200;
        iceBlocks.computeIfAbsent(level.dimension(), k -> new HashMap<>()).put(pos.immutable(), expireTick);
    }

    public static void handleLevelTick(Level level) {
        if (level.isClientSide) return;
        var map = iceBlocks.get(level.dimension());
        if (map == null || map.isEmpty()) return;

        long currentTime = level.getGameTime();
        var iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (currentTime >= entry.getValue()) {
                BlockPos pos = entry.getKey();
                var state = level.getBlockState(pos);
                if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.POOF,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                5, 0.2, 0.2, 0.2, 0.05
                        );
                    }
                }
                iterator.remove();
            }
        }
    }

    public static boolean handleBlockBreak(Level level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
            var map = iceBlocks.get(level.dimension());
            if (map != null && map.containsKey(pos)) {
                map.remove(pos);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return true;
            }
        }
        return false;
    }

    public static void handlePlayerClone(Player player) {
        removeInfiniteIceItem(player);
        Services.PLATFORM.getPersistentData(player).remove(NBT_ICE_TIMER);
    }

    private static void removeInfiniteIceItem(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(ModItems.INFINITE_ICE.get())) {
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
