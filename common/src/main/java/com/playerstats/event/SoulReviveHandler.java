package com.playerstats.event;

import com.playerstats.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SoulReviveHandler {

    public static void handleLevelTick(Level level) {
        if (level.isClientSide()) return;
        if (level.getGameTime() % 40 != 0) return;

        for (var player : level.players()) {
            BlockPos pPos = player.blockPosition();
            AABB box = new AABB(
                    pPos.getX() - 8, pPos.getY() - 4, pPos.getZ() - 8,
                    pPos.getX() + 8, pPos.getY() + 4, pPos.getZ() + 8
            );

            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box);

            Map<BlockPos, ItemEntity> stones = new HashMap<>();
            for (ItemEntity ie : items) {
                ItemStack stack = ie.getItem();
                if (stack != null && stack.getItem() == ModItems.SOUL_STONE.get()) {
                    stones.put(ie.blockPosition(), ie);
                }
            }

            if (stones.isEmpty()) return;

            for (ItemEntity fragmentEntity : items) {
                ItemStack fragStack = fragmentEntity.getItem();
                if (fragStack.getItem() != ModItems.SOUL_FRAGMENT.get()) continue;
                BlockPos pos = fragmentEntity.blockPosition();

                BlockState state = level.getBlockState(pos.below());
                if (state.is(Blocks.RESPAWN_ANCHOR) && !(state.getValue(RespawnAnchorBlock.CHARGE) > 0)) return;

                ItemEntity stoneEntity = stones.get(pos);
                if (stoneEntity == null) continue;

                CompoundTag tag = fragStack.getTag();
                if (tag == null || !tag.contains("StoredEntity")) continue;

                CompoundTag storedEntityTag = tag.getCompound("StoredEntity");
                if (!storedEntityTag.contains("id")) continue;

                String entityId = storedEntityTag.getString("id");
                if (entityId.isEmpty()) continue;

                List<LivingEntity> candidates;

                if (entityId.equals("minecraft:player")) {
                    candidates = level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(pos).inflate(1.5),
                            e -> e.getType() == EntityType.VILLAGER);
                } else {
                    candidates = level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(pos).inflate(1.5),
                            e -> {
                                ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
                                return key != null && entityId.equals(key.toString());
                            });
                }

                if (candidates.isEmpty()) continue;

                LivingEntity sacrifice = candidates.get(0);
                ServerLevel server = (ServerLevel) level;
                Entity created = null;

                if (entityId.equals("minecraft:player")) {
                    String uuidStr = storedEntityTag.getString("UUID");
                    if (uuidStr != null && !uuidStr.isEmpty()) {
                        UUID uuid = UUID.fromString(uuidStr);
                        ServerPlayer revivedPlayer = server.getServer().getPlayerList().getPlayer(uuid);
                        if (revivedPlayer == null || revivedPlayer.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) continue;
                        created = revivedPlayer;
                    }
                } else {
                    ResourceLocation id = ResourceLocation.tryParse(entityId);
                    EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
                    if (type != null) {
                        created = type.create(level);
                    }
                }

                if (created == null) continue;

                server.sendParticles(ParticleTypes.SOUL,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        40, 0.5, 0.5, 0.5, 0.05);

                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                    lightning.setVisualOnly(true);
                    level.addFreshEntity(lightning);
                }

                level.explode(null, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                        2.0F, Level.ExplosionInteraction.BLOCK);

                if (created instanceof ServerPlayer revivedPlayer) {
                    revivedPlayer.setGameMode(GameType.SURVIVAL);
                    revivedPlayer.load(storedEntityTag);
                    revivedPlayer.getFoodData().setSaturation(0.0F);
                    revivedPlayer.getFoodData().setFoodLevel(3);

                    revivedPlayer.getAbilities().flying = false;
                    revivedPlayer.getAbilities().mayfly = false;
                    revivedPlayer.onUpdateAbilities();

                    revivedPlayer.fallDistance = 0.0F;

                    BlockPos safePos = pos.above();
                    revivedPlayer.teleportTo(server,
                            safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                            level.random.nextFloat() * 360f, 0f);

                    revivedPlayer.connection.teleport(
                            safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                            level.random.nextFloat() * 360f, 0f);

                    int duration = 10 * 60 * 20;
                    revivedPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1));
                    revivedPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
                    revivedPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
                    revivedPlayer.setHealth(1);
                    revivedPlayer.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
                } else if (created instanceof LivingEntity revived) {
                    revived.load(storedEntityTag);
                    revived.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            level.random.nextFloat() * 360f, 0f);
                    revived.setHealth(1);
                    level.addFreshEntity(revived);
                    revived.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
                }

                fragmentEntity.discard();
                stoneEntity.discard();
                sacrifice.kill();
                level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setHealth(1.0F);
                    serverPlayer.getFoodData().setSaturation(0.0F);
                    serverPlayer.getFoodData().setFoodLevel(3);

                    int duration = 10 * 60 * 20;
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
                }
            }
        }
    }
}
