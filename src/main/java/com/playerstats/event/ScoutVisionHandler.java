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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

@EventBusSubscriber(modid = "playerstats")
public class ScoutVisionHandler {

    @SubscribeEvent
    public static void onParrotTick(LevelTickEvent.Post event) {

        Level level = event.getLevel();
        if (level.isClientSide()) return;

        // Executa a cada 20 ticks (1 segundo) para balancear responsividade e performance
        if (level.getGameTime() % 20 != 0) return;

        double radius = 16.0D;

        for (var player : level.players()) {

            // Papagaios soltos com Scout Vision
            List<Parrot> parrots = level.getEntitiesOfClass(Parrot.class,
                    player.getBoundingBox().inflate(radius),
                    g -> {
                        var attr = g.getAttribute(ModAttributes.SCOUT_VISION.getDelegate());
                        return g.isAlive() && attr != null && attr.getValue() == 1.0D;
                    });

            // Papagaios nos ombros (não há entidade, então usamos player como centro)
            boolean left = hasScoutVision(player, true);
            boolean right = hasScoutVision(player, false);

            if (left || right) {
                // cria um centro de visão no player
                parrots.add(null); // usamos null como “flag” para indicar Scout Vision no player
            }

            if (parrots.isEmpty()) continue;

            for (Parrot parrot : parrots) {
                AABB visionBox = (parrot != null)
                        ? parrot.getBoundingBox().inflate(radius)
                        : player.getBoundingBox().inflate(radius);

                List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class,
                        visionBox,
                        e -> e instanceof Monster || e instanceof Slime);

                for (LivingEntity mob : mobs) {
                    mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
                }
            }
        }
    }

    /** Verifica se o papagaio no ombro tem Scout Vision */
    private static boolean hasScoutVision(Player player, boolean leftShoulder) {
        CompoundTag tag = leftShoulder ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        if (tag == null || tag.isEmpty()) return false;
        if (!"minecraft:parrot".equals(tag.getString("id"))) return false;

        // No 1.21.1 a tag é "attributes" (minúsculo), mantendo suporte a "Attributes"
        String attrKey = tag.contains("attributes", 9) ? "attributes" : (tag.contains("Attributes", 9) ? "Attributes" : null);
        if (attrKey == null) return false;

        ListTag attributes = tag.getList(attrKey, 10);

        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attr = attributes.getCompound(i);
            // No 1.21.1 é "id", anteriormente era "Name"
            String id = attr.contains("id") ? attr.getString("id") : attr.getString("Name");

            if ("playerstats:scout_vision".equals(id) || "attribute.playerstats.scout_vision".equals(id)) {
                double base = attr.contains("base") ? attr.getDouble("base") : (attr.contains("Base") ? attr.getDouble("Base") : -1.0D);
                double total = base;

                String modKey = attr.contains("modifiers", 9) ? "modifiers" : (attr.contains("Modifiers", 9) ? "Modifiers" : null);
                if (modKey != null) {
                    ListTag mods = attr.getList(modKey, 10);
                    for (int j = 0; j < mods.size(); j++) {
                        CompoundTag mod = mods.getCompound(j);
                        double amount = mod.contains("amount") ? mod.getDouble("amount") : mod.getDouble("Amount");
                        total += amount;
                    }
                }

                if (total == 1.0D) {
                    return true;
                }
            }
        }
        return false;
    }
}
