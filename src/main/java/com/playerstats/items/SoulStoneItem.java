package com.playerstats.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoulStoneItem extends Item {
    public SoulStoneItem(Properties properties) {
        super(properties);
    }


    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {

        components.add(Component.translatable("item.playerstats.soul_stone.hoverText" ).withStyle(ChatFormatting.AQUA));
        super.appendHoverText(itemStack, level, components, tooltipFlag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // faz efeito brilhante tipo encantamento
    }
}
