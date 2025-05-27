package se.wilmer.factory.command.commands.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.energy.EnergyComponent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class NetworkLookUpCommand {
    private final Factory plugin;

    public NetworkLookUpCommand(Factory plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("networklookup")
                .executes(this::retrieveNetworkData);
    }

    //FIXME: Just saying, this code is shit, and is only used for testing, and do not follow the standards.
    private int retrieveNetworkData(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        sender.sendMessage(Component.text("The information will only be logged in the console.", NamedTextColor.RED));

        plugin.getEnergyNetworkManager().getNetworks().forEach(network -> {
            plugin.getComponentLogger().error("Network: {}", network.getNetworkID());
            plugin.getComponentLogger().error("------ Connections ------");

            for (Map.Entry<UUID, List<UUID>> entry : network.getComponentsConnections().entrySet()) {
                UUID uuid = entry.getKey();
                List<UUID> components = entry.getValue();

                plugin.getComponentLogger().error("UUID: {}", uuid);
                for (UUID component : components) {
                    plugin.getComponentLogger().warn("- {}", component);
                }
            }

            plugin.getComponentLogger().error("------ Components ------");

            for (EnergyComponent component : network.getComponents()) {
                plugin.getComponentLogger().warn("- {}", component.getUUID());
            }

            plugin.getComponentLogger().error("------------------");
        });

        plugin.getComponentLogger().error("ALL Entities");
        for (ComponentEntity<?> componentEntity : plugin.getComponentManager().getComponentEntities()) {
            plugin.getComponentLogger().warn("- {}", componentEntity.getUUID());
        }
        plugin.getComponentLogger().error("Total: {}", plugin.getComponentManager().getComponentEntities().size());

        return Command.SINGLE_SUCCESS;
    }
}
