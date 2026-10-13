package com.playerstats.entities.goals;

import com.playerstats.ModAttributes;
import com.playerstats.util.AttributeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

public class FollowOwnerGoal extends Goal {

    private final Mob mob;
    private Player owner;

    // parâmetros fixos
    private final double speed = 1.2D;
    private final float minDist = 6.0F;
    private final float maxDist = 15.0F;

    private int timeToRecalcPath;

    public FollowOwnerGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() != null && mob.getTarget().isAlive()) {
            return false; // se já está atacando alguém, não segue
        }

        if (!mob.getPersistentData().hasUUID("Owner")) {
            return false;
        }

        if (AttributeUtils.getAttributeValue(mob, ModAttributes.FOLLOW_OWNER) < 1.0D) {
            return false;
        }

        // Se a patrulha estiver ativa, o mob não segue o dono nem teleporta
        if (AttributeUtils.getAttributeValue(mob, ModAttributes.PATROL) == 1.0D) {
            return false;
        }

        UUID ownerId = mob.getPersistentData().getUUID("Owner");
        owner = mob.level().getPlayerByUUID(ownerId);

        return owner != null
                && !owner.isSpectator()
                && !mob.isPassenger()
                && !mob.isLeashed()
                && mob.distanceTo(owner) > minDist;
    }

    @Override
    public boolean canContinueToUse() {
        if (mob.getTarget() != null && mob.getTarget().isAlive()) {
            return false; // interrompe enquanto luta
        }

        if (AttributeUtils.getAttributeValue(mob, ModAttributes.FOLLOW_OWNER) < 1.0D) {
            return false;
        }

        // Se a patrulha estiver ativa, interrompe imediatamente o seguimento e teleporte
        if (AttributeUtils.getAttributeValue(mob, ModAttributes.PATROL) == 1.0D) {
            return false;
        }

        return owner != null
                && !owner.isSpectator()
                && !mob.isPassenger()
                && !mob.isLeashed()
                && mob.distanceTo(owner) > minDist;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        mob.getLookControl().setLookAt(owner, 10.0F, (float) mob.getMaxHeadXRot());

        // Recalcula rota a cada 10 ticks (0.5s) em vez de todo tick
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);

            double distance = mob.distanceTo(owner);

            // Teleporta se muito longe
            if (distance > 12.0 && AttributeUtils.getAttributeValue(mob, ModAttributes.TELEPORT_TO_OWNER) == 1.0D) {
                teleportToOwner();
            } else {
                mob.getNavigation().moveTo(owner, speed);
            }
        }
    }

    private void teleportToOwner() {

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy >= -2; dy--) {
                    double targetX = owner.getX() + dx + 0.5D;
                    double targetY = owner.getY() + dy;
                    double targetZ = owner.getZ() + dz + 0.5D;
                    BlockPos pos = BlockPos.containing(targetX, targetY, targetZ);
                    BlockPos below = pos.below();

                    // Verifica se o bloco de baixo é sólido e não é perigoso (lava/fogo/etc)
                    if (!mob.level().getBlockState(below).entityCanStandOn(mob.level(), below, mob)) {
                        continue;
                    }

                    // Verifica se a bounding box completa do mob nesta posição não colide com blocos ou fluidos
                    var boundingBox = mob.getDimensions(mob.getPose()).makeBoundingBox(targetX, targetY, targetZ);
                    if (mob.level().noCollision(mob, boundingBox)) {
                        mob.moveTo(targetX, targetY, targetZ, mob.getYRot(), mob.getXRot());
                        mob.getNavigation().stop();
                        return;
                    }
                }
            }
        }
    }
}
