package com.playerstats.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.playerstats.Config;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.commands.CommandSourceStack;
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
            AttributeInstance instance = player.getAttributes().getInstance(attr);

            if (instance == null || !attr.isClientSyncable()) continue;
            if (Config.cachedIgnoredAttributes.contains(attr.getDescriptionId())) continue;

            ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attr);
            if (id != null) {
                builder.suggest(id.toString());
            }
        }

        return builder.buildFuture();
    };

}
