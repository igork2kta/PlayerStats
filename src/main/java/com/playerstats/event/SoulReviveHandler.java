package com.playerstats.event;

import com.mojang.authlib.GameProfile;
import com.playerstats.items.ModItems;
import com.playerstats.util.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
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
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "playerstats")
public class SoulReviveHandler {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {

        Level level = event.getLevel();
        if (level.isClientSide()) return;

        if (level.getGameTime() % 40 != 0) return;


        //valida se hÃ¡ ritual perto de players
        for (var player : level.players()) {
            BlockPos pPos = player.blockPosition();
            AABB box = new AABB(
                    pPos.getX() - 8, pPos.getY() - 4, pPos.getZ() - 8,
                    pPos.getX() + 8, pPos.getY() + 4, pPos.getZ() + 8
            );

            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box);

            // Mapa rÃ¡pido para localizar Soul Stone por posiÃ§Ã£o
            Map<BlockPos, ItemEntity> stones = new HashMap<>();
            for (ItemEntity ie : items) {
                ItemStack stack = ie.getItem();
                if (stack != null && stack.getItem() == ModItems.SOUL_STONE.get()) {
                    stones.put(ie.blockPosition(), ie);
                }
            }

            //Se nÃ£o achar nenhuma Soul Stone, nÃ£o da para completar o ritual.
            if(stones.isEmpty()) return;

            for (ItemEntity fragmentEntity : items) {
                ItemStack fragStack = fragmentEntity.getItem();
                if (fragStack.getItem() != ModItems.SOUL_FRAGMENT.get()) continue;
                BlockPos pos = fragmentEntity.blockPosition();

                BlockState state = level.getBlockState(pos.below());
                // Precisa ter uma respawn anchor carregada abaixo
                if (state.is(Blocks.RESPAWN_ANCHOR) && !(state.getValue(RespawnAnchorBlock.CHARGE) > 0)) return;

                // verifica se existe uma Soul Stone na mesma posiÃ§Ã£o
                ItemEntity stoneEntity = stones.get(pos);
                if (stoneEntity == null) continue;

                // pega o StoredEntity
                CompoundTag storedEntityTag = fragStack.get(ModDataComponents.STORED_ENTITY);
                if (storedEntityTag == null) continue;
                //CompoundTag storedEntityTag = tag.getCompound("StoredEntity");


                if (!storedEntityTag.contains("id")) continue;

                String entityId = storedEntityTag.getStringOr("id", "");
                if (entityId.isEmpty()) continue;

                List<LivingEntity> candidates;

                if (entityId.equals("minecraft:player")) {
                    // Para reviver player, sacrifÃ­cio deve ser um villager
                    candidates = level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(pos).inflate(1.5),
                            e -> e.getType() == EntityType.VILLAGER);
                } else {
                    // Para mobs, sacrifÃ­cio deve ser do mesmo tipo
                    candidates = level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(pos).inflate(1.5),
                            e -> {
                                Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
                                return key != null && entityId.equals(key.toString());
                            });
                }

                if (candidates.isEmpty()) continue;

                LivingEntity sacrifice = candidates.get(0);

                ServerLevel server = (ServerLevel) level;
                Entity created = null;

                //Revival de player, valido para hardcore
                if (entityId.equals("minecraft:player")) {

                    String uuidStr = storedEntityTag.getStringOr("UUID", "");
                    if (uuidStr != null && !uuidStr.isEmpty()) {
                        UUID uuid = UUID.fromString(uuidStr);
                        ServerPlayer revivedPlayer = server.getServer().getPlayerList().getPlayer(uuid);

                        //NÃ£o deixa reviver se o player estiver na tela de respawn, pois buga
                        if (revivedPlayer == null || revivedPlayer.gameMode.getGameModeForPlayer() != GameType.SPECTATOR ) continue;
                        created = revivedPlayer;
                    }
                }
                // revival normal de mobs
                else {

                    Identifier id = Identifier.tryParse(entityId);
                    EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
                    created = type.create(level, EntitySpawnReason.EVENT);
                }

                //Efeitos dramÃ¡ticos
                {
                    server.sendParticles(ParticleTypes.SOUL,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            40, 0.5, 0.5, 0.5, 0.05);

                    // Raio visual sem dano
                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
                    if (lightning != null) {
                        lightning.snapTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                        lightning.setVisualOnly(true); // nÃ£o causa dano
                        level.addFreshEntity(lightning);
                    }

                    level.explode(null, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                            2.0F, Level.ExplosionInteraction.BLOCK);
                }


                //Isso precisa estar depois da explosÃ£o, senÃ£o o defunto morre dnv
                if ((created instanceof ServerPlayer revivedPlayer)) {

                    // Precisa passar para survival para reviver no hardcore
                    revivedPlayer.setGameMode(GameType.SURVIVAL);

                    // Restaura os dados persistentes
                    revivedPlayer.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), storedEntityTag));
                    revivedPlayer.getFoodData().setSaturation(0.0F);
                    revivedPlayer.getFoodData().setFoodLevel(3);

                    // Garante que nÃ£o esteja marcado como voando
                    revivedPlayer.getAbilities().flying = false;
                    revivedPlayer.getAbilities().mayfly = false;
                    revivedPlayer.onUpdateAbilities();

                    // Zera a distÃ¢ncia de queda
                    revivedPlayer.fallDistance = 0.0F;

                    // ForÃ§a posiÃ§Ã£o vÃ¡lida em cima do bloco sÃ³lido
                    BlockPos safePos = pos.above(); // garante que estÃ¡ acima da Ã¢ncora
                    revivedPlayer.teleportTo(server, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, Set.of(), level.getRandom().nextFloat() * 360f, 0f, true);

                    // ForÃ§a sincronizaÃ§Ã£o de posiÃ§Ã£o com o cliente
                    revivedPlayer.connection.teleport(
                            safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                            level.getRandom().nextFloat() * 360f, 0f);

                    int duration = 10 * 60 * 20; // 10 minutos
                    revivedPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1));
                    revivedPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
                    revivedPlayer.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 1));
                    revivedPlayer.setHealth(1);
                    revivedPlayer.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
                }
                else if((created instanceof LivingEntity revived)){

                    // Restaura os dados persistentes
                    revived.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), storedEntityTag));
                    // Teleporta para o ritual
                    revived.snapTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            level.getRandom().nextFloat() * 360f, 0f);
                    revived.setHealth(1);
                    level.addFreshEntity(revived);
                    revived.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);

                }


                // consome fragment e stone e mata a oferenda
                fragmentEntity.discard();
                stoneEntity.discard();
                sacrifice.kill(server);
                // remove a Ã¢ncora
                level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());

                //Aplicar efeitos negativos no conjurador
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setHealth(1.0F);
                    serverPlayer.getFoodData().setSaturation(0.0F);
                    serverPlayer.getFoodData().setFoodLevel(3);

                    int duration = 10 * 60 * 20;
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 1));
                }
            }
        }

    }
}
