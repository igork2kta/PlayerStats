package com.playerstats.items;

import com.playerstats.platform.Services;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Supplier;

public class ModItems {

    public static final Supplier<Item> UPGRADE_RUNE = Services.REGISTRY.registerItem("upgrade_rune",
            () -> new UpgradeRuneItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(16)));

    public static final Supplier<Item> ATTRIBUTE_BOOST_SCROLL = Services.REGISTRY.registerItem("attribute_boost_scroll",
            () -> new AttributeBoostScrollItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final Supplier<Item> ABILITY_CRYSTAL = Services.REGISTRY.registerItem("ability_crystal",
            () -> new AbilityCrystalItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final Supplier<Item> SOUL_FRAGMENT = Services.REGISTRY.registerItem("soul_fragment",
            () -> new SoulFragmentItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final Supplier<Item> SOUL_STONE = Services.REGISTRY.registerItem("soul_stone",
            () -> new SoulStoneItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final Supplier<Item> INFINITE_ICE = Services.REGISTRY.registerItem("infinite_ice",
            InfiniteIceItem::new);

    public static void init() {
    }
}
