package com.playerstats.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "playerstats");

    public static final RegistryObject<Item> UPGRADE_RUNE = ITEMS.register("upgrade_rune",
            () -> new UpgradeRuneItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(16)));

    public static final RegistryObject<Item> ATTRIBUTE_BOOST_SCROLL = ITEMS.register("attribute_boost_scroll",
            () -> new AttributeBoostScrollItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> ABILITY_CRYSTAL = ITEMS.register("ability_crystal",
            () -> new AbilityCrystalItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SOUL_FRAGMENT = ITEMS.register("soul_fragment",
            () -> new SoulFragmentItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> SOUL_STONE = ITEMS.register("soul_stone",
            () -> new SoulStoneItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
