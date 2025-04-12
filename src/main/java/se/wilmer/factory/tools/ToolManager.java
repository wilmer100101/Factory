package se.wilmer.factory.tools;

import org.bukkit.NamespacedKey;
import se.wilmer.factory.Factory;

public class ToolManager {
    private final Factory plugin;
    private final NamespacedKey typeKey;
    private final ToolRegistry registry;

    public ToolManager(Factory plugin) {
        this.plugin = plugin;

        typeKey = new NamespacedKey(plugin, "tool_type");

        registry = new ToolRegistry(plugin);
    }

    public void load() {
        registry.register();
    }

    public NamespacedKey getTypeKey() {
        return typeKey;
    }
}
