package se.wilmer.factory.component.wire;

import se.wilmer.factory.Factory;

public class WireManager {
    private final Factory plugin;
    private final WireConnector wireConnector;
    private final WireDisconnector wireDisconnector;
    private final WireSelector wireSelector;

    public WireManager(Factory plugin) {
        this.plugin = plugin;

        wireConnector = new WireConnector(plugin);
        wireDisconnector = new WireDisconnector(plugin);
        wireSelector = new WireSelector(plugin, this);
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
}
