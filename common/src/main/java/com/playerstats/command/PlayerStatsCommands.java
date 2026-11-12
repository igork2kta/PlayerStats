package com.playerstats.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.playerstats.Config;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.util.AttributeSuggestions;
import com.playerstats.util.AttributeUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class PlayerStatsCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerstats")
                .requires(source -> source.hasPermission(2))
                // -------------------------------------------------------
                // ADD ATTRIBUTE POINTS
                // -------------------------------------------------------
                .then(Commands.literal("addattributepoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                // Versão padrão (sem player)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    int current = PlayerAttributePersistence.getPoints(player);

                                    PlayerAttributePersistence.addPoints(player, amount);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.translatable("gui.playerstats.added_points", current + amount), false);
                                    return 1;
                                })
                                // Versão com player opcional
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            int current = PlayerAttributePersistence.getPoints(target);

                                            PlayerAttributePersistence.addPoints(target, amount);
                                            ctx.getSource().sendSuccess(() ->
                                                    Component.translatable("gui.playerstats.added_points_target", target.getName(), current + amount), false);
                                            return 1;
                                        })
                                )
                        )
                )
                // -------------------------------------------------------
                // REMOVE ATTRIBUTE POINTS
                // -------------------------------------------------------
                .then(Commands.literal("removeattributepoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    int current = PlayerAttributePersistence.getPoints(player);

                                    PlayerAttributePersistence.addPoints(player, -amount);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.translatable("gui.playerstats.removed_points", current - amount), false);
                                    return 1;
                                })
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            int current = PlayerAttributePersistence.getPoints(target);

                                            PlayerAttributePersistence.addPoints(target, -amount);
                                            ctx.getSource().sendSuccess(() ->
                                                    Component.translatable("gui.playerstats.removed_points_target", target.getName(), current - amount), false);
                                            return 1;
                                        })
                                )
                        )
                )
                // -------------------------------------------------------
                // ADD ABILITY POINTS
                // -------------------------------------------------------
                .then(Commands.literal("addabilitypoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    int current = PlayerAttributePersistence.getAbilityPoints(player);

                                    PlayerAttributePersistence.addAbilityPoints(player, amount);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.translatable("gui.playerstats.added_points", amount + current), false);
                                    return 1;
                                })
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            int current = PlayerAttributePersistence.getAbilityPoints(target);

                                            PlayerAttributePersistence.addAbilityPoints(target, amount);
                                            ctx.getSource().sendSuccess(() ->
                                                    Component.translatable("gui.playerstats.added_points_target", target.getName(), amount + current), false);
                                            return 1;
                                        })
                                )
                        )
                )
                // -------------------------------------------------------
                // REMOVE ABILITY POINTS
                // -------------------------------------------------------
                .then(Commands.literal("removeabilitypoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    int current = PlayerAttributePersistence.getAbilityPoints(player);

                                    PlayerAttributePersistence.addAbilityPoints(player, -amount);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.translatable("gui.playerstats.removed_points", current - amount), false);
                                    return 1;
                                })
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            int current = PlayerAttributePersistence.getAbilityPoints(target);

                                            PlayerAttributePersistence.addAbilityPoints(target, -amount);
                                            ctx.getSource().sendSuccess(() ->
                                                    Component.translatable("gui.playerstats.removed_points_target", target.getName(), current - amount), false);
                                            return 1;
                                        })
                                )
                        )
                )
                // -------------------------------------------------------
                // RELOAD CONFIG
                // -------------------------------------------------------
                .then(Commands.literal("reloadconfig")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            Config.reloadCustomMobChances();
                            context.getSource().sendSuccess(() ->
                                    Component.translatable("gui.playerstats.reloaded_configs"), true);
                            return 1;
                        })
                )
                // -------------------------------------------------------
                // SET ATTRIBUTE VALUE
                // -------------------------------------------------------
                .then(Commands.literal("set")
                        .then(Commands.argument("attribute_id", ResourceLocationArgument.id())
                                .suggests(AttributeSuggestions.EDITABLE_ATTRIBUTE_IDS)
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> executeSetAttribute(ctx.getSource().getPlayerOrException(), ctx))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                                    return executeSetAttribute(target, ctx);
                                                })
                                        )
                                )
                        )
                )
        );
    }

    private static int executeSetAttribute(ServerPlayer player, com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "attribute_id");
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(id);
        if (attr == null) {
            ctx.getSource().sendFailure(Component.literal("Atributo não encontrado: " + id));
            return 0;
        }

        AttributeInstance instance = AttributeUtils.getAttributeInstance(player, attr);
        AttributeModifier modifier = instance.getModifiers().stream()
                .filter(mod -> mod.getName().equals("playerstats:" + attr.getDescriptionId()))
                .findFirst()
                .orElse(null);

        double modifierValue = (modifier != null) ? modifier.getAmount() : 0;
        double value = DoubleArgumentType.getDouble(ctx, "value") + modifierValue - instance.getValue();

        PlayerAttributePersistence.applyModifier(instance, attr.getDescriptionId() , value);

        ctx.getSource().sendSuccess(() ->
                Component.translatable("command.playerstats.attribute_set", AttributeUtils.getAttributeName(instance.getAttribute()),  player.getName(), DoubleArgumentType.getDouble(ctx, "value")), true);
        return 1;
    }
}
