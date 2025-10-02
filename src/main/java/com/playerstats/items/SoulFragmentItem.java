package com.playerstats.items;

import com.playerstats.util.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SoulFragmentItem extends Item {
    public SoulFragmentItem(Properties properties) {
        super(properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {


        if (stack.get(ModDataComponents.STORED_ENTITY) != null) {
            String mobName = stack.get(ModDataComponents.STORED_ENTITY).getString("StoredEntityName");
            //Component mobName = json;

            if (mobName != null) {
                tooltip.add(Component.translatable("item.playerstats.soul_fragment.hoverText", mobName)
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // faz efeito brilhante tipo encantamento
    }
}
