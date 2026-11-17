package com.playerstats.fabric.platform;

import com.playerstats.PlayerStats;
import com.playerstats.platform.services.IRegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricRegistryHelper implements IRegistryHelper {

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> itemFunction, Item.Properties properties) {
        Identifier id = Identifier.fromNamespaceAndPath(PlayerStats.MODID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        properties.setId(key);
        T item = itemFunction.apply(properties);
        T registered = Registry.register(
                BuiltInRegistries.ITEM,
                key,
                item
        );
        return () -> registered;
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        DataComponentType<T> component = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(PlayerStats.MODID, name),
                builderOperator.apply(DataComponentType.builder()).build()
        );
        return () -> component;
    }

    @Override
    public Holder<Attribute> registerAttribute(String name, Supplier<Attribute> attributeSupplier) {
        return Registry.registerForHolder(
                BuiltInRegistries.ATTRIBUTE,
                Identifier.fromNamespaceAndPath(PlayerStats.MODID, name),
                attributeSupplier.get()
        );
    }

    @Override
    public Supplier<SoundEvent> registerSoundEvent(String name, Supplier<SoundEvent> soundEventSupplier) {
        SoundEvent registered = Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                Identifier.fromNamespaceAndPath(PlayerStats.MODID, name),
                soundEventSupplier.get()
        );
        return () -> registered;
    }
}
