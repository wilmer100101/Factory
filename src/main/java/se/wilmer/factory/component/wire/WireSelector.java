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

    public void removePlayerSelection(UUID uuid) {
        firstSelectedList.remove(uuid);
    }

    public void selectComponent(Player player, Block firstBlock, ItemStack mainHand) {
        if (!mainHand.isSimilar(WIRE_ITEM)) {
            return;
        }

        CustomBlockData firstCustomBlockData = new CustomBlockData(firstBlock, plugin);
        CustomBlockData secondCustomBlockData = firstSelectedList.get(player.getUniqueId());
        if (secondCustomBlockData == null) {
            wireManager.getWireDisplay().displayFirstLocation(player, firstBlock.getLocation());
            firstSelectedList.put(player.getUniqueId(), firstCustomBlockData);
            return;
        }

        firstSelectedList.remove(player.getUniqueId());
        wireManager.getWireDisplay().removeDisplay(player);

        if (tryDisconnectComponents(firstCustomBlockData, secondCustomBlockData)) {
            return;
        }

        ComponentManager componentManager = plugin.getComponentManager();
        String firstType = firstCustomBlockData.get(componentManager.getTypeKey(), PersistentDataType.STRING);
        String secondType = secondCustomBlockData.get(componentManager.getTypeKey(), PersistentDataType.STRING);
        UUID firstUUID = firstCustomBlockData.get(componentManager.getUUIDKey(), DataType.UUID);
        UUID secondUUID = secondCustomBlockData.get(componentManager.getUUIDKey(), DataType.UUID);
        if (firstType == null || secondType == null || firstUUID == null || secondUUID == null) {
            return;
        }

        tryConnectComponents(firstBlock, firstUUID, secondUUID, firstType, secondType, secondCustomBlockData);
    }

    private void tryConnectComponents(Block firstBlock, UUID firstUUID, UUID secondUUID, String firstType, String secondType, CustomBlockData secondCustomBlockData) {
        ComponentManager componentManager = plugin.getComponentManager();
        List<Component> components = plugin.getComponentManager().getRegistry().getComponents();
        ComponentEntity<?> firstComponentEntity = componentManager.getComponentEntity(firstUUID).orElse(null);
        ComponentEntity<?> secondComponentEntity = componentManager.getComponentEntity(secondUUID).orElse(null);

        final boolean isCreateFirstEntity;
        final boolean isCreateSecondEntity;

        if (firstComponentEntity == null) {
            firstComponentEntity = createComponentEntity(components, firstType, firstBlock).orElse(null);
            isCreateFirstEntity = true;
        } else {
            isCreateFirstEntity = false;
        }

        if (secondComponentEntity == null) {
            secondComponentEntity = createComponentEntity(components, secondType, secondCustomBlockData.getBlock()).orElse(null);
            isCreateSecondEntity = true;
        } else {
            isCreateSecondEntity = false;
        }

        if (firstComponentEntity == null || secondComponentEntity == null) {
            return;
        }

        final ComponentEntity<?> finalFirstComponentEntity = firstComponentEntity;
        final ComponentEntity<?> finalSecondComponentEntity = secondComponentEntity;
        if (wireManager.getWireConnector().connectComponents(firstComponentEntity, secondComponentEntity)) {
            if (isCreateFirstEntity) {
                finalFirstComponentEntity.spawn();
            }
            if (isCreateSecondEntity) {
                finalSecondComponentEntity.spawn();
            }
        }
    }

    private Optional<ComponentEntity<?>> createComponentEntity(List<Component> components, String componentId, Block block) {
        Optional<Component> component = components.stream()
                .filter(c -> c.getId().equals(componentId))
                .findAny();

        return component.map(value -> value.createEntity(block));
    }

    private boolean tryDisconnectComponents(CustomBlockData firstCustomBlockData, CustomBlockData secondCustomBlockData) {
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
