package se.wilmer.factory.component.wire;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
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
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateDisplays, 1L, 10L);
    }

    public void displayFirstLocation(Player player, Location blockLocation) {
        player.sendActionBar(formatDisplay(blockLocation));
        displayLocations.put(player, blockLocation);
    }

    public void removeDisplay(Player player) {
        player.sendActionBar(Component.text());
        displayLocations.remove(player);
    }

    public void updateDisplays() {
        displayLocations.forEach((player, location) -> {
            player.sendActionBar(formatDisplay(location));
        });
    }

    private Component formatDisplay(Location location) {
        return Component.text("Linking from: " + location.x() + ", " + location.y() + ", " + location.z()).color(NamedTextColor.YELLOW);
    }
}
