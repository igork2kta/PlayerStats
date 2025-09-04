package com.playerstats.entities.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

public class HorseFollowOwnerGoal extends Goal {

    private final AbstractHorse horse;
    private Player owner;
    private final double speed;
    private final float minDist;
    private final float maxDist;

    public HorseFollowOwnerGoal(AbstractHorse horse) {
        this.horse = horse;
        this.speed = 1.2D;
        this.minDist = 4.0F;
        this.maxDist = 12.0F;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        try{
            UUID ownerId = horse.getPersistentData().getUUID("Owner");
            if (ownerId == null) return false;
            owner = horse.level().getPlayerByUUID(ownerId);
            return owner != null && !horse.isVehicle() && !horse.isLeashed() && horse.distanceTo(owner) > minDist;
        }
        catch(Exception ex){
            return false;
        }

    }

    @Override
    public boolean canContinueToUse() {
        return owner != null && horse.distanceTo(owner) > maxDist && !horse.isVehicle() && !horse.isLeashed();
    }

    @Override
    public void tick() {
        if (owner == null) return;
        if(!horse.getPersistentData().getBoolean("follow_owner")) return;
        double distance = horse.distanceTo(owner);

        // Se estiver longe demais, teletransporta
        if (distance > 12.0 && horse.getPersistentData().getBoolean("teleport_to_owner")) {
            teleportToOwner();
        } else {
            // Move normalmente
            horse.getNavigation().moveTo(owner, speed);
        }
    }

    private void teleportToOwner() {
        if (owner == null) return;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                // Procura do nível do jogador para baixo
                for (int dy = 0; dy >= -2; dy--) {
                    Vec3 target = owner.position().add(dx, dy, dz);
                    BlockPos blockPos = new BlockPos((int) target.x, (int) target.y -1, (int) target.z);

                    // Verifica se o bloco atual está vazio e o bloco abaixo é sólido
                    BlockPos blockBelow = blockPos.below();
                    if (horse.level().getBlockState(blockPos).getCollisionShape(horse.level(), blockPos).isEmpty() &&
                            horse.level().getBlockState(blockBelow).isSolidRender(horse.level(), blockBelow)) {

                        horse.setPos(target.x, target.y, target.z);
                        horse.getNavigation().stop();
                        return;
                    }
                }
            }
        }
    }
}

