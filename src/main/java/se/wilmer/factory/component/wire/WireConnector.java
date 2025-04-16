package se.wilmer.factory.component.wire;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Frog;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentData;
import se.wilmer.factory.component.ComponentEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WireConnector {
    private final Factory plugin;

    public WireConnector(Factory plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Boolean> connectComponents(ComponentEntity<?> firstComponent, ComponentEntity<?> secondComponent) {
        return plugin.getEnergyNetworkManager().getConnector().connectComponents(firstComponent, secondComponent).thenApply(connected -> {
            if (!connected) {
                return false;
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                World world = firstComponent.getBlock().getWorld();
                Frog firstEntity = spawnWireEntity(world, firstComponent);
                Frog secondEntity = spawnWireEntity(world, secondComponent);

                firstEntity.setLeashHolder(secondEntity);

                updateComponentConnections(firstComponent, secondComponent, firstEntity.getUniqueId(), secondEntity.getUniqueId());
                updateComponentConnections(secondComponent, firstComponent, firstEntity.getUniqueId(), secondEntity.getUniqueId());
            });

            return true;
        });
    }

    private Frog spawnWireEntity(World world, ComponentEntity<?> component) {
        return world.spawn(component.getOffsetLocation().clone().subtract(0, 0.5, 0), Frog.class, frog -> {
            frog.setSilent(true);
            frog.setAI(false);
            frog.setGravity(false);
            frog.setInvulnerable(true);
            frog.setInvisible(true);
        });
    }

    private void updateComponentConnections(ComponentEntity<?> component, ComponentEntity<?> otherComponent, UUID firstEntityUUID, UUID secondEntityUUID) {
        ComponentData componentData = component.getData();
        Map<UUID, Wire> connections = componentData.getConnections();
        connections.put(otherComponent.getUUID(), new Wire(firstEntityUUID, secondEntityUUID));
        componentData.setConnections(connections);
    }
}
