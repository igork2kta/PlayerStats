package com.playerstats.util;

import com.mojang.serialization.Codec;
import com.playerstats.platform.Services;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Supplier;

public class ModDataComponents {

    public static final Supplier<DataComponentType<String>> DEFINED_ATTRIBUTE =
            Services.REGISTRY.registerDataComponent("defined_attribute",
                    builder -> builder.persistent(Codec.STRING));

    public static final Supplier<DataComponentType<Double>> BOOST_AMOUNT =
            Services.REGISTRY.registerDataComponent("boost_amount",
                    builder -> builder.persistent(Codec.DOUBLE));

    public static final Supplier<DataComponentType<Integer>> BOOST_DURATION =
            Services.REGISTRY.registerDataComponent("boost_duration",
                    builder -> builder.persistent(Codec.INT));

    public static final Supplier<DataComponentType<CompoundTag>> STORED_ENTITY =
            Services.REGISTRY.registerDataComponent("stored_entity",
                    builder -> builder.persistent(CompoundTag.CODEC));

    public static void init() {
    }
}
