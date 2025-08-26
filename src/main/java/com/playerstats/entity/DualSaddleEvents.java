package com.playerstats.entity;

import com.playerstats.items.DoubleSaddleItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "playerstats")
public class DualSaddleEvents {

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Horse horse)) return;
        if (!(event.getItemStack().getItem() instanceof DoubleSaddleItem)) return;

        Level level = event.getLevel();
        if (level.isClientSide) return;

        System.out.println("SERVER: substituindo cavalo...");
        Player player = event.getEntity();

        // cria um novo cavalo customizado
        DualSaddleHorse newHorse = ModEntities.DUAL_SADDLE_HORSE.get().create(level);
        if (newHorse == null) return;

        // copia posição
        newHorse.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());

        // copia atributos básicos
        newHorse.setHealth(horse.getHealth());
        newHorse.setTamed(horse.isTamed());
        newHorse.setOwnerUUID(horse.getOwnerUUID());
        newHorse.setAge(horse.getAge());

        // copia inventário (armaduras, etc.)
        //newHorse.getInventory().replaceWith(horse.getInventory());
        // Copiar sela
        if (horse.isSaddled()) {
            newHorse.equipSaddle(null); // equipa sela normal
        }
/*
        // Copiar armadura
        ItemStack armor = horse.getArmor();
        if (!armor.isEmpty()) {
            newHorse.setArmor(armor.copy());
        }*/
        // remove o cavalo original e coloca o novo
        horse.discard();
        level.addFreshEntity(newHorse);

        // consome a sela dupla
        if (!player.isCreative()) {
            event.getItemStack().shrink(1);
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
