package com.playerstats.mixin;

import com.playerstats.event.InfiniteIceHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer {

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void playerstats$onDrop(ItemStack itemStack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (InfiniteIceHandler.isInfiniteIce(itemStack)) {
            ServerPlayer player = (ServerPlayer) (Object) this;
            if (InfiniteIceHandler.isAbilityActive(player)) {
                if (player.getMainHandItem().isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                } else {
                    player.getInventory().add(itemStack);
                }
                player.containerMenu.broadcastChanges();
            }
            cir.setReturnValue(null);
        }
    }
}
