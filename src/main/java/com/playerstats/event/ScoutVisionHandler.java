package com.playerstats.event;

import com.playerstats.ModAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
@Mod.EventBusSubscriber(modid = "playerstats")
public class ScoutVisionHandler {


    @SubscribeEvent
    public static void onParrotTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Level level = event.level;
        if (level.isClientSide()) return;

        // Executa a cada 20 ticks (1 segundo) para balancear responsividade e performance
        if (level.getGameTime() % 20 != 0) return;

        double radius = 16.0D;

        for (var player : level.players()) {
            int shoulderLvl = getShoulderScoutVisionLevel(player);

            // Papagaios soltos com Scout Vision
            List<Parrot> parrots = level.getEntitiesOfClass(Parrot.class,
                    player.getBoundingBox().inflate(32.0D),
                    g -> {
                        var attr = g.getAttribute(ModAttributes.SCOUT_VISION.get());
                        return g.isAlive() && attr != null && attr.getValue() > 0.0D;
                    });

            if (parrots.isEmpty() && shoulderLvl <= 0) continue;

            // Processa papagaio no ombro
            if (shoulderLvl > 0) {
                double shoulderRadius = 16.0D + ((shoulderLvl - 1) * 8.0D); // Nv 1: 16b, Nv 2: 24b, Nv 3: 32b
                AABB visionBox = player.getBoundingBox().inflate(shoulderRadius);
                List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class,
                        visionBox,
                        e -> e instanceof Monster || e instanceof Slime);
                for (LivingEntity mob : mobs) {
                    mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
                }
            }

            // Processa papagaios soltos
            for (Parrot parrot : parrots) {
                int lvl = (int) Math.round(parrot.getAttribute(ModAttributes.SCOUT_VISION.get()).getValue());
                double parrotRadius = 16.0D + ((lvl - 1) * 8.0D);
                AABB visionBox = parrot.getBoundingBox().inflate(parrotRadius);

                List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class,
                        visionBox,
                        e -> e instanceof Monster || e instanceof Slime);

                for (LivingEntity mob : mobs) {
                    mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
                }
            }
        }
    }

    /** Retorna o nível de Scout Vision do papagaio no ombro (ou 0 se não tiver) */
    private static int getShoulderScoutVisionLevel(Player player) {
        int left = getShoulderLevel(player, true);
        int right = getShoulderLevel(player, false);
        return Math.max(left, right);
    }

    private static int getShoulderLevel(Player player, boolean leftShoulder) {
        CompoundTag tag = leftShoulder ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        if (tag == null || tag.isEmpty()) return 0;
        if (!"minecraft:parrot".equals(tag.getString("id"))) return 0;

        String attrKey = tag.contains("Attributes", 9) ? "Attributes" : (tag.contains("attributes", 9) ? "attributes" : null);
        if (attrKey == null) return 0;

        ListTag attributes = tag.getList(attrKey, 10);

        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attr = attributes.getCompound(i);
            String id = attr.contains("Name") ? attr.getString("Name") : attr.getString("id");

            if ("playerstats:scout_vision".equals(id) || "attribute.playerstats.scout_vision".equals(id)) {
                double base = attr.contains("Base") ? attr.getDouble("Base") : (attr.contains("base") ? attr.getDouble("base") : -1.0D);
                double total = base;

                String modKey = attr.contains("Modifiers", 9) ? "Modifiers" : (attr.contains("modifiers", 9) ? "modifiers" : null);
                if (modKey != null) {
                    ListTag mods = attr.getList(modKey, 10);
                    for (int j = 0; j < mods.size(); j++) {
                        CompoundTag mod = mods.getCompound(j);
                        double amount = mod.contains("Amount") ? mod.getDouble("Amount") : mod.getDouble("amount");
                        total += amount;
                    }
                }

                if (total > 0.0D) {
                    return (int) Math.round(total);
                }
            }
        }
        return 0;
    }

}
