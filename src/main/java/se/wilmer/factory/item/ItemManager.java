package se.wilmer.factory.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import se.wilmer.factory.Factory;
import se.wilmer.factory.item.registries.ItemComponentRegistry;
import se.wilmer.factory.item.registries.ItemDefaultRegistry;
import se.wilmer.factory.item.registries.ItemToolRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemManager {
    private final Factory plugin;
    private final List<Item> items;
    private final List<ItemRegistry> registries;
    private final ItemRecipe recipe;
    private final ItemSerializer serializer;

    public ItemManager(Factory plugin) {
        this.plugin = plugin;

        items = new ArrayList<>();
        recipe = new ItemRecipe(plugin, this);
        serializer = new ItemSerializer(plugin);

        registries = List.of(
                new ItemToolRegistry(plugin),
                new ItemComponentRegistry(plugin),
                new ItemDefaultRegistry(plugin)
        );
    }

    public void register() {
        registries.forEach(registry -> items.addAll(registry.fetchItems()));
        recipe.registerRecipes();
    }

    public Optional<Item> getItem(NamespacedKey namespacedKey) {
        return items.stream()
                .filter(i -> i.namespacedKey().equals(namespacedKey))
                .findFirst();
    }

    /**
     * Tries to get the ItemStack, by reading the key.
     * It can either be a normal Minecraft item, or a custom item by this plugin.
     *
     * @param key The raw item key.
     * @return An optional ItemStack that might be a Minecraft item or a custom item by this plugin.
     */
    public Optional<ItemStack> getItemStackFromKey(String key) {
        Material material = Material.matchMaterial(key);
        if (material != null) {
            return Optional.of(new ItemStack(material));
        }

        NamespacedKey namespacedKey = NamespacedKey.fromString(key);
        Optional<Item> item = getItem(namespacedKey);
        return item.map(Item::itemStack);
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<NamespacedKey> getKeys() {
        return items.stream()
                .map(Item::namespacedKey)
                .collect(Collectors.toList());
    }

    public ItemRecipe getRecipe() {
        return recipe;
    }

    public ItemSerializer getSerializer() {
        return serializer;
    }

}
