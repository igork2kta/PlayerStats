package com.playerstats.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    // Cria o DeferredRegister para itens
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems("playerstats");

    public static final DeferredItem<Item> UPGRADE_RUNE = ITEMS.registerItem("upgrade_rune",
            properties -> new UpgradeRuneItem(properties
                    .rarity(Rarity.RARE)
                    .stacksTo(16)));

    public static final DeferredItem<Item> ATTRIBUTE_BOOST_SCROLL = ITEMS.registerItem("attribute_boost_scroll",
            properties -> new AttributeBoostScrollItem(properties
                    .rarity(Rarity.UNCOMMON)
                    .stacksTo(1)));

    public static final DeferredItem<Item> ABILITY_CRYSTAL = ITEMS.registerItem("ability_crystal",
            properties -> new AbilityCrystalItem(properties
                    .rarity(Rarity.RARE)
                    .stacksTo(16)));

    public static final DeferredItem<Item> SOUL_FRAGMENT = ITEMS.registerItem("soul_fragment",
            properties -> new SoulFragmentItem(properties.stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> SOUL_STONE = ITEMS.registerItem("soul_stone",
            properties -> new SoulStoneItem(properties.stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredItem<Item> INFINITE_ICE = ITEMS.registerItem("infinite_ice",
            properties -> new InfiniteIceItem(properties.stacksTo(1).rarity(Rarity.RARE)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
