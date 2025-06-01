package se.wilmer.factory.component.wire;

import org.bukkit.Location;
import org.bukkit.World;
import se.wilmer.factory.Factory;

public class WireManager {
    private final Factory plugin;
    private final WireConnector wireConnector;
    private final WireDisconnector wireDisconnector;
    private final WireSelector wireSelector;
    private final WireDisplay wireDisplay;

    public WireManager(Factory plugin) {
        this.plugin = plugin;

        wireConnector = new WireConnector(plugin);
        wireDisconnector = new WireDisconnector(plugin);
        wireSelector = new WireSelector(plugin, this);
        wireDisplay = new WireDisplay(plugin);
    }

    public void load() {
        wireDisplay.load();
    }

    public boolean isWireOutOfRange(Location firstBlockLocation, Location secondBlockLocation) {
        World world = firstBlockLocation.getWorld();
        if (!world.equals(secondBlockLocation.getWorld())) {
            return false;
        }

        return !(firstBlockLocation.distanceSquared(secondBlockLocation) > 100);
    }

    public WireConnector getWireConnector() {
        return wireConnector;
    }

    public WireDisconnector getWireDisconnector() {
        return wireDisconnector;
    }

    public WireSelector getWireSelector() {
        return wireSelector;
    }

    public WireDisplay getWireDisplay() {
        return wireDisplay;
    }
}
