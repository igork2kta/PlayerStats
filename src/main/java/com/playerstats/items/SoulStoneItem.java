package com.playerstats.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SoulStoneItem extends Item {
    public SoulStoneItem(Properties properties) {
        super(properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.playerstats.soul_stone.hoverText" ).withStyle(ChatFormatting.AQUA));

    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // faz efeito brilhante tipo encantamento
    }
}
