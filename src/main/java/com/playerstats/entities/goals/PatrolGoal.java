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

import java.util.List;

public class PatrolGoal extends Goal {
    private final Mob mob;
    private final double speed;
    private final double radius;
    private BlockPos patrolCenter;
    private int cooldown;

    public PatrolGoal(Mob mob, double speed, double radius) {
        this.mob = mob;
        this.speed = speed;
        this.radius = radius;
    }

    @Override
    public boolean canUse() {
        return AttributeUtils.getAttributeValue(mob, ModAttributes.PATROL) == 1;
    }

    @Override
    public void start() {
        patrolCenter = new BlockPos(
                mob.getPersistentData().getInt("PatrolX"),
                mob.getPersistentData().getInt("PatrolY"),
                mob.getPersistentData().getInt("PatrolZ")
        );
    }

    @Override
    public void tick() {
        Level level = mob.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        // A cada X ticks, anda para um ponto aleatório dentro do raio
        if (--cooldown <= 0) {
            cooldown = 60; // 3 segundos
            double angle = mob.getRandom().nextDouble() * Math.PI * 2;
            int dx = (int) (patrolCenter.getX() + (Math.cos(angle) * radius));
            int dz = (int) (patrolCenter.getZ() + (Math.sin(angle) * radius));
            BlockPos target = new BlockPos(dx, patrolCenter.getY(), dz);

            mob.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), speed);
        }

        // Verifica inimigos próximos
        List<Monster> hostiles = serverLevel.getEntitiesOfClass(Monster.class,
                new AABB(patrolCenter).inflate(radius),
                Entity::isAlive);

        if (!hostiles.isEmpty()) {
            //if(hostiles.get(0) instanceof Creeper)
            mob.setTarget(hostiles.get(0)); // Ataca o primeiro inimigo detectado
        }
    }
}
