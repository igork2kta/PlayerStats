package com.playerstats.event;

import com.mojang.authlib.GameProfile;
import com.playerstats.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "playerstats")
public class SoulReviveHandler {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Level level = event.level;
        if (level.isClientSide()) return;

        tickCounter++;
        if (tickCounter % 40 != 0) return; // 20 ticks = 1 segundo

        //valida se há ritual perto de players
        for (var player : level.players()) {
            BlockPos pPos = player.blockPosition();
            AABB box = new AABB(
                    pPos.getX() - 8, pPos.getY() - 4, pPos.getZ() - 8,
                    pPos.getX() + 8, pPos.getY() + 4, pPos.getZ() + 8
            );

            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box);

            // Mapa rápido para localizar Soul Stone por posição
            Map<BlockPos, ItemEntity> stones = new HashMap<>();
            for (ItemEntity ie : items) {
                ItemStack stack = ie.getItem();
                if (stack != null && stack.getItem() == ModItems.SOUL_STONE.get()) {
                    stones.put(ie.blockPosition(), ie);
                }
            }

            //Se não achar nenhuma Soul Stone, não da para completar o ritual.
            if(stones.isEmpty()) return;

            for (ItemEntity fragmentEntity : items) {
                ItemStack fragStack = fragmentEntity.getItem();
                if (fragStack.getItem() != ModItems.SOUL_FRAGMENT.get()) continue;

                BlockPos pos = fragmentEntity.blockPosition();

                BlockState state = level.getBlockState(pos.below());
                // Precisa ter uma respawn anchor carregada abaixo
                if (state.is(Blocks.RESPAWN_ANCHOR) && !(state.getValue(RespawnAnchorBlock.CHARGE) > 0)) return;

                // verifica se existe uma Soul Stone na mesma posição
                ItemEntity stoneEntity = stones.get(pos);
                if (stoneEntity == null) continue;

                // pega o StoredEntity
                CompoundTag tag = fragStack.getTag();
                if (tag == null || !tag.contains("StoredEntity")) continue;
                CompoundTag storedEntityTag = tag.getCompound("StoredEntity");


                if (!storedEntityTag.contains("id")) continue;
                String entityId = storedEntityTag.getString("id");
                if (entityId.isEmpty()) continue;

                List<LivingEntity> candidates;

                if (entityId.equals("minecraft:player")) {
                    // Para reviver player, sacrifício deve ser um villager
                    candidates = level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(pos).inflate(1.5),
                            e -> e.getType() == EntityType.VILLAGER);
                } else {
                    // Para mobs, sacrifício deve ser do mesmo tipo
                    candidates = level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(pos).inflate(1.5),
                            e -> {
                                ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
                                return key != null && entityId.equals(key.toString());
                            });
                }

                if (candidates.isEmpty()) continue;

                LivingEntity sacrifice = candidates.get(0);

                ServerLevel server = (ServerLevel) level;

                //Revival de player, valido para hardcore
                if (entityId.equals("minecraft:player")) {

                    String uuidStr = storedEntityTag.getString("UUID");
                    if (uuidStr != null && !uuidStr.isEmpty()) {
                        UUID uuid = UUID.fromString(uuidStr);
                        ServerPlayer revivedPlayer = server.getServer().getPlayerList().getPlayer(uuid);

                        //Não deixa reviver se o player estiver na tela de respawn, pois buga
                        if (revivedPlayer == null || revivedPlayer.gameMode.getGameModeForPlayer() != GameType.SPECTATOR ) continue;

                        // Restaura os dados persistentes
                        revivedPlayer.load(storedEntityTag);

                        // Teleporta para o ritual
                        revivedPlayer.teleportTo(server,
                                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                                level.random.nextFloat() * 360f, 0f);

                        revivedPlayer.setHealth(1);
                        revivedPlayer.getFoodData().setFoodLevel(3);
                        revivedPlayer.getFoodData().setSaturation(0.0F);
                        //revivedPlayer.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK); // limpa estado morto
                        revivedPlayer.setGameMode(GameType.SURVIVAL);

                        int duration = 10 * 60 * 20; // 10 minutos
                        revivedPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1));
                        revivedPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
                        revivedPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
                        revivedPlayer.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
                    }



                }
                // revival normal de mobs
                else {

                    EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(entityId));
                    if (type == null) continue;

                    Entity created = type.create(level);
                    if (!(created instanceof LivingEntity revived)) continue;

                    revived.load(storedEntityTag);
                    revived.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            level.random.nextFloat() * 360f, 0f);
                    revived.setHealth(1);
                    level.addFreshEntity(revived);
                    revived.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);

                }

                //Efeitos dramáticos
                {
                    server.sendParticles(ParticleTypes.SOUL,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            40, 0.5, 0.5, 0.5, 0.05);

                    // Raio visual sem dano
                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                    if (lightning != null) {
                        lightning.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                        lightning.setVisualOnly(true); // não causa dano
                        level.addFreshEntity(lightning);
                    }

                    level.explode(null, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                            2.0F, Level.ExplosionInteraction.BLOCK);
                }

                // consome fragment e stone e mata a oferenda
                fragmentEntity.discard();
                stoneEntity.discard();
                sacrifice.kill();
                // remove a âncora
                level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());

                //Aplicar efeitos negativos no conjurador
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setHealth(1.0F);
                    serverPlayer.getFoodData().setFoodLevel(0);
                    serverPlayer.getFoodData().setSaturation(0.0F);

                    int duration = 10 * 60 * 20;
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
                }
            }
        }

    }
}
