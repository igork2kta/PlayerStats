package com.playerstats.util;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.compress.harmony.pack200.Codec;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final String MODID = "playerstats";

    // Criar o registry
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> BOOST_AMOUNT =
            register("boost_amount",
                    builder -> builder.persistent(Codec));





    // Definir os componentes
    public static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator){
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register (IEventBus eventBus){
        DATA_COMPONENT_TYPES.register(eventBus);
    }






    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> BOOST_AMOUNT =
            DATA_COMPONENT_TYPES.register("boost_amount",
                    () -> DataComponentType.<Double>builder()
                            .persistent(net.minecraft.util.)
                            .networkSynchronized(ByteBufCodecs.DOUBLE)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BOOST_DURATION =
            DATA_COMPONENT_TYPES.register("boost_duration",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(net.minecraft.util.ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build()
            );


