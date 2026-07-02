package com.catknife.command;

import com.catknife.CatsKnife;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class CatsKnifeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("catsknife")
                .then(Commands.literal("info")
                        .executes(CatsKnifeCommand::info))
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(CatsKnifeCommand::reload))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("§6/CatsKnife §7v1.0.0"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("§7  /catsknife info — view mod info"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("§7  /catsknife reload — reload TACZ packs (op only)"), false);
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        Set<ResourceLocation> knives = CommonAssetsManager.getInstance().getAllGuns().stream()
                .map(e -> e.getKey())
                .filter(id -> id.getNamespace().equals("catsknife"))
                .collect(java.util.stream.Collectors.toSet());

        ctx.getSource().sendSuccess(() -> Component.literal("§6=== Cat's Knife ==="), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7Version: §f1.0.0"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7Knives loaded: §f" + knives.size()), false);
        knives.forEach(id -> ctx.getSource().sendSuccess(() -> Component.literal("  §7- §f" + id.getPath()), false));
        ctx.getSource().sendSuccess(() -> Component.literal("§6==================="), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("§eReloading TACZ packs..."), true);
        CommonAssetsManager.reloadAllPack();
        return Command.SINGLE_SUCCESS;
    }
}