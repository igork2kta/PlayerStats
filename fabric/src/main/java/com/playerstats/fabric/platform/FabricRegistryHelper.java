package com.playerstats.fabric.platform;

import com.playerstats.PlayerStats;
import com.playerstats.platform.services.IRegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricRegistryHelper implements IRegistryHelper {

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> itemSupplier) {
        T registered = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(PlayerStats.MODID, name),
                itemSupplier.get()
        );
        return () -> registered;
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        DataComponentType<T> component = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(PlayerStats.MODID, name),
                builderOperator.apply(DataComponentType.builder()).build()
        );
        return () -> component;
    }

    @Override
    public Holder<Attribute> registerAttribute(String name, Supplier<Attribute> attributeSupplier) {
        return Registry.registerForHolder(
                BuiltInRegistries.ATTRIBUTE,
                ResourceLocation.fromNamespaceAndPath(PlayerStats.MODID, name),
                attributeSupplier.get()
        );
    }
}
