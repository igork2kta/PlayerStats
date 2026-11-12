package com.playerstats.items;

import com.playerstats.platform.Services;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Supplier;

public class ModItems {

    public static final Supplier<UpgradeRuneItem> UPGRADE_RUNE = Services.REGISTRY.registerItem("upgrade_rune",
            () -> new UpgradeRuneItem(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(16)));

    public static final Supplier<AttributeBoostScrollItem> ATTRIBUTE_BOOST_SCROLL = Services.REGISTRY.registerItem("attribute_boost_scroll",
            () -> new AttributeBoostScrollItem(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .stacksTo(1)));

    public static final Supplier<AbilityCrystalItem> ABILITY_CRYSTAL = Services.REGISTRY.registerItem("ability_crystal",
            () -> new AbilityCrystalItem(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(16)));

    public static final Supplier<SoulFragmentItem> SOUL_FRAGMENT = Services.REGISTRY.registerItem("soul_fragment",
            () -> new SoulFragmentItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final Supplier<SoulStoneItem> SOUL_STONE = Services.REGISTRY.registerItem("soul_stone",
            () -> new SoulStoneItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final Supplier<InfiniteIceItem> INFINITE_ICE = Services.REGISTRY.registerItem("infinite_ice",
            InfiniteIceItem::new);

    public static void init() {
    }
}
