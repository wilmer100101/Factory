package se.wilmer.factory.component.wire;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentManager;

import java.util.*;
import java.util.logging.Level;

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


        //TODO: Try, and add more debugging to see what did not work

        CustomBlockData firstCustomBlockData = new CustomBlockData(firstBlock, plugin);
        String firstType = firstCustomBlockData.get(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING);
        if (firstType == null) {
            player.sendMessage("FirstType is null");
            return;
        }

        CustomBlockData secondCustomBlockData = firstSelectedList.get(player.getUniqueId());
        if (secondCustomBlockData == null) {
            firstSelectedList.put(player.getUniqueId(), firstCustomBlockData);
            player.sendMessage("Selected first");
            return;
        }
        firstSelectedList.remove(player.getUniqueId());
        player.sendMessage("Selected second");

        if (isDisconnectComponents(firstCustomBlockData, secondCustomBlockData)) {
            player.sendMessage("Might be disconnected");
            return;
        }

        String secondType = secondCustomBlockData.get(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING);
        if (secondType == null) {
            player.sendMessage("SecondType is null");
            return;
        }

        List<Component> components = plugin.getComponentManager().getRegistry().getComponents();
        Optional<Component> firstComponent = findComponent(components, firstType);
        Optional<Component> secondComponent = findComponent(components, secondType);

        if (firstComponent.isEmpty() || secondComponent.isEmpty()) {
            player.sendMessage("No component found");
            return;
        }

        ComponentEntity<?> firstComponentEntity = firstComponent.get().createEntity(firstBlock);
        ComponentEntity<?> secondComponentEntity = secondComponent.get().createEntity(secondCustomBlockData.getBlock());

        wireManager.getWireConnector().connectComponents(firstComponentEntity, secondComponentEntity).thenAccept(isConnected -> {
            player.sendMessage("Did connect? " + isConnected);
        });
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
            plugin.getLogger().log(Level.SEVERE, "First UUID and Second UUID are null");
            return false;
        }

        Optional<ComponentEntity<?>> firstComponent = componentManager.getComponentEntity(firstUUID);
        Optional<ComponentEntity<?>> secondComponent = componentManager.getComponentEntity(secondUUID);
        if (firstComponent.isEmpty() || secondComponent.isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "First and Second are empty");
            return false;
        }

        if (wireManager.getWireDisconnector().disconnectComponents(firstComponent.get(), secondComponent.get())) {
            plugin.getLogger().log(Level.INFO, "did connect (2)");
            return true;
        }
        //TODO: Disconnect components failar?
        return false;
    }
}
