package se.wilmer.factory.command.commands.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.energy.EnergyComponent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class NetworkClearCommand {
    private final Factory plugin;

    public NetworkClearCommand(Factory plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("networkclear")
                .requires(sender -> sender.getExecutor() instanceof Player player && player.hasPermission("factory.command.debug.networkclear"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();

                    sender.sendMessage(Component.text("Clearing network...", NamedTextColor.YELLOW));
                    plugin.getEnergyNetworkManager().getNetworks().clear();
                    plugin.getComponentManager().getComponentEntities().clear();
                    sender.sendMessage(Component.text("All networks, and component entities are now cleared!", NamedTextColor.GREEN));

                    return Command.SINGLE_SUCCESS;
                });
    }
}
