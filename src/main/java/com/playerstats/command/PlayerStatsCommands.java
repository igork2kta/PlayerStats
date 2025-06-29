package com.playerstats.command;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.playerstats.Config;
import com.playerstats.event.PlayerAttributePersistence;
import com.playerstats.network.PacketHandler;
import com.playerstats.network.UpdatePointsPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class PlayerStatsCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerstats")
                .requires(source -> source.hasPermission(2)) // apenas operadores por padrão
                .then(Commands.literal("addpoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    int current = PlayerAttributePersistence.getPoints(player);
                                    int newValue = Math.max(0, current + amount);
                                    PlayerAttributePersistence.setPoints(player, newValue);
                                    PacketHandler.sendToClient(new UpdatePointsPacket(newValue), player);
                                    ctx.getSource().sendSuccess(() ->
                                            net.minecraft.network.chat.Component.translatable("gui.playerstats.added_points", newValue), false);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("removepoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    int current = PlayerAttributePersistence.getPoints(player);
                                    int newValue = Math.max(0, current - amount);
                                    PlayerAttributePersistence.setPoints(player, newValue);
                                    PacketHandler.sendToClient(new UpdatePointsPacket(newValue), player);
                                    ctx.getSource().sendSuccess(() ->
                                            net.minecraft.network.chat.Component.translatable("gui.playerstats.removed_points", newValue), false);
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
                )
        );
    }
}

