package se.wilmer.factory.item;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spongepowered.configurate.ConfigurationNode;
import se.wilmer.factory.Factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemSerializer {
    private final Factory plugin;
    private final NamespacedKey itemKey;

    public ItemSerializer(Factory plugin) {
        this.plugin = plugin;

        itemKey = new NamespacedKey(plugin, "item");
    }

    public void saveItemToBlock(ItemStack itemStack, CustomBlockData customBlockData) {
        customBlockData.set(itemKey, DataType.ITEM_META, itemStack.getItemMeta());
    }

    public Optional<ItemStack> getItemFromBlock(CustomBlockData customBlockData) {
        ItemMeta itemMeta = customBlockData.get(itemKey, DataType.ITEM_META);
        Block block = customBlockData.getBlock();
        if (itemMeta == null || block == null) {
            return Optional.empty();
        }

        ItemStack newItemStack = ItemStack.of(block.getType());
        newItemStack.editMeta((meta) -> {
            meta.displayName(itemMeta.displayName());
            meta.lore(itemMeta.lore());
        });

        return Optional.of(newItemStack);
    }

    public Optional<ItemStack> getItemFromFile(ConfigurationNode configurationNode) {
        MiniMessage miniMessage = MiniMessage.miniMessage();

        String name = configurationNode.node("name").getString();
        if (name == null || name.isEmpty()) {
            plugin.getComponentLogger().error("No name provided, when trying to deserialize item!");
            return Optional.empty();
        }

        Optional<Material> optionalMaterial = getMaterial(configurationNode.node("material"), name);
        if (optionalMaterial.isEmpty()) {
            return Optional.empty();
        }

        ItemStack item = new ItemStack(optionalMaterial.get());
        item.editMeta(itemMeta -> {
            itemMeta.displayName(miniMessage.deserialize(name));
            itemMeta.lore(getLoreComponents(configurationNode, name, miniMessage));
        });
        return Optional.of(item);
    }

    private List<Component> getLoreComponents(ConfigurationNode configurationNode, String name, MiniMessage miniMessage) {
        List<Component> loreComponents = new ArrayList<>();
        for (ConfigurationNode node : configurationNode.node("lore").childrenList()) {
            String lore = node.getString();
            if (lore == null || lore.isEmpty()) {
                plugin.getComponentLogger().error("Missing lore, when trying to deserialize item: {}", name);
                continue;
            }
            loreComponents.add(miniMessage.deserialize(lore));
        }

        return loreComponents;
    }

    private Optional<Material> getMaterial(ConfigurationNode configurationNode, String name) {
        String materialName = configurationNode.getString();
        if (materialName == null || materialName.isEmpty()) {
            plugin.getComponentLogger().error("Missing material-name, when trying to deserialize item: {}", name);
            return Optional.empty();
        }
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            plugin.getComponentLogger().error("Invalid material name: {}", materialName);
            return Optional.empty();
        }
        return Optional.of(material);
    }
}
