package se.wilmer.factory.component;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import com.jeff_media.morepersistentdatatypes.datatypes.UuidDataType;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import se.wilmer.factory.Factory;
import se.wilmer.factory.energy.EnergyNetworkManager;

import java.util.UUID;

public class ComponentLoader {
    private final Factory plugin;
    private final ComponentManager componentManager;

    public ComponentLoader(Factory plugin, ComponentManager componentManager) {
        this.plugin = plugin;
        this.componentManager = componentManager;
    }

    /**
     * Loads all the components in the chunk
     *
     * @param chunk The loaded chunk.
     */
    public void loadComponentsInChunk(Chunk chunk) {
        final NamespacedKey componentTypeKey = componentManager.getTypeKey();

        for (Block block : CustomBlockData.getBlocksWithCustomData(plugin, chunk)) {
            final PersistentDataContainer customBlockData = new CustomBlockData(block, plugin);

            String componentId = customBlockData.get(componentTypeKey, PersistentDataType.STRING);
            if (componentId == null) {
                continue;
            }

            componentManager.getRegistry().getComponents().stream()
                    .filter(component -> component.getId().equals(componentId))
                    .findAny()
                    .ifPresent(component ->  {
                        ComponentEntity<?> componentEntity = component.createEntity(block);
                        componentEntity.load();

                        plugin.getEnergyNetworkManager().loadComponent(componentEntity);
                    });
        }
    }

    /**
     * Unloads all the components in the chunk
     *
     * @param chunk The unloaded chunk.
     */
    public void unloadComponentsInChunk(Chunk chunk) {
        final EnergyNetworkManager energyNetworkManager = plugin.getEnergyNetworkManager();

        for (Block block : CustomBlockData.getBlocksWithCustomData(plugin, chunk)) {
            final PersistentDataContainer customBlockData = new CustomBlockData(block, plugin);

            UUID componentUUID = customBlockData.get(componentManager.getUUIDKey(), DataType.UUID);
            if (componentUUID == null) {
                continue;
            }

            componentManager.getComponentEntity(componentUUID).ifPresent(componentEntity -> {
                energyNetworkManager.unloadComponent(componentEntity);
                componentEntity.unload();
            });
        }
    }
}
