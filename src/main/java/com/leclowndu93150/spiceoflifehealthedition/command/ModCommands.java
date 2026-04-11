package com.leclowndu93150.spiceoflifehealthedition.command;

import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.NutritionManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ModCommands {

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("solhe")
                .then(Commands.literal("diet")
                        .then(Commands.literal("clear").executes(ModCommands::clearDiet))
                        .then(Commands.literal("clean").executes(ModCommands::cleanEmpty))
                        .then(Commands.literal("info").executes(ModCommands::dietInfo))
                )
                .then(Commands.literal("recompute").executes(ModCommands::recompute))
        );
    }

    private static int clearDiet(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
        DietHistory history = player.getData(DietAttachment.DIET);
        history.clear();
        player.setData(DietAttachment.DIET, history);
        ctx.getSource().sendSuccess(() -> Component.literal("Diet history cleared"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int cleanEmpty(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
        DietHistory history = player.getData(DietAttachment.DIET);
        int before = history.getEntryCount();
        history.clearEmptyEntries();
        int after = history.getEntryCount();
        player.setData(DietAttachment.DIET, history);
        int removed = before - after;
        ctx.getSource().sendSuccess(() -> Component.literal("Removed " + removed + " empty entries"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int dietInfo(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
        DietHistory history = player.getData(DietAttachment.DIET);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Entries: " + history.getEntryCount()
                        + " | Diversity: " + history.getDiversity()
                        + " | Weight: " + String.format("%.1f", history.getWeight())
                        + " | Avg total: " + String.format("%.2f", history.getAverage().total())
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int recompute(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        NutritionManager.get().recompute(
                ctx.getSource().getServer().getRecipeManager(),
                ctx.getSource().getServer().registryAccess()
        );
        int count = NutritionManager.get().getCacheByKey().size();
        ctx.getSource().sendSuccess(() -> Component.literal("Recomputed nutrition cache: " + count + " entries"), false);
        return Command.SINGLE_SUCCESS;
    }
}
