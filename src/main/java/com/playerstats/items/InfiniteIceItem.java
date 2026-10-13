package com.playerstats.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class InfiniteIceItem extends BlockItem {

    public InfiniteIceItem() {
        super(Blocks.ICE, new Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPlaceContext placeContext = this.updatePlacementContext(new BlockPlaceContext(context));
        if (placeContext == null) {
            return InteractionResult.FAIL;
        }

        // Armazena a quantidade do item na mão antes da colocação
        ItemStack itemInHand = context.getItemInHand();
        int countBefore = itemInHand.getCount();

        InteractionResult result = this.place(placeContext);

        // Se o vanilla consumiu o item (shrink), restaura para manter infinito
        if (result.consumesAction()) {
            if (!context.getPlayer().isCreative()) {
                itemInHand.setCount(countBefore);
            }

            // Registra para evaporar em AR (sem deixar água) após 5 segundos
            Level level = context.getLevel();
            if (!level.isClientSide) {
                BlockPos placedPos = placeContext.getClickedPos();
                com.playerstats.event.InfiniteIceHandler.registerPlacedIce(level, placedPos);
            }
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.playerstats.infinite_ice.hover_text").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.playerstats.infinite_ice.hover_text_desc").withStyle(ChatFormatting.GRAY));
    }
}

