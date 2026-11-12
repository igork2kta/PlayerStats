package com.playerstats.forge.platform;

import com.playerstats.PlayerStats;
import com.playerstats.platform.services.IRegistryHelper;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ForgeRegistryHelper implements IRegistryHelper {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PlayerStats.MODID);
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, PlayerStats.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        ATTRIBUTES.register(eventBus);
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> itemSupplier) {
        return ITEMS.register(name, itemSupplier);
    }

    @Override
    public Supplier<Attribute> registerAttribute(String name, Supplier<Attribute> attributeSupplier) {
        return ATTRIBUTES.register(name, attributeSupplier);
    }
}
