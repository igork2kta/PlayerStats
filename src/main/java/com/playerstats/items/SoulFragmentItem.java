package com.playerstats.items;

import com.playerstats.util.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class SoulFragmentItem extends Item {
    public SoulFragmentItem(Properties properties) {
        super(properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        var stored = stack.get(ModDataComponents.STORED_ENTITY);
        if (stored != null) {
            String mobName = stored.getStringOr("StoredEntityName", "");
            if (!mobName.isEmpty()) {
                tooltip.accept(Component.translatable("item.playerstats.soul_fragment.hoverText", mobName)
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // faz efeito brilhante tipo encantamento
    }
}
