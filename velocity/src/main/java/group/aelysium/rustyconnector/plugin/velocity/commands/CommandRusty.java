package group.aelysium.rustyconnector.plugin.velocity.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.rustyconnector.core.lib.generic.cache.CacheableMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import group.aelysium.rustyconnector.core.lib.generic.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.generic.Lang;

import java.util.List;

@Plugin(id = "rustyconnector-velocity")
public final class CommandRusty {
    public static BrigadierCommand create() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        LiteralCommandNode<CommandSource> rusty = LiteralArgumentBuilder
            .<CommandSource>literal("rc")
            .requires(source -> source instanceof ConsoleCommandSource)
            .executes(context -> {
                CommandSource source = context.getSource();

                Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                source.sendMessage(Component.text("/rc family").color(NamedTextColor.AQUA));
                source.sendMessage(Component.text("Used to access the family controls for this plugin.").color(NamedTextColor.GRAY));
                VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                source.sendMessage(Component.text("/rc message").color(NamedTextColor.AQUA));
                source.sendMessage(Component.text("Access recently sent rusty-connector messages.").color(NamedTextColor.GRAY));
                VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                source.sendMessage(Component.text("/rc player").color(NamedTextColor.AQUA));
                source.sendMessage(Component.text("Used to access the player controls for this plugin.").color(NamedTextColor.GRAY));
                VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                source.sendMessage(Component.text("/rc registerAll").color(NamedTextColor.YELLOW));
                source.sendMessage(Component.text("Request that all servers listening to the datachannel attempt to register themselves").color(NamedTextColor.GRAY));
                VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                source.sendMessage(Component.text("/rc reload").color(NamedTextColor.YELLOW));
                source.sendMessage(Component.text("Reloads the RustyConnector plugin.").color(NamedTextColor.GRAY));
                source.sendMessage(Component.text("This command should really only be used if the network is down for maintenance or if nobody is online!").color(NamedTextColor.GRAY));
                source.sendMessage(Component.text("This command will kick EVERYONE off of this proxy!").color(NamedTextColor.RED));
                VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                VelocityRustyConnector.getInstance().logger().log(Lang.border());

                return 1;
            })
            .then(LiteralArgumentBuilder.<CommandSource>literal("message")
                    .executes(context -> {
                        CommandSource source = context.getSource();

                        Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                        source.sendMessage(Component.text("/rc message get <Message ID>").color(NamedTextColor.YELLOW));
                        source.sendMessage(Component.text("Pulls a message out of the message cache. If a message is to old it might not be available anymore!").color(NamedTextColor.GRAY));
                        VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                        source.sendMessage(Component.text("/rc message list <page number>").color(NamedTextColor.YELLOW));
                        source.sendMessage(Component.text("Lists all currently cached messages! As new messages get cached, older ones will be pushed out of the cache.").color(NamedTextColor.GRAY));
                        VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                        VelocityRustyConnector.getInstance().logger().log(Lang.border());

                        return 1;
                    })
                    .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                            .executes(context -> {
                                new Thread(() -> {
                                    try {
                                        if(plugin.getMessageCache().getSize() > 10) {
                                            double numberOfPages = Math.floorDiv(plugin.getMessageCache().getSize(),10) + 1;

                                            List<CacheableMessage> messagesPage = plugin.getMessageCache().getMessagesPage(1);

                                            plugin.logger().log(Lang.spacing());
                                            plugin.logger().log(Lang.spacing());
                                            plugin.logger().log(Lang.spacing());
                                            messagesPage.forEach(message -> {
                                                Lang.print(plugin.logger(),
                                                    Lang.get(
                                                            "boxed-message",
                                                            "ID: "+message.getSnowflake(),
                                                            "Date: "+message.getDate().toString(),
                                                            "Contents: "+message.getContents()
                                                        )
                                                    );
                                            });

                                            plugin.logger().log(Lang.spacing());
                                            plugin.logger().log("Showing page 1 out of "+ Math.floor(numberOfPages));
                                            plugin.logger().log(Lang.spacing());
                                            plugin.logger().log(Lang.border());

                                            return;
                                        }

                                        List<CacheableMessage> messages = plugin.getMessageCache().getMessages();

                                        plugin.logger().log(Lang.spacing());
                                        plugin.logger().log(Lang.spacing());
                                        plugin.logger().log(Lang.spacing());
                                        messages.forEach(message -> {
                                            Lang.print(plugin.logger(),
                                                    Lang.get(
                                                            "boxed-message",
                                                            "ID: "+message.getSnowflake(),
                                                            "Date: "+message.getDate().toString(),
                                                            "Contents: "+message.getContents()
                                                    )
                                            );
                                        });
                                    } catch (Exception e) {
                                        plugin.logger().error("There was an issue getting those messages!");
                                    }
                                }).start();

                                return 1;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, Integer>argument("page-number", IntegerArgumentType.integer())
                                    .executes(context -> {
                                        new Thread(() -> {
                                            try {
                                                Integer pageNumber = context.getArgument("page-number", Integer.class);

                                                List<CacheableMessage> messagesPage = plugin.getMessageCache().getMessagesPage(pageNumber);

                                                double numberOfPages = Math.floorDiv(plugin.getMessageCache().getSize(),10) + 1;


                                                plugin.logger().log(Lang.spacing());
                                                plugin.logger().log(Lang.spacing());
                                                plugin.logger().log(Lang.spacing());
                                                messagesPage.forEach(message -> {
                                                    Lang.print(plugin.logger(),
                                                            Lang.get(
                                                                    "boxed-message",
                                                                    "ID: "+message.getSnowflake(),
                                                                    "Date: "+message.getDate().toString(),
                                                                    "Contents: "+message.getContents()
                                                            )
                                                    );
                                                });

                                                plugin.logger().log(Lang.spacing());
                                                plugin.logger().log("Showing page "+pageNumber+" out of "+ Math.floor(numberOfPages));
                                                plugin.logger().log(Lang.spacing());
                                                plugin.logger().log(Lang.border());

                                                return;
                                            } catch (Exception e) {
                                                plugin.logger().error("There was an issue getting those messages!");
                                            }

                                        }).start();
                                        return 1;
                                    })
                            )
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("get")
                            .executes(context -> {
                                CommandSource source = context.getSource();

                                Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                                source.sendMessage(Component.text("/rc message get <Message ID>").color(NamedTextColor.YELLOW));
                                source.sendMessage(Component.text("Pulls a message out of the message cache. If a message is to old it might not be available anymore!").color(NamedTextColor.GRAY));
                                VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                                VelocityRustyConnector.getInstance().logger().log(Lang.border());

                                return 1;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, Long>argument("snowflake", LongArgumentType.longArg())
                                    .executes(context -> {
                                        try {
                                            Long snowflake = context.getArgument("snowflake", Long.class);
                                            MessageCache messageCache = VelocityRustyConnector.getInstance().getMessageCache();

                                            CacheableMessage message = messageCache.getMessage(snowflake);

                                            Lang.print(VelocityRustyConnector.getInstance().logger(),
                                                    Lang.get("boxed-message",
                                                            "Found message with ID "+snowflake.toString(),
                                                            Lang.spacing(),
                                                            "ID: "+message.getSnowflake(),
                                                            "Date: "+message.getDate().toString(),
                                                            "Contents: "+message.getContents()
                                                    )
                                            );
                                        } catch (NullPointerException e) {
                                            VelocityRustyConnector.getInstance().logger().log("That message either doesn't exist or is no-longer available in the cache!");
                                        } catch (Exception e) {
                                            VelocityRustyConnector.getInstance().logger().log("An error stopped us from getting that message!", e);
                                        }

                                        return 1;
                                    })
                            )
                    )
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("family")
                .executes(context -> {
                    CommandSource source = context.getSource();

                    Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                    source.sendMessage(Component.text("/rc family list").color(NamedTextColor.AQUA));
                    source.sendMessage(Component.text("Gets a list of all registered families.").color(NamedTextColor.GRAY));
                    VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                    source.sendMessage(Component.text("/rc family info <family name>").color(NamedTextColor.AQUA));
                    source.sendMessage(Component.text("Gets info about a particular family").color(NamedTextColor.GRAY));
                    VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                    source.sendMessage(Component.text("/rc family reload all").color(NamedTextColor.AQUA));
                    source.sendMessage(Component.text("Reloads all families, this also unregisters all servers that are saved.").color(NamedTextColor.GRAY));
                    source.sendMessage(Component.text("This command will kick EVERYONE off of this proxy!").color(NamedTextColor.RED));
                    VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                    source.sendMessage(Component.text("/rc family reload <family name>").color(NamedTextColor.AQUA));
                    source.sendMessage(Component.text("Reload a specific family, this also unregisters all servers that are saved to this family.").color(NamedTextColor.GRAY));
                    source.sendMessage(Component.text("This command will kick EVERYONE off of this specific family!").color(NamedTextColor.RED));
                    VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                    VelocityRustyConnector.getInstance().logger().log(Lang.border());

                    return 1;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                    .executes(context -> {
                        plugin.getProxy().printFamilies();
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("info")
                    .executes(context -> {
                        CommandSource source = context.getSource();

                        Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                        source.sendMessage(Component.text("/rc family info <family name>").color(NamedTextColor.YELLOW));
                        source.sendMessage(Component.text("Get info for this family").color(NamedTextColor.GRAY));
                        VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                        source.sendMessage(Component.text("/rc family info <family name> servers").color(NamedTextColor.YELLOW));
                        source.sendMessage(Component.text("Lists all servers that are registered to this family").color(NamedTextColor.GRAY));
                        VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                        VelocityRustyConnector.getInstance().logger().log(Lang.border());

                        return 1;
                    })
                    .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                            .executes(context -> {
                                String familyName = context.getArgument("familyName", String.class);
                                ServerFamily family = VelocityRustyConnector.getInstance().getProxy().findFamily(familyName);

                                family.printInfo();
                                return 1;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, String>argument("servers", StringArgumentType.word())
                                    .executes(context -> {
                                        String familyName = context.getArgument("familyName", String.class);
                                        ServerFamily family = VelocityRustyConnector.getInstance().getProxy().findFamily(familyName);

                                        family.printServers();
                                        return 1;
                                    })
                            )
                    )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                    .executes(context -> {
                        CommandSource source = context.getSource();

                        Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                        source.sendMessage(Component.text("/rc family reload all").color(NamedTextColor.AQUA));
                        source.sendMessage(Component.text("Reloads all families, this also unregisters all servers that are saved.").color(NamedTextColor.GRAY));
                        source.sendMessage(Component.text("This command will kick EVERYONE off of this proxy!").color(NamedTextColor.RED));
                        VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                        source.sendMessage(Component.text("/rc family reload <family name>").color(NamedTextColor.AQUA));
                        source.sendMessage(Component.text("Reload a specific family, this also unregisters all servers that are saved to this family.").color(NamedTextColor.GRAY));
                        source.sendMessage(Component.text("This command will kick EVERYONE off of this specific family!").color(NamedTextColor.RED));
                        VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                        VelocityRustyConnector.getInstance().logger().log(Lang.border());
                        return 1;
                    })
                    .then(LiteralArgumentBuilder.<CommandSource>literal("all")
                            .executes(context -> {
                                List<ServerFamily> families = VelocityRustyConnector.getInstance().getProxy().getRegisteredFamilies();

                                // TODO: Reload all families

                                return 1;
                            })
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("familyName")
                            .executes(context -> {
                                String familyName = context.getArgument("familyName", String.class);
                                ServerFamily family = VelocityRustyConnector.getInstance().getProxy().findFamily(familyName);

                                // TODO: Reload a specific family

                                return 1;
                            })
                    )
                )
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("registerAll")
                    .executes(context -> {
                        VelocityRustyConnector.getInstance().registerAllServers();

                        return 1;
                    })
            )
            /*.then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                    .executes(context -> {
                        VelocityRustyConnector.getInstance().reload();

                        return 1;
                    })
            )*/
            .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(rusty);
    }
}