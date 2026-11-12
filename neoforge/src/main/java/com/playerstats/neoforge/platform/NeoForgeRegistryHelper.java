package com.playerstats.neoforge.platform;

import com.playerstats.PlayerStats;
import com.playerstats.platform.services.IRegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NeoForgeRegistryHelper implements IRegistryHelper {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, PlayerStats.MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PlayerStats.MODID);
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, PlayerStats.MODID);

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        DATA_COMPONENT_TYPES.register(modBus);
        ATTRIBUTES.register(modBus);
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> itemSupplier) {
        return ITEMS.register(name, itemSupplier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<DataComponentType<T>> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        var holder = DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
        return (Supplier<DataComponentType<T>>) (Supplier<?>) holder;
    }

    @Override
    public Holder<Attribute> registerAttribute(String name, Supplier<Attribute> attributeSupplier) {
        return ATTRIBUTES.register(name, attributeSupplier);
    }
}
