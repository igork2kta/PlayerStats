package com.playerstats.entities.goals;

import com.playerstats.ModAttributes;
import com.playerstats.util.AttributeUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class DefendOwnerTargetGoal extends TargetGoal {
    private final IronGolem golem;
    private Player owner;

    public DefendOwnerTargetGoal(IronGolem golem) {
        super(golem, false);
        this.golem = golem;
    }

    @Override
    public boolean canUse() {

        UUID ownerId = null;

        if(golem.getPersistentData().contains("Owner"))
            ownerId = golem.getPersistentData().getUUID("Owner");

        if (ownerId == null) return false;

        if(AttributeUtils.getAttributeValue(golem, ModAttributes.DEFEND_OWNER) < 1) return false;

        owner = golem.level().getPlayerByUUID(ownerId);
        if (owner == null) return false;

        LivingEntity attacker = owner.getLastHurtByMob();
        if (attacker != null && attacker.isAlive()) {
            this.targetMob = attacker;
            return true;
        }

        return false;
    }
}
