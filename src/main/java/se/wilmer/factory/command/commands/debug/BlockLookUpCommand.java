package se.wilmer.factory.command.commands.debug;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.wire.Wire;
import se.wilmer.factory.component.wire.WireDataType;
import se.wilmer.factory.energy.EnergyNetworkManager;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class BlockLookUpCommand {
    private final Factory plugin;

    public BlockLookUpCommand(Factory plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("blocklookup")
                .requires(sender -> sender.getExecutor() instanceof Player player && player.hasPermission("factory.command.debug.blocklookup"))
                .executes(this::retrieveBlockData);
    }

    private int retrieveBlockData(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return Command.SINGLE_SUCCESS;
        }

        Block block = player.getTargetBlockExact(50);
        if (block == null) {
            player.sendMessage(Component.text("No block found in a distance of 50 blocks").color(NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Component loadingComponentText = Component.text("Loading BlockLookUpData...", NamedTextColor.GREEN);
        player.sendMessage(loadingComponentText);
        plugin.getComponentLogger().info(loadingComponentText);

        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        String componentType = customBlockData.get(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING);
        UUID componentUUID = customBlockData.get(plugin.getComponentManager().getUUIDKey(), DataType.UUID);
        Map<UUID, Wire> connectedWires = customBlockData.get(plugin.getComponentManager().getConnectionsKey(), DataType.asMap(DataType.UUID, new WireDataType()));
        UUID info = customBlockData.get(plugin.getComponentManager().getInfoKey(), DataType.UUID);

        Component initialTextComponents = getInitialTextComponents(componentType, componentUUID, connectedWires, info);
        player.sendMessage(initialTextComponents);
        plugin.getComponentLogger().info(initialTextComponents);

        EnergyNetworkManager energyNetworkManager = plugin.getEnergyNetworkManager();
        plugin.getEnergyNetworkManager().getEnergyComponent(componentUUID).ifPresent(energyComponent -> energyNetworkManager.getComponentFromLoadedNetworks(energyComponent).ifPresent(energyNetwork -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            UUID energyComponentUUID = energyComponent.getUUID();
            Optional<List<UUID>> optionalConnectedComponents = energyNetwork.getComponentsConnections(energyComponentUUID);

            Component textComponent = Component.text("EnergyUUID:", NamedTextColor.GRAY).appendSpace().append(Component.text(energyComponentUUID.toString(), NamedTextColor.YELLOW))
                    .appendNewline()
                    .append(Component.text("ConnectedComponents:", NamedTextColor.GRAY).appendSpace().append(Component.text(optionalConnectedComponents.isEmpty() ? "empty" : "", NamedTextColor.YELLOW)));

            if (optionalConnectedComponents.isPresent()) {
                for (UUID uuid : optionalConnectedComponents.get()) {
                    textComponent = textComponent.appendNewline().append(Component.text("-", NamedTextColor.GRAY).appendSpace().append(Component.text(uuid.toString(), NamedTextColor.YELLOW)));
                }
            }

            player.sendMessage(textComponent);
            plugin.getComponentLogger().info(textComponent);
        })));

        return Command.SINGLE_SUCCESS;
    }

    private Component getInitialTextComponents(String componentType, UUID componentUUID, Map<UUID, Wire> connectedWires, UUID info) {
        Component blockLookUpText = Component.text("ComponentType:", NamedTextColor.GRAY).appendSpace().append(Component.text(componentType == null ? "null" : componentType, NamedTextColor.YELLOW))
                .appendNewline()
                .append(Component.text("ComponentUUID:", NamedTextColor.GRAY).appendSpace().append(Component.text(componentUUID == null ? "null" : componentUUID.toString(), NamedTextColor.YELLOW)))
                .appendNewline()
                .append(Component.text("Info:", NamedTextColor.GRAY).appendSpace().append(Component.text(info == null ? "null" : info.toString(), NamedTextColor.YELLOW)))
                .appendNewline()
                .append(Component.text("ConnectedWires:", NamedTextColor.GRAY).appendSpace().append(Component.text(connectedWires == null ? "null" : "", NamedTextColor.YELLOW)));

        if (connectedWires != null) {
            for (Map.Entry<UUID, Wire> uuidWireEntry : connectedWires.entrySet()) {
                UUID connectedComponentUUID = uuidWireEntry.getKey();
                Wire connectedWire = uuidWireEntry.getValue();

                blockLookUpText = blockLookUpText.append(Component.text().appendNewline()
                        .append(Component.text("-", NamedTextColor.GRAY).appendSpace().append(Component.text(connectedComponentUUID.toString(), NamedTextColor.YELLOW)))
                        .appendNewline()
                        .append(Component.text().appendSpace().appendSpace().append(Component.text("-", NamedTextColor.GRAY)).appendSpace().append(Component.text(connectedWire.firstEntityUUID().toString(), NamedTextColor.YELLOW)))
                        .appendNewline()
                        .append(Component.text().appendSpace().appendSpace().append(Component.text("-", NamedTextColor.GRAY)).appendSpace().append(Component.text(connectedWire.secondEntityUUID().toString(), NamedTextColor.YELLOW))));
            }
        }

        return blockLookUpText;
    }
}
