package com.playerstats.entity;


import com.playerstats.items.DoubleSaddleItem;
import com.playerstats.items.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
public class DualSaddleHorse extends Horse {

    public DualSaddleHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 2 && passenger instanceof Player;
    }

    @Override
    public void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) return;

        int index = this.getPassengers().indexOf(passenger);
        double yOffset = this.getPassengersRidingOffset();

        // rotação do cavalo
        float rotYaw = this.getYRot() * ((float)Math.PI / 180F);
        float sin = (float)Math.sin(rotYaw);
        float cos = (float)Math.cos(rotYaw);

        double xOffset = 0;
        double zOffset = 0;

        if (index == 0) {
            // primeiro passageiro
            xOffset = 0;
            zOffset = 0;
        } else if (index == 1) {
            // segundo passageiro (atrás)
            xOffset = -0.5 * cos;
            zOffset = 0.5 * sin;
        }

        passenger.setPos(this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset);

        // sincroniza rotação do passageiro com o cavalo
        passenger.setYRot(this.getYRot());
        passenger.setYHeadRot(this.getYRot());
        passenger.yRotO = this.yRotO;
        passenger.xRotO = this.xRotO;
    }




    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.getFirstPassenger() instanceof LivingEntity living) {
            return living; // só o primeiro controla
        }
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            if (this.isVehicle() && this.getPassengers().size() == 1) {
                // já tem 1 montado, deixa o segundo subir
                player.startRiding(this);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isSaddleable() {
        return true;
    }

    @Override
    public boolean isSaddled() {
        return this.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.DOUBLE_SADDLE.get();
    }
}
