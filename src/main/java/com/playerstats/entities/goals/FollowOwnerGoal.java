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

    public FollowOwnerGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() != null && mob.getTarget().isAlive()) {
            return false; // se já está atacando alguém, não segue
        }

        try {
            UUID ownerId = mob.getPersistentData().getUUID("Owner");
            if (ownerId == null) return false;
            owner = mob.level().getPlayerByUUID(ownerId);

            return owner != null
                    && !mob.isPassenger()
                    && !mob.isLeashed()
                    && mob.distanceTo(owner) > minDist;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (mob.getTarget() != null && mob.getTarget().isAlive()) {
            return false; // interrompe enquanto luta
        }

        return owner != null
                && mob.distanceTo(owner) > maxDist
                && !mob.isPassenger()
                && !mob.isLeashed();
    }


    @Override
    public void tick() {
        if (owner == null) return;


        if (AttributeUtils.getAttributeValue(mob, ModAttributes.FOLLOW_OWNER) < 1) return;

        double distance = mob.distanceTo(owner);

        // Teleporta se muito longe
        if (distance > 12.0 && AttributeUtils.getAttributeValue(mob, ModAttributes.TELEPORT_TO_OWNER) == 1) {
            teleportToOwner();
        } else {
            mob.getNavigation().moveTo(owner, speed);
        }
    }

    private void teleportToOwner() {
        if (owner == null) return;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy >= -2; dy--) {
                    Vec3 target = owner.position().add(dx, dy, dz);
                    BlockPos pos = BlockPos.containing(target);
                    BlockPos below = pos.below();

                    if (mob.level().getBlockState(pos).getCollisionShape(mob.level(), pos).isEmpty()
                            && mob.level().getBlockState(below).isSolidRender(mob.level(), below)) {

                        mob.setPos(target.x, target.y, target.z);
                        mob.getNavigation().stop();
                        return;
                    }
                }
            }
        }
    }
}
