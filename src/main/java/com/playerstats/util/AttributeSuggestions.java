package com.playerstats.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.playerstats.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class AttributeSuggestions {
    public static final SuggestionProvider<CommandSourceStack> EDITABLE_ATTRIBUTE_IDS = (context, builder) -> {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException e) {
            return Suggestions.empty();
        }

        for (Attribute attr : BuiltInRegistries.ATTRIBUTE) {
            ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attr);
            if (id == null) continue;

            // Criamos a ResourceKey para este atributo
            ResourceKey<Attribute> key = ResourceKey.create(Registries.ATTRIBUTE, id);

            // Pegamos o Holder correspondente
            Holder.Reference<Attribute> holder = player.level().registryAccess()
                    .registryOrThrow(Registries.ATTRIBUTE)
                    .getHolder(key)
                    .orElse(null);

            if (holder == null) continue;

            AttributeInstance instance = player.getAttributes().getInstance(holder);

            if (instance == null || !attr.isClientSyncable()) continue;
            if (Config.cachedIgnoredAttributes.contains(attr.getDescriptionId())) continue;

            builder.suggest(id.toString());
        }

        return builder.buildFuture();
    };
}
