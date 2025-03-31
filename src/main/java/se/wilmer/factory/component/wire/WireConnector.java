package se.wilmer.factory.component.wire;

import io.papermc.paper.entity.Leashable;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
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
        boolean connected = plugin.getEnergyNetworkManager().getConnector().connectComponents(firstComponent, secondComponent).join();
        if (!connected) {
            return CompletableFuture.completedFuture(false);
        }

        firstComponent.load();
        secondComponent.load();

        Block firstBlock = firstComponent.getBlock();
        Block secondBlock = secondComponent.getBlock();

        World world = firstBlock.getWorld();
        Frog firstEntity = world.spawn(firstBlock.getLocation(), Frog.class, frog -> {
            frog.setSilent(true);
            frog.setAI(false);
            frog.setGravity(false);
            frog.setInvulnerable(true);
        });

        Frog secondEntity = world.spawn(secondBlock.getLocation(), Frog.class, frog -> {
            frog.setSilent(true);
            frog.setAI(false);
            frog.setGravity(false);
            frog.setInvulnerable(true);
        });

        firstEntity.setLeashHolder(secondEntity);

        ComponentData firstComponentData = firstComponent.getData();
        Map<UUID, Wire> firstConnections = firstComponentData.getConnections();
        firstConnections.put(secondComponent.getUUID(), new Wire(firstEntity.getUniqueId(), secondEntity.getUniqueId()));
        firstComponentData.setConnections(firstConnections);

        ComponentData secondComponentData = secondComponent.getData();
        Map<UUID, Wire> secondConnections = secondComponentData.getConnections();
        secondConnections.put(firstComponent.getUUID(), new Wire(firstEntity.getUniqueId(), secondEntity.getUniqueId()));
        secondComponentData.setConnections(secondConnections);

        return CompletableFuture.completedFuture(true);
    }
}
