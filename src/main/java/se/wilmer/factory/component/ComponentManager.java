package se.wilmer.factory.component;

import org.bukkit.NamespacedKey;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.wire.WireManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ComponentManager {
    private final Factory plugin;
    private final List<ComponentEntity<?>> componentEntities = new ArrayList<>();
    private final NamespacedKey uuidKey;
    private final NamespacedKey typeKey;
    private final NamespacedKey connectionsKey;
    private final ComponentRegistry registry;
    private final ComponentLoader loader;
    private final ComponentItemConverter itemConverter;
    private final WireManager wireManager;

    public ComponentManager(Factory plugin) {
        this.plugin = plugin;

        uuidKey = new NamespacedKey(plugin, "component_uuid");
        typeKey = new NamespacedKey(plugin, "type_uuid");
        connectionsKey = new NamespacedKey(plugin, "component_connections");
        loader = new ComponentLoader(plugin, this);
        itemConverter = new ComponentItemConverter(plugin, this);
        registry = new ComponentRegistry(plugin);
        wireManager = new WireManager(plugin);
    }

    public Optional<ComponentEntity<?>> getComponentEntity(UUID uuid) {
        return componentEntities.stream()
                .filter(componentEntity -> componentEntity.getUUID().equals(uuid))
                .findAny();
    }

    public void load() {
        plugin.getServer().getPluginManager().registerEvents(new ComponentListener(this), plugin);

        registry.register();
    }

    public void unload() {

    }

    public ComponentLoader getLoader() {
        return loader;
    }

    public ComponentRegistry getRegistry() {
        return registry;
    }

    public NamespacedKey getConnectionsKey() {
        return connectionsKey;
    }

    public NamespacedKey getTypeKey() {
        return typeKey;
    }

    public NamespacedKey getUUIDKey() {
        return uuidKey;
    }

    public ComponentItemConverter getItemConverter() {
        return itemConverter;
    }

    public WireManager getWireManager() {
        return wireManager;
    }

    public List<ComponentEntity<?>> getComponentEntities() {
        return componentEntities;
    }
}
