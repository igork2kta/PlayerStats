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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

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

        CompoundTag data = Services.PLATFORM.getPersistentData(player);

        // 2. Checa se já está ativo
        int currentTimer = data.getIntOr(NBT_ICE_TIMER, 0);
        if (currentTimer > 0) {
            int remainingSeconds = (currentTimer + 19) / 20;
            player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_already_active", remainingSeconds)
                    .withStyle(ChatFormatting.GOLD));
            return;
        }

        // 3. Checa cooldown
        int currentCooldown = data.getIntOr(NBT_ICE_COOLDOWN, 0);
        if (currentCooldown > 0) {
            int remainingSeconds = (currentCooldown + 19) / 20;
            player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_cooldown", remainingSeconds)
                    .withStyle(ChatFormatting.RED));
            return;
        }

        // 4. Guarda o item na mão principal se houver
        if (!stashHeldItem(player)) {
            player.sendSystemMessage(Component.translatable("gui.playerstats.ice_inventory_full")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        // 5. Ativa a habilidade
        data.putInt(NBT_ICE_TIMER, DURATION_TICKS);
        data.putInt(NBT_ICE_COOLDOWN, COOLDOWN_TICKS);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.INFINITE_ICE.get()));
        player.containerMenu.broadcastChanges();

        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
        player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_activated")
                .withStyle(ChatFormatting.AQUA));
    }

    public static void handlePlayerTick(Player player) {
        if (player.level().isClientSide()) return;

        CompoundTag data = Services.PLATFORM.getPersistentData(player);

        // Decrementa o cooldown se existir
        int cooldown = data.getIntOr(NBT_ICE_COOLDOWN, 0);
        if (cooldown > 0) {
            data.putInt(NBT_ICE_COOLDOWN, cooldown - 1);
        }

        // Decrementa o timer da habilidade ativa
        int timer = data.getIntOr(NBT_ICE_TIMER, 0);
        if (timer > 0) {
            timer--;
            data.putInt(NBT_ICE_TIMER, timer);

            if (timer == 0) {
                removeInfiniteIceItem(player);
                player.level().playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.sendSystemMessage(Component.translatable("gui.playerstats.ice_ability_expired")
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            // Se o timer expirou ou está inativo, garante que nenhum item de gelo infinito permaneça
            removeInfiniteIceItem(player);
        }
    }

    // Mapa para controlar os blocos de gelo colocados temporariamente por dimensão
    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> iceBlocks = new HashMap<>();
    private static MinecraftServer currentServer = null;
    private static boolean loadedFromDisk = false;
    private static boolean isDirty = false;

    private static Path getSaveFilePath(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve("playerstats_infinite_ice.dat");
    }

    private static synchronized void checkServerSwitch(ServerLevel level) {
        MinecraftServer server = level.getServer();
        if (currentServer != server) {
            currentServer = server;
            iceBlocks.clear();
            loadedFromDisk = false;
            isDirty = false;
        }
    }

    private static synchronized void saveToDisk(ServerLevel level) {
        if (!isDirty) return;
        try {
            Path path = getSaveFilePath(level);
            boolean allEmpty = true;
            for (var m : iceBlocks.values()) {
                if (!m.isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
            if (allEmpty) {
                Files.deleteIfExists(path);
                isDirty = false;
                return;
            }
            Files.createDirectories(path.getParent());
            Path tempPath = path.resolveSibling("playerstats_infinite_ice.tmp");
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(tempPath)))) {
                dos.writeInt(1); // version
                int total = 0;
                for (var m : iceBlocks.values()) {
                    total += m.size();
                }
                dos.writeInt(total);
                for (var dimEntry : iceBlocks.entrySet()) {
                    String dimKey = dimEntry.getKey().identifier().toString();
                    for (var blockEntry : dimEntry.getValue().entrySet()) {
                        dos.writeUTF(dimKey);
                        BlockPos pos = blockEntry.getKey();
                        dos.writeInt(pos.getX());
                        dos.writeInt(pos.getY());
                        dos.writeInt(pos.getZ());
                        dos.writeLong(blockEntry.getValue());
                    }
                }
            }
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            isDirty = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static synchronized void loadFromDisk(ServerLevel level) {
        checkServerSwitch(level);
        if (loadedFromDisk) return;
        loadedFromDisk = true;
        try {
            Path path = getSaveFilePath(level);
            if (!Files.exists(path)) return;
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
                int version = dis.readInt();
                int count = dis.readInt();
                for (int i = 0; i < count; i++) {
                    String dimKey = dis.readUTF();
                    int x = dis.readInt();
                    int y = dis.readInt();
                    int z = dis.readInt();
                    long expireTick = dis.readLong();
                    Identifier loc = Identifier.tryParse(dimKey);
                    if (loc != null) {
                        ResourceKey<Level> resKey = ResourceKey.create(Registries.DIMENSION, loc);
                        iceBlocks.computeIfAbsent(resKey, k -> new HashMap<>()).put(new BlockPos(x, y, z), expireTick);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerPlacedIce(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        if (level instanceof ServerLevel serverLevel) {
            loadFromDisk(serverLevel);
        }
        long expireTick = level.getGameTime() + 200; // 10 segundos para sumir
        iceBlocks.computeIfAbsent(level.dimension(), k -> new HashMap<>()).put(pos.immutable(), expireTick);
        isDirty = true;
        if (level instanceof ServerLevel serverLevel) {
            saveToDisk(serverLevel);
        }
    }

    public static void handleLevelTick(Level level) {
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        loadFromDisk(serverLevel);

        var map = iceBlocks.get(level.dimension());
        if (map == null || map.isEmpty()) {
            if (isDirty) {
                saveToDisk(serverLevel);
            }
            return;
        }

        long currentTime = level.getGameTime();
        var iterator = map.entrySet().iterator();
        boolean changed = false;

        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (currentTime >= entry.getValue()) {
                BlockPos pos = entry.getKey();
                if (!level.isLoaded(pos)) {
                    continue; // Espera o chunk carregar para evaporar o gelo
                }
                var state = level.getBlockState(pos);
                if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    serverLevel.sendParticles(
                            ParticleTypes.POOF,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            5, 0.2, 0.2, 0.2, 0.05
                    );
                }
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            isDirty = true;
            saveToDisk(serverLevel);
        }
    }

    public static boolean handleBlockBreak(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            return false;
        }
        if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
            if (level instanceof ServerLevel serverLevel) {
                loadFromDisk(serverLevel);
            }
            var map = iceBlocks.get(level.dimension());
            if (map != null && map.containsKey(pos)) {
                map.remove(pos);
                isDirty = true;
                if (level instanceof ServerLevel serverLevel) {
                    saveToDisk(serverLevel);
                    serverLevel.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
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

    public static boolean stashHeldItem(Player player) {
        Inventory inventory = player.getInventory();
        int selected = inventory.getSelectedSlot();
        ItemStack held = inventory.getItem(selected);
        if (held.isEmpty()) {
            return true;
        }

        ItemStack toStash = held.copy();
        int remaining = toStash.getCount();
        int invSize = Inventory.INVENTORY_SIZE;

        // 1. Calcular espaço disponível em pilhas existentes compatíveis (exceto o slot selecionado)
        if (toStash.isStackable()) {
            for (int i = 0; i < invSize; i++) {
                if (i == selected) continue;
                ItemStack slotStack = inventory.getItem(i);
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, toStash)) {
                    int space = slotStack.getMaxStackSize() - slotStack.getCount();
                    if (space > 0) {
                        remaining -= space;
                        if (remaining <= 0) {
                            break;
                        }
                    }
                }
            }
        }

        // 2. Calcular espaço em slots vazios (exceto o slot selecionado)
        if (remaining > 0) {
            for (int i = 0; i < invSize; i++) {
                if (i == selected) continue;
                ItemStack slotStack = inventory.getItem(i);
                if (slotStack.isEmpty()) {
                    int space = Math.min(inventory.getMaxStackSize(), toStash.getMaxStackSize());
                    remaining -= space;
                    if (remaining <= 0) {
                        break;
                    }
                }
            }
        }

        // Se não houver espaço suficiente para guardar o item inteiro, aborta sem alterar nada
        if (remaining > 0) {
            return false;
        }

        // Espaço garantido! Executa a transferência do item
        ItemStack movingStack = held.copy();

        // Passo A: Mesclar com pilhas existentes
        if (movingStack.isStackable()) {
            for (int i = 0; i < invSize; i++) {
                if (i == selected) continue;
                ItemStack slotStack = inventory.getItem(i);
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, movingStack)) {
                    int space = slotStack.getMaxStackSize() - slotStack.getCount();
                    if (space > 0) {
                        int toMove = Math.min(movingStack.getCount(), space);
                        slotStack.grow(toMove);
                        movingStack.shrink(toMove);
                        if (movingStack.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        }

        // Passo B: Alocar o restante em slots vazios
        if (!movingStack.isEmpty()) {
            for (int i = 0; i < invSize; i++) {
                if (i == selected) continue;
                ItemStack slotStack = inventory.getItem(i);
                if (slotStack.isEmpty()) {
                    int space = Math.min(inventory.getMaxStackSize(), movingStack.getMaxStackSize());
                    int toMove = Math.min(movingStack.getCount(), space);
                    inventory.setItem(i, movingStack.copyWithCount(toMove));
                    movingStack.shrink(toMove);
                    if (movingStack.isEmpty()) {
                        break;
                    }
                }
            }
        }

        // Passo C: Limpar o slot da mão principal
        inventory.setItem(selected, ItemStack.EMPTY);
        return true;
    }

    public static boolean isAbilityActive(Player player) {
        CompoundTag data = Services.PLATFORM.getPersistentData(player);
        return data.getIntOr(NBT_ICE_TIMER, 0) > 0;
    }

    public static boolean isInfiniteIce(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.INFINITE_ICE.get());
    }

    public static void removeInfiniteIceItem(Player player) {
        Inventory inv = player.getInventory();
        boolean changed = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(ModItems.INFINITE_ICE.get())) {
                inv.setItem(i, ItemStack.EMPTY);
                changed = true;
            }
        }
        if (player.containerMenu != null) {
            ItemStack carried = player.containerMenu.getCarried();
            if (!carried.isEmpty() && carried.is(ModItems.INFINITE_ICE.get())) {
                player.containerMenu.setCarried(ItemStack.EMPTY);
                changed = true;
            }
        }
        if (changed) {
            player.containerMenu.broadcastChanges();
        }
    }
}
