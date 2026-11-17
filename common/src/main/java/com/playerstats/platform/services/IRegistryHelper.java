package com.playerstats.platform.services;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface IRegistryHelper {

    <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> itemFunction, Item.Properties properties);

    <T> Supplier<DataComponentType<T>> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator);

    Holder<Attribute> registerAttribute(String name, Supplier<Attribute> attributeSupplier);

    Supplier<SoundEvent> registerSoundEvent(String name, Supplier<SoundEvent> soundEventSupplier);
}
