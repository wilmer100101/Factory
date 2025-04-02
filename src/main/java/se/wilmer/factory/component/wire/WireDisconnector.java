package se.wilmer.factory.component.wire;

import io.papermc.paper.entity.Leashable;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentData;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentManager;

import java.util.Map;
import java.util.UUID;

public class WireDisconnector {
    private final Factory plugin;

    public WireDisconnector(Factory plugin) {
        this.plugin = plugin;
    }

    public void disconnectAllComponents(ComponentEntity<?> firstComponent) {
        ComponentManager componentManager = plugin.getComponentManager();

        Map<UUID, Wire> connections = firstComponent.getData().getConnections();
        for (Map.Entry<UUID, Wire> entry : connections.entrySet()) {
            UUID secondUUID = entry.getKey();

            componentManager.getComponentEntity(secondUUID).ifPresent(secondComponent -> disconnectComponents(firstComponent, secondComponent));
        }

        firstComponent.unload();
    }

    public boolean disconnectComponents(ComponentEntity<?> firstComponent, ComponentEntity<?> secondComponent) {
        Wire wire = firstComponent.getData().getConnections().get(secondComponent.getUUID());
        if (wire == null) {
            return false;
        }

        World world = firstComponent.getBlock().getWorld();
        Entity firstEntity = world.getEntity(wire.firstEntityUUID());
        Entity secondEntity = world.getEntity(wire.secondEntityUUID());

        if (!(firstEntity instanceof Leashable firstLeashable) || secondEntity == null) {
            return false;
        }

        firstLeashable.setLeashHolder(null);
        secondEntity.remove();
        firstEntity.remove();

        plugin.getEnergyNetworkManager().getDisconnector().disconnectComponents(firstComponent, secondComponent);

        ComponentData firstData = firstComponent.getData();
        ComponentData secondData = secondComponent.getData();

        Map<UUID, Wire> firstConnections = firstData.getConnections();
        Map<UUID, Wire> secondConnections = secondData.getConnections();

        firstConnections.remove(secondComponent.getUUID());
        secondConnections.remove(firstComponent.getUUID());

        firstData.setConnections(firstConnections);
        secondData.setConnections(secondConnections);

        if (firstConnections.isEmpty()) {
            firstComponent.unload();
        }
        if (secondConnections.isEmpty()) {
            secondComponent.unload();
        }
        return true;
    }
}
