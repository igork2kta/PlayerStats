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
        if (!tag.contains("Attributes", 9)) return false;
        ListTag attributes = tag.getList("Attributes", 10);

        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attr = attributes.getCompound(i);
            if ("playerstats:scout_vision".equals(attr.getString("Name")) && attr.contains("Modifiers", 9)) {
                ListTag mods = attr.getList("Modifiers", 10);
                if (!mods.isEmpty()) {
                    CompoundTag mod = mods.getCompound(0);
                    double amount = mod.getDouble("Amount");
                    if (-1 + amount == 1.0) { // lógica original mantida
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
