package se.wilmer.factory.component.wire;

import net.minecraft.util.datafix.fixes.EntityFallDistanceFloatToDoubleFix;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
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

    public boolean connectComponents(ComponentEntity<?> firstComponent, ComponentEntity<?> secondComponent) {
        if (!plugin.getEnergyNetworkManager().getConnector().connectComponents(firstComponent, secondComponent)) {
            return false;
        }

        World world = firstComponent.getBlock().getWorld();
        Frog firstEntity = spawnWireEntity(world, firstComponent);
        Frog secondEntity = spawnWireEntity(world, secondComponent);

        firstEntity.setLeashHolder(secondEntity);

        updateComponentConnections(firstComponent, secondComponent, firstEntity.getUniqueId(), secondEntity.getUniqueId());
        updateComponentConnections(secondComponent, firstComponent, firstEntity.getUniqueId(), secondEntity.getUniqueId());
        return true;
    }

    private Frog spawnWireEntity(World world, ComponentEntity<?> component) {
        return world.spawn(component.getOffsetLocation().clone().subtract(0, 0.5, 0), Frog.class, frog -> {
            frog.setSilent(true);
            frog.setAI(false);
            frog.setGravity(false);
            frog.setInvulnerable(true);
            frog.setInvisible(true);
            frog.setCollidable(false);

            AttributeInstance attribute = frog.getAttribute(Attribute.SCALE);
            if (attribute != null) {
                // Seems to be a bug, where the rope disappears if the entity it's connected to, is too small
                attribute.setBaseValue(0.5);
            }
        });
    }

    private void updateComponentConnections(ComponentEntity<?> component, ComponentEntity<?> otherComponent, UUID firstEntityUUID, UUID secondEntityUUID) {
        ComponentData componentData = component.getData();
        Map<UUID, Wire> connections = componentData.getConnections();
        connections.put(otherComponent.getUUID(), new Wire(firstEntityUUID, secondEntityUUID));
        componentData.setConnections(connections);
    }
}
