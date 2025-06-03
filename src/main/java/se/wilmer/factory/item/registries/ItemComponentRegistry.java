package se.wilmer.factory.item.registries;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.spongepowered.configurate.ConfigurationNode;
import se.wilmer.factory.Factory;
import se.wilmer.factory.item.Item;
import se.wilmer.factory.item.ItemRegistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemComponentRegistry extends ItemRegistry {
    private static final Path COMPONENTS_ITEMS_FILE = Path.of("items").resolve("component_items.yml");

    public ItemComponentRegistry(Factory plugin) {
        super(plugin, COMPONENTS_ITEMS_FILE);
    }

    public List<Item> fetchItems() {
        final ConfigurationNode node = createConfigurationNode();
        if (node == null) {
            return new ArrayList<>();
        }

        return node.childrenMap().entrySet().stream()
                .flatMap(entry -> getComponentItemStack(entry.getKey(), entry.getValue()).stream())
                .toList();
    }

    private Optional<Item> getComponentItemStack(Object key, ConfigurationNode value) {
        Optional<ItemStack> optionalItemStack = plugin.getItemManager().getSerializer().getItemFromFile(value);
        if (optionalItemStack.isEmpty()) {
            return Optional.empty();
        }
        ItemStack itemStack = optionalItemStack.get();

        if (!(key instanceof String itemId)) {
            plugin.getComponentLogger().error("Could not get componentId, while fetching component items");
            return Optional.empty();
        }

        String componentId = value.node("component-id").getString();
        if (componentId == null || componentId.isEmpty()) {
            plugin.getComponentLogger().error("Could not get componentId, while fetching component items");
            return Optional.empty();
        }

        itemStack.editPersistentDataContainer(pdc -> pdc.set(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING, componentId));

        return Optional.of(new Item(itemStack, new NamespacedKey(plugin, itemId)));
    }
}
