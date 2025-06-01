package se.wilmer.factory.component.wire;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import se.wilmer.factory.Factory;

import java.util.HashMap;
import java.util.Map;

public class WireDisplay {
    private final Factory plugin;
    private final Map<Player, Location> displayLocations = new HashMap<>();

    public WireDisplay(Factory plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateDisplays, 0L, 0L);
    }

    public void displayFirstLocation(Player player, Location blockLocation) {
        player.sendActionBar(formatDisplay(player, blockLocation));
        displayLocations.put(player, blockLocation);
    }

    public void removeDisplay(Player player) {
        player.sendActionBar(Component.text());
        displayLocations.remove(player);
    }

    public void updateDisplays() {
        displayLocations.forEach((player, location) -> {
            player.sendActionBar(formatDisplay(player, location));
        });
    }

    private Component formatDisplay(Player player, Location location) {
        NamedTextColor namedTextColor = NamedTextColor.GREEN;
        if (!isWireValid(player, location)) {
            namedTextColor = NamedTextColor.RED;
        }

        return Component.text("Linking from: " + location.x() + ", " + location.y() + ", " + location.z(), namedTextColor);
    }

    private boolean isWireValid(Player player, Location location) {
        AttributeInstance attributeInstance = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        if (attributeInstance == null) {
            return false;
        }

        Block block = player.getTargetBlockExact((int) attributeInstance.getValue());
        if (block == null) {
            // Returns true here, as it does not touch anything, and it just looks better for the player.
            return true;
        }

        return plugin.getComponentManager().getWireManager().isWireOutOfRange(block.getLocation(), location);
    }
}
