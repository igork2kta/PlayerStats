package com.playerstats.items;

import com.playerstats.entity.DualSaddleHorse;
import com.playerstats.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "playerstats")
public class DoubleSaddleItem extends Item {
    public DoubleSaddleItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide && target instanceof Horse horse) {
            if (!horse.isSaddled()) {
                DualSaddleHorse newHorse = ModEntities.DUAL_SADDLE_HORSE.get().create(player.level());

                if (newHorse != null) {
                    newHorse.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
                    newHorse.setHealth(horse.getHealth());
                    newHorse.getAttributes().assignValues(horse.getAttributes());
                    newHorse.readAdditionalSaveData(horse.saveWithoutId(new CompoundTag()));

                    // agora seta a sela dupla direto no novo cavalo
                    ItemStack saddleStack = stack.copyWithCount(1);
                    ItemStack sela = new ItemStack(Items.SADDLE, 1);
                    newHorse.setItemSlot(LivingEntity.getEquipmentSlotForItem(sela),saddleStack);

                    horse.discard();
                    player.level().addFreshEntity(newHorse);

                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
