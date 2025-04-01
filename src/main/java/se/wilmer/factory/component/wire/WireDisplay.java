package se.wilmer.factory.component.wire;

import io.papermc.paper.entity.Leashable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import se.wilmer.factory.Factory;

import java.util.HashMap;
import java.util.Map;

public class WireDisplay {
    private final Map<Player, Wire> displayWires = new HashMap<>();

    public WireDisplay(Factory plugin) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateWirePositions, 1L, 1L);
    }

    public void displayWire(Player player, Block block) {
        World world = player.getWorld();

        Frog firstEntity = world.spawn(block.getLocation(), Frog.class, frog -> {
            frog.setSilent(true);
            frog.setAI(false);
            frog.setGravity(false);
            frog.setInvulnerable(true);
            frog.setVisibleByDefault(false);
        });

        Frog secondEntity = world.spawn(player.getLocation(), Frog.class, frog -> {
            frog.setSilent(true);
            frog.setAI(false);
            frog.setGravity(false);
            frog.setInvulnerable(true);
            frog.setVisibleByDefault(false);
        });

        firstEntity.setLeashHolder(secondEntity);
    }

    public void removeWire(Player player) {
        Wire wire = displayWires.get(player);
        if (wire == null) {
            return;
        }

        World world = player.getWorld();
        Entity firstEntity = world.getEntity(wire.firstEntityUUID());
        Entity secondEntity = world.getEntity(wire.secondEntityUUID());

        if (!(firstEntity instanceof Leashable firstLeashable) || secondEntity == null) {
            return;
        }

        firstLeashable.setLeashHolder(null);
        secondEntity.remove();
        firstEntity.remove();

        displayWires.remove(player);
    }

    public void updateWirePositions() {
        displayWires.forEach((player, wire) -> {
            Entity entity = player.getWorld().getEntity(wire.firstEntityUUID());
            if (entity != null) {
                entity.teleport(player);
            }
        });
    }
}
