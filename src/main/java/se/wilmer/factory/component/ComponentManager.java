package se.wilmer.factory.component;

import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.wire.WireManager;
import se.wilmer.factory.item.registries.ItemComponentRegistry;

import java.util.*;

public class ComponentManager {
    private final Factory plugin;
    private final List<ComponentEntity<?>> componentEntities = new ArrayList<>();
    private final NamespacedKey uuidKey;
    private final NamespacedKey typeKey;
    private final NamespacedKey connectionsKey;
    private final NamespacedKey infoKey;
    private final ItemComponentRegistry itemConfig;
    private final ComponentRegistry registry;
    private final ComponentLoader loader;
    private final ComponentStateConverter itemConverter;
    private final WireManager wireManager;

    public ComponentManager(Factory plugin) {
        this.plugin = plugin;

        uuidKey = new NamespacedKey(plugin, "component_uuid");
        typeKey = new NamespacedKey(plugin, "component_type");
        connectionsKey = new NamespacedKey(plugin, "component_connections");
        infoKey = new NamespacedKey(plugin, "component_info");
        itemConfig = new ItemComponentRegistry(plugin);
        loader = new ComponentLoader(plugin, this);
        itemConverter = new ComponentStateConverter(plugin, this);
        registry = new ComponentRegistry(plugin);
        wireManager = new WireManager(plugin);
    }

    public Optional<ComponentEntity<?>> getComponentEntity(UUID uuid) {
        return componentEntities.stream()
                .filter(componentEntity -> componentEntity.getUUID().equals(uuid))
                .findAny();
    }

    public void load() {
        Server server = plugin.getServer();
        server.getPluginManager().registerEvents(new ComponentListener(this), plugin);

        wireManager.load();
        registry.register();

        server.getWorlds().forEach(world -> Arrays.stream(world.getLoadedChunks()).forEach(loader::loadComponentsInChunk));
    }

    public void unload() {
        plugin.getServer().getWorlds().forEach(world -> Arrays.stream(world.getLoadedChunks()).forEach(loader::unloadComponentsInChunk));
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

    public NamespacedKey getInfoKey() {
        return infoKey;
    }

    public ComponentStateConverter getItemConverter() {
        return itemConverter;
    }

    public WireManager getWireManager() {
        return wireManager;
    }

    public List<ComponentEntity<?>> getComponentEntities() {
        return componentEntities;
    }
}
