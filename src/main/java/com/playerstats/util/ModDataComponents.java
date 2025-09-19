package com.playerstats.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final String MODID = "playerstats";


    // Criar o registry
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    //region AttributeBoostScrool
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> DEFINED_ATTRIBUTE =
            register("defined_attribute",
                    builder -> builder.persistent(Codec.STRING));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> BOOST_AMOUNT =
            register("boost_amount",
                    builder -> builder.persistent(Codec.DOUBLE));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BOOST_DURATION =
            register("boost_duration",
                    builder -> builder.persistent(Codec.INT));

    //endregion



    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> STORED_ENTITY =
            register("stored_entity",
                    builder -> builder.persistent(CompoundTag.CODEC));




    // Definir os componentes
    public static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}


