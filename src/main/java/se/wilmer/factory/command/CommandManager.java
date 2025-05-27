package se.wilmer.factory.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import se.wilmer.factory.Factory;
import se.wilmer.factory.command.commands.debug.BlockLookUpCommand;
import se.wilmer.factory.command.commands.debug.NetworkClearCommand;
import se.wilmer.factory.command.commands.debug.NetworkLookUpCommand;
import se.wilmer.factory.command.commands.items.GetItemCommand;

@SuppressWarnings("UnstableApiUsage")
public class CommandManager {
    private final Factory plugin;

    public CommandManager(Factory plugin) {
        this.plugin = plugin;
    }

    public void register() {
        LiteralCommandNode<CommandSourceStack> advancedCommandRoot = Commands.literal("factory")
                .then(Commands.literal("debug")
                        .then(new BlockLookUpCommand(plugin).createCommand())
                        .then(new NetworkLookUpCommand(plugin).createCommand())
                        .then(new NetworkClearCommand(plugin).createCommand())
                )
                .then(Commands.literal("items")
                        .then(new GetItemCommand(plugin).createCommand())
                )
                .build();

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(advancedCommandRoot);
        });
    }
}
