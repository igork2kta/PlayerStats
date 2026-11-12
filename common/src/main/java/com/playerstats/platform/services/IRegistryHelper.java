package com.playerstats.platform.services;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public interface IRegistryHelper {

    <T extends Item> Supplier<T> registerItem(String name, Supplier<T> itemSupplier);

    Supplier<Attribute> registerAttribute(String name, Supplier<Attribute> attributeSupplier);
}
