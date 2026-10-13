package com.playerstats.event;

import com.playerstats.ModAttributes;
import com.playerstats.items.ModItems;
import com.playerstats.util.AttributeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = "playerstats")
public class InfiniteIceHandler {

    public static final String NBT_ICE_TIMER = "InfiniteIceTimer";
    public static final String NBT_ICE_COOLDOWN = "InfiniteIceCooldown";

    public static final int DURATION_TICKS = 60 * 20; // 1 minuto (1200 ticks)
    public static final int COOLDOWN_TICKS = DURATION_TICKS * 3; // 3 minutos (3600 ticks)

    public static void activateAbility(ServerPlayer player) {
        // 1. Checa se o jogador possui o atributo habilitado
        double abilityValue = AttributeUtils.getAttributeValue(player, ModAttributes.INFINITE_ICE);
        if (abilityValue < 1.0D) {
            player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_locked")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        CompoundTag data = player.getPersistentData();

        // 2. Checa se já está ativo
        int currentTimer = data.getInt(NBT_ICE_TIMER);
        if (currentTimer > 0) {
            int remainingSeconds = (currentTimer + 19) / 20;
            player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_already_active", remainingSeconds)
                    .withStyle(ChatFormatting.GOLD));
            return;
        }

        // 3. Checa cooldown
        int currentCooldown = data.getInt(NBT_ICE_COOLDOWN);
        if (currentCooldown > 0) {
            int remainingSeconds = (currentCooldown + 19) / 20;
            player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_cooldown", remainingSeconds)
                    .withStyle(ChatFormatting.RED));
            return;
        }

        // 4. Checa item na mão principal
        ItemStack heldItem = player.getMainHandItem();
        Inventory inventory = player.getInventory();

        if (!heldItem.isEmpty()) {
            // Tenta mover o item segurado para o inventário
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            if (!inventory.add(heldItem)) {
                // Inventário cheio! Restaura o item na mão e cancela a habilidade
                player.setItemInHand(InteractionHand.MAIN_HAND, heldItem);
                player.sendSystemMessage(Component.translatable("gui.playerstats.ice_inventory_full")
                        .withStyle(ChatFormatting.RED));
                return;
            }
        }

        // 5. Dá o item de Gelo Infinito na mão principal
        ItemStack iceStack = new ItemStack(ModItems.INFINITE_ICE.get(), 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, iceStack);

        // 6. Seta timer
        data.putInt(NBT_ICE_TIMER, DURATION_TICKS);

        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
        player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_activated")
                .withStyle(ChatFormatting.AQUA));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        CompoundTag data = player.getPersistentData();

        // Reduz cooldown se houver
        int cooldown = data.getInt(NBT_ICE_COOLDOWN);
        if (cooldown > 0) {
            data.putInt(NBT_ICE_COOLDOWN, cooldown - 1);
        }

        // Gerencia timer ativo
        int timer = data.getInt(NBT_ICE_TIMER);
        if (timer > 0) {
            timer--;
            data.putInt(NBT_ICE_TIMER, timer);

            if (timer == 0) {
                // Habilidade expirou!
                removeInfiniteIceItem(player);
                data.putInt(NBT_ICE_COOLDOWN, COOLDOWN_TICKS);

                player.level().playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_expired")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static final Map<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>, Map<BlockPos, Long>> iceBlocks = new HashMap<>();

    public static void registerPlacedIce(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        long expireTick = level.getGameTime() + 200; // 10 segundos para sumir
        iceBlocks.computeIfAbsent(level.dimension(), k -> new HashMap<>()).put(pos.immutable(), expireTick);
    }

    @SubscribeEvent
    public static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide) return;
        var level = event.getLevel();
        var map = iceBlocks.get(level.dimension());
        if (map == null || map.isEmpty()) return;

        long currentTime = level.getGameTime();
        var iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (currentTime >= entry.getValue()) {
                BlockPos pos = entry.getKey();
                var state = level.getBlockState(pos);
                // Se ainda for gelo normal ou frosted ice, vira AR (sem deixar água!)
                if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                net.minecraft.core.particles.ParticleTypes.POOF,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                5, 0.2, 0.2, 0.2, 0.05
                        );
                    }
                }
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        // Se quebrarem o gelo temporário com a mão ou ferramenta, não deixa virar água nem dropar
        if (event.getState().is(Blocks.ICE) || event.getState().is(Blocks.FROSTED_ICE)) {
            if (event.getLevel() instanceof Level level) {
                var map = iceBlocks.get(level.dimension());
                if (map != null && map.containsKey(event.getPos())) {
                    map.remove(event.getPos());
                    level.setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Ao morrer/renascer, limpa o item para evitar duplicação ou travamento
        Player player = event.getEntity();
        removeInfiniteIceItem(player);
        player.getPersistentData().remove(NBT_ICE_TIMER);
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

