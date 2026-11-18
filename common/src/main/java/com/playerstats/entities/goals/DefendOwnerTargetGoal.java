package com.playerstats.entities.goals;

import com.playerstats.ModAttributes;
import com.playerstats.platform.Services;
import com.playerstats.util.AttributeUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

public class DefendOwnerTargetGoal extends TargetGoal {
    private final IronGolem golem;
    private LivingEntity targetToAttack;
    private int hurtByTimestamp;
    private int hurtTimestamp;

    public DefendOwnerTargetGoal(IronGolem golem) {
        super(golem, false);
        this.golem = golem;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (AttributeUtils.getAttributeValue(golem, ModAttributes.DEFEND_OWNER) < 1.0D) {
            return false;
        }

        UUID ownerId = Services.PLATFORM.getPersistentData(golem).read("Owner", UUIDUtil.CODEC).orElse(null);
        if (ownerId == null) return false;

        Player owner = golem.level().getPlayerByUUID(ownerId);
        if (owner == null || !owner.isAlive()) return false;

        // 1. Prioridade: Quem atacou o dono recentemente
        LivingEntity attacker = owner.getLastHurtByMob();
        int attackerTime = owner.getLastHurtByMobTimestamp();
        if (attacker != null && attackerTime != this.hurtByTimestamp && attacker != golem && this.canAttack(attacker, TargetingConditions.DEFAULT)) {
            this.targetToAttack = attacker;
            return true;
        }

        // 2. Prioridade: Quem o dono atacou recentemente
        LivingEntity target = owner.getLastHurtMob();
        int targetTime = owner.getLastHurtMobTimestamp();
        if (target != null && targetTime != this.hurtTimestamp && target != golem && this.canAttack(target, TargetingConditions.DEFAULT)) {
            this.targetToAttack = target;
            return true;
        }

        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.targetToAttack);
        UUID ownerId = Services.PLATFORM.getPersistentData(golem).read("Owner", UUIDUtil.CODEC).orElse(null);
        if (ownerId != null) {
            Player owner = golem.level().getPlayerByUUID(ownerId);
            if (owner != null) {
                this.hurtByTimestamp = owner.getLastHurtByMobTimestamp();
                this.hurtTimestamp = owner.getLastHurtMobTimestamp();
            }
        }
        super.start();
    }
}
