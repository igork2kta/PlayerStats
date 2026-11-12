package com.playerstats.fabric.platform;

import com.playerstats.PlayerStats;
import com.playerstats.platform.services.IRegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class FabricRegistryHelper implements IRegistryHelper {

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> itemSupplier) {
        T registered = Registry.register(
                BuiltInRegistries.ITEM,
                new ResourceLocation(PlayerStats.MODID, name),
                itemSupplier.get()
        );
        return () -> registered;
    }

    @Override
    public Supplier<Attribute> registerAttribute(String name, Supplier<Attribute> attributeSupplier) {
        Attribute registered = Registry.register(
                BuiltInRegistries.ATTRIBUTE,
                new ResourceLocation(PlayerStats.MODID, name),
                attributeSupplier.get()
        );
        return () -> registered;
    }
}
