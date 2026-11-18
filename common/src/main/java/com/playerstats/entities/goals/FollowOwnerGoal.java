package com.playerstats.entities.goals;

import com.playerstats.ModAttributes;
import com.playerstats.platform.Services;
import com.playerstats.util.AttributeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

public class FollowOwnerGoal extends Goal {

    private static final Identifier FOLLOW_RANGE_MODIFIER_ID = Identifier.fromNamespaceAndPath("playerstats", "follow_owner_range");

    private final Mob mob;
    private Player owner;

    // parâmetros de distância e velocidade
    private final double speed = 1.25D;
    private final float startDist = 6.0F;
    private final float stopDist = 3.0F;

    private int timeToRecalcPath;

    public FollowOwnerGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private UUID getOwnerId() {
        UUID ownerId = Services.PLATFORM.getPersistentData(mob).read("Owner", UUIDUtil.CODEC).orElse(null);
        if (ownerId != null) {
            return ownerId;
        }
        if (mob instanceof OwnableEntity ownable && ownable.getOwnerReference() != null) {
            ownerId = ownable.getOwnerReference().getUUID();
            if (ownerId != null) {
                Services.PLATFORM.getPersistentData(mob).store("Owner", UUIDUtil.CODEC, ownerId);
                return ownerId;
            }
        }
        return null;
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() != null && mob.getTarget().isAlive()) {
            return false; // se já está atacando alguém, não segue
        }

        if (mob.isPassenger() || mob.isVehicle()) {
            return false; // se estiver montado em algo ou sendo montado
        }

        if (mob.isLeashed()) {
            return false; // se estiver na coleira
        }

        if (AttributeUtils.getAttributeValue(mob, ModAttributes.FOLLOW_OWNER) < 1.0D) {
            return false;
        }

        // Se a patrulha estiver ativa, o mob não segue o dono nem teleporta
        if (AttributeUtils.getAttributeValue(mob, ModAttributes.PATROL) == 1.0D) {
            return false;
        }

        UUID ownerId = getOwnerId();
        if (ownerId == null) {
            return false;
        }

        owner = mob.level().getPlayerByUUID(ownerId);
        if (owner == null || !owner.isAlive() || owner.isSpectator()) {
            return false;
        }

        return mob.distanceTo(owner) > startDist;
    }

    @Override
    public boolean canContinueToUse() {
        if (mob.getTarget() != null && mob.getTarget().isAlive()) {
            return false;
        }

        if (mob.isPassenger() || mob.isVehicle()) {
            return false;
        }

        if (mob.isLeashed()) {
            return false;
        }

        if (AttributeUtils.getAttributeValue(mob, ModAttributes.FOLLOW_OWNER) < 1.0D) {
            return false;
        }

        if (AttributeUtils.getAttributeValue(mob, ModAttributes.PATROL) == 1.0D) {
            return false;
        }

        if (owner == null || !owner.isAlive() || owner.isSpectator()) {
            return false;
        }

        return mob.distanceTo(owner) > stopDist;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null && !followRange.hasModifier(FOLLOW_RANGE_MODIFIER_ID)) {
            followRange.addTransientModifier(new AttributeModifier(FOLLOW_RANGE_MODIFIER_ID, 32.0D, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
        AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null) {
            followRange.removeModifier(FOLLOW_RANGE_MODIFIER_ID);
        }
    }

    @Override
    public void tick() {
        if (owner == null) return;

        mob.getLookControl().setLookAt(owner, 10.0F, (float) mob.getMaxHeadXRot());

        // Se a navegação terminou precocemente mas ainda está longe, recalcula imediatamente
        if (mob.getNavigation().isDone() && mob.distanceTo(owner) > stopDist) {
            this.timeToRecalcPath = 0;
        }

        // Recalcula rota a cada 10 ticks (0.5s) em vez de todo tick
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);

            double distance = mob.distanceTo(owner);

            // Teleporta se muito longe
            if (distance > 12.0 && AttributeUtils.getAttributeValue(mob, ModAttributes.TELEPORT_TO_OWNER) == 1.0D) {
                if (!teleportToOwner()) {
                    mob.getNavigation().moveTo(owner, speed);
                }
            } else {
                mob.getNavigation().moveTo(owner, speed);
            }
        }
    }

    private boolean teleportToOwner() {
        BlockPos ownerPos = owner.blockPosition();

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                    continue; // Não teleporta na mesma posição do dono
                }
                for (int dy = 1; dy >= -2; dy--) {
                    int targetX = ownerPos.getX() + dx;
                    int targetY = ownerPos.getY() + dy;
                    int targetZ = ownerPos.getZ() + dz;

                    BlockPos pos = new BlockPos(targetX, targetY, targetZ);
                    BlockPos below = pos.below();

                    if (!mob.level().getBlockState(below).entityCanStandOn(mob.level(), below, mob)) {
                        continue;
                    }

                    double entityX = targetX + 0.5D;
                    double entityY = targetY;
                    double entityZ = targetZ + 0.5D;

                    var boundingBox = mob.getDimensions(mob.getPose()).makeBoundingBox(entityX, entityY, entityZ);
                    if (mob.level().noCollision(mob, boundingBox)) {
                        mob.snapTo(entityX, entityY, entityZ, mob.getYRot(), mob.getXRot());
                        mob.getNavigation().stop();
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
