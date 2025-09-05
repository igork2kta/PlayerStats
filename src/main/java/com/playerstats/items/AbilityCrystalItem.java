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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AbilityCrystalItem extends Item {
    public AbilityCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        // Apenas no lado do servidor
        if (!level.isClientSide) {
            // Exemplo: envia uma mensagem e remove 1 item da mão
            player.sendSystemMessage(Component.translatable("item.playerstats.ability_crystal.message").withStyle(ChatFormatting.YELLOW));

            // Consumir o item (1 unidade)
            if (!player.isCreative()) {
                stack.shrink(1);
            }

            level.playSound(null, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,1.0F, 1.0F);

            PlayerAttributePersistence.addAbilityPoints(player, 1);

        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {

        components.add(Component.translatable("item.playerstats.ability_crystal.hoverText").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(itemStack, level, components, tooltipFlag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // faz efeito brilhante tipo encantamento
    }
}
