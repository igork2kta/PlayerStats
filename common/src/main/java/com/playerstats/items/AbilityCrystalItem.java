package com.playerstats.items;

import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class AbilityCrystalItem extends Item {
    public AbilityCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        // Apenas no lado do servidor
        if (!level.isClientSide()) {
            // Exemplo: envia uma mensagem e remove 1 item da mão
            player.sendSystemMessage(Component.translatable("item.playerstats.ability_crystal.message").withStyle(ChatFormatting.YELLOW));

            // Consumir o item (1 unidade)
            if (!player.isCreative()) {
                stack.shrink(1);
            }

            level.playSound(null, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);

            PlayerAttributePersistence.addAbilityPoints(player, 1);

        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.playerstats.ability_crystal.hoverText").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // faz efeito brilhante tipo encantamento
    }
}
