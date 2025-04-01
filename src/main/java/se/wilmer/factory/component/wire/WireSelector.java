package se.wilmer.factory.component.wire;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentManager;

import java.util.*;

public class WireSelector {
    private static final ItemStack WIRE_ITEM = new ItemStack(Material.COPPER_INGOT);

    private final Factory plugin;
    private final WireManager wireManager;
    private final Map<UUID, CustomBlockData> firstSelectedList = new HashMap<>();

    public WireSelector(Factory plugin, WireManager wireManager) {
        this.plugin = plugin;
        this.wireManager = wireManager;
    }

    public void selectComponent(Player player, Block firstBlock, ItemStack mainHand) {
        if (!mainHand.isSimilar(WIRE_ITEM)) {
            return;
        }

        CustomBlockData firstCustomBlockData = new CustomBlockData(firstBlock, plugin);
        String firstType = firstCustomBlockData.get(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING);
        if (firstType == null) {
            return;
        }

        CustomBlockData secondCustomBlockData = firstSelectedList.get(player.getUniqueId());
        if (secondCustomBlockData == null) {
            wireManager.getWireDisplay().displayWire(player, firstBlock);
            firstSelectedList.put(player.getUniqueId(), firstCustomBlockData);
            return;
        }
        firstSelectedList.remove(player.getUniqueId());
        wireManager.getWireDisplay().removeWire(player);

        if (isDisconnectComponents(firstCustomBlockData, secondCustomBlockData)) {
            return;
        }

        String secondType = secondCustomBlockData.get(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING);
        if (secondType == null) {
            return;
        }

        List<Component> components = plugin.getComponentManager().getRegistry().getComponents();
        Optional<Component> firstComponent = findComponent(components, firstType);
        Optional<Component> secondComponent = findComponent(components, secondType);

        if (firstComponent.isEmpty() || secondComponent.isEmpty()) {
            return;
        }

        ComponentEntity<?> firstComponentEntity = firstComponent.get().createEntity(firstBlock);
        ComponentEntity<?> secondComponentEntity = secondComponent.get().createEntity(secondCustomBlockData.getBlock());

        wireManager.getWireConnector().connectComponents(firstComponentEntity, secondComponentEntity);
    }

    public void removePlayerSelection(UUID uuid) {
        firstSelectedList.remove(uuid);
    }

    private Optional<Component> findComponent(List<Component> components, String componentId) {
        return components.stream()
                .filter(component -> component.getId().equals(componentId))
                .findAny();
    }

    private boolean isDisconnectComponents(CustomBlockData firstCustomBlockData, CustomBlockData secondCustomBlockData) {
        ComponentManager componentManager = plugin.getComponentManager();
        NamespacedKey uuidKey = componentManager.getUUIDKey();

        UUID firstUUID = firstCustomBlockData.get(uuidKey, DataType.UUID);
        UUID secondUUID = secondCustomBlockData.get(uuidKey, DataType.UUID);

        if (firstUUID == null || secondUUID == null) {
            return false;
        }

        Optional<ComponentEntity<?>> firstComponent = componentManager.getComponentEntity(firstUUID);
        Optional<ComponentEntity<?>> secondComponent = componentManager.getComponentEntity(secondUUID);
        if (firstComponent.isEmpty() || secondComponent.isEmpty()) {
            return false;
        }

        return wireManager.getWireDisconnector().disconnectComponents(firstComponent.get(), secondComponent.get());
    }
}
