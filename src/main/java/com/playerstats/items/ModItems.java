package com.playerstats.items;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    // Cria o DeferredRegister para itens
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, "playerstats");

    public static final DeferredHolder<Item, Item> UPGRADE_RUNE = ITEMS.register("upgrade_rune",
            () -> new UpgradeRuneItem(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(16)));
/*
    public static final DeferredHolder<Item, Item> ATTRIBUTE_BOOST_SCROLL = ITEMS.register("attribute_boost_scroll",
            () -> new UpgradeRuneItem(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .stacksTo(1)));*/

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
