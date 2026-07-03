package com.catknife.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CatsKnifeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("catsknife")
                .then(Commands.literal("info")
                        .executes(new Command<CommandSourceStack>() {
                            @Override
                            public int run(CommandContext<CommandSourceStack> ctx) {
                                return info(ctx);
                            }
                        }))
                .then(Commands.literal("reload")
                        .requires(new java.util.function.Predicate<CommandSourceStack>() {
                            @Override
                            public boolean test(CommandSourceStack source) {
                                return source.hasPermission(2);
                            }
                        })
                        .executes(new Command<CommandSourceStack>() {
                            @Override
                            public int run(CommandContext<CommandSourceStack> ctx) {
                                return reload(ctx);
                            }
                        }))
                .executes(new Command<CommandSourceStack>() {
                    @Override
                    public int run(CommandContext<CommandSourceStack> ctx) {
                        ctx.getSource().sendSuccess(new Supplier<Component>() {
                            @Override
                            public Component get() {
                                return Component.literal("§6/CatsKnife §7v1.0.0");
                            }
                        }, false);
                        ctx.getSource().sendSuccess(new Supplier<Component>() {
                            @Override
                            public Component get() {
                                return Component.literal("§7  /catsknife info — view mod info");
                            }
                        }, false);
                        ctx.getSource().sendSuccess(new Supplier<Component>() {
                            @Override
                            public Component get() {
                                return Component.literal("§7  /catsknife reload — reload TACZ packs (op only)");
                            }
                        }, false);
                        return Command.SINGLE_SUCCESS;
                    }
                })
        );
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        Set<Map.Entry<ResourceLocation, CommonGunIndex>> allGuns = CommonAssetsManager.getInstance().getAllGuns();
        Set<String> knives = new HashSet<>();
        for (Map.Entry<ResourceLocation, CommonGunIndex> entry : allGuns) {
            ResourceLocation id = entry.getKey();
            if (id.getNamespace().equals("catsknife")) {
                knives.add(id.getPath());
            }
        }

        ctx.getSource().sendSuccess(new Supplier<Component>() {
            @Override
            public Component get() {
                return Component.literal("§6=== Cat's Knife ===");
            }
        }, false);
        ctx.getSource().sendSuccess(new Supplier<Component>() {
            @Override
            public Component get() {
                return Component.literal("§7Version: §f1.0.0");
            }
        }, false);
        ctx.getSource().sendSuccess(new Supplier<Component>() {
            @Override
            public Component get() {
                return Component.literal("§7Knives loaded: §f" + knives.size());
            }
        }, false);
        for (String path : knives) {
            final String knifePath = path;
            ctx.getSource().sendSuccess(new Supplier<Component>() {
                @Override
                public Component get() {
                    return Component.literal("  §7- §f" + knifePath);
                }
            }, false);
        }
        ctx.getSource().sendSuccess(new Supplier<Component>() {
            @Override
            public Component get() {
                return Component.literal("§6===================");
            }
        }, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(new Supplier<Component>() {
            @Override
            public Component get() {
                return Component.literal("§eReloading TACZ packs...");
            }
        }, true);
        CommonAssetsManager.reloadAllPack();
        return Command.SINGLE_SUCCESS;
    }
}