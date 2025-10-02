package com.playerstats.command;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.playerstats.Config;
import com.playerstats.event.PlayerAttributePersistence;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class PlayerStatsCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerstats")
                .requires(source -> source.hasPermission(2)) // apenas operadores por padrão
                .then(Commands.literal("addattributepoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    int current = PlayerAttributePersistence.getPoints(player);

                                    PlayerAttributePersistence.addPoints(player, amount);

                                    ctx.getSource().sendSuccess(() ->
                                            net.minecraft.network.chat.Component.translatable("gui.playerstats.added_points", current + amount), false);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("removeattributepoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    int current = PlayerAttributePersistence.getPoints(player);

                                    PlayerAttributePersistence.addAbilityPoints(player, -amount);
                                    ctx.getSource().sendSuccess(() ->
                                            net.minecraft.network.chat.Component.translatable("gui.playerstats.removed_points", current - amount), false);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("addabilitypoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    int current = PlayerAttributePersistence.getAbilityPoints(player);

                                    PlayerAttributePersistence.addAbilityPoints(player, amount);
                                    ctx.getSource().sendSuccess(() ->
                                            net.minecraft.network.chat.Component.translatable("gui.playerstats.added_points", amount + current), false);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("removeabilitypoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    int current = PlayerAttributePersistence.getAbilityPoints(player);

                                    PlayerAttributePersistence.addAbilityPoints(player, -amount);
                                    ctx.getSource().sendSuccess(() ->
                                            net.minecraft.network.chat.Component.translatable("gui.playerstats.removed_points", current - amount), false);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("reloadconfig")
                        .requires(source -> source.hasPermission(2)) // Só operadores
                        .executes(context -> {
                            Config.reloadCustomMobChances();
                            context.getSource().sendSuccess(() ->
                                    net.minecraft.network.chat.Component.translatable("gui.playerstats.reloaded_configs"), true);
                            return 1;
                        })
                )/*
                .then(Commands.literal("set")
                        .then(Commands.argument("attribute_id", StringArgumentType.string())
                                .suggests(AttributeSuggestions.EDITABLE_ATTRIBUTE_IDS)
                        .then(Commands.argument("value", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String attributeId = StringArgumentType.getString(ctx, "attribute_id");
                                    int value = IntegerArgumentType.getInteger(ctx, "value");

                                    boolean success = PlayerAttributePersistence.setAttribute(player, attributeId, value);

                                    if (success) {
                                        ctx.getSource().sendSuccess(() ->
                                                Component.literal("Atributo '" + attributeId + "' definido como " + value), true);
                                        return 1;
                                    } else {
                                        ctx.getSource().sendFailure(
                                                Component.literal("Falha ao aplicar atributo: '" + attributeId + "'"));
                                        return 0;
                                    }
                                }))))*/
        );
    }
}

