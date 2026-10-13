package com.playerstats.entities.goals;

import com.playerstats.ModAttributes;
import com.playerstats.util.AttributeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class PatrolGoal extends Goal {
    private final Mob mob;
    private final double speed;
    private final double radius;
    private BlockPos patrolCenter;
    private int cooldown;
    private int scanCooldown;

    public PatrolGoal(Mob mob, double speed, double radius) {
        this.mob = mob;
        this.speed = speed;
        this.radius = radius;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() != null && mob.getTarget().isAlive()) {
            return false;
        }
        return AttributeUtils.getAttributeValue(mob, ModAttributes.PATROL) == 1.0D;
    }
    public void start() {
        this.cooldown = 0;
        this.scanCooldown = 0;
        patrolCenter = new BlockPos(
                mob.getPersistentData().getInt("PatrolX"),
                mob.getPersistentData().getInt("PatrolY"),
                mob.getPersistentData().getInt("PatrolZ")
        );
        if (patrolCenter.getX() == 0 && patrolCenter.getY() == 0 && patrolCenter.getZ() == 0) {
            patrolCenter = mob.blockPosition();
            mob.getPersistentData().putInt("PatrolX", patrolCenter.getX());
            mob.getPersistentData().putInt("PatrolY", patrolCenter.getY());
            mob.getPersistentData().putInt("PatrolZ", patrolCenter.getZ());
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        Level level = mob.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        // A cada 3 segundos (60 ticks), anda para um ponto aleatório dentro do raio
        if (--cooldown <= 0) {
            cooldown = 60;
            double angle = mob.getRandom().nextDouble() * Math.PI * 2;
            int dx = (int) (patrolCenter.getX() + (Math.cos(angle) * radius));
            int dz = (int) (patrolCenter.getZ() + (Math.sin(angle) * radius));
            BlockPos target = new BlockPos(dx, patrolCenter.getY(), dz);

            mob.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), speed);
        }

        // Verifica inimigos próximos 1 vez por segundo (a cada 20 ticks) em vez de todo tick
        if (--scanCooldown <= 0) {
            scanCooldown = 20;

            List<Monster> hostiles = serverLevel.getEntitiesOfClass(Monster.class,
                    new AABB(patrolCenter).inflate(radius),
                    Entity::isAlive);

            if (!hostiles.isEmpty()) {
                mob.setTarget(hostiles.get(0)); // Ataca o primeiro inimigo detectado
            }
        }
    }
}
