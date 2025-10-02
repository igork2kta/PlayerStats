package com.playerstats.items;


import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class UpgradeRuneItem extends Item {
    public UpgradeRuneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Apenas no lado do servidor
        if (!level.isClientSide) {
            // Mensagem de feedback
            player.sendSystemMessage(Component.translatable("item.playerstats.upgrade_rune.message")
                    .withStyle(ChatFormatting.YELLOW));

            // Consumir o item (1 unidade), se não for criativo
            if (!player.isCreative()) {
                stack.shrink(1);
            }

            // Som de feedback
            level.playSound(null, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);

            // Dá pontos ao jogador
            PlayerAttributePersistence.addPoints(player, 1);


        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.playerstats.upgrade_rune.hoverText").withStyle(ChatFormatting.AQUA));
    }


}

