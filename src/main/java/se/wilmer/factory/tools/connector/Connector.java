package se.wilmer.factory.tools.connector;

import se.wilmer.factory.Factory;
import se.wilmer.factory.tools.Tool;

public class Connector extends Tool {
    private final Factory plugin;

    public Connector(Factory plugin) {
        super(plugin, "connector");

        this.plugin = plugin;
    }

    @Override
    public void load() {
        plugin.getServer().getPluginManager().registerEvents(new ConnectorListener(plugin, this), plugin);
    }
}