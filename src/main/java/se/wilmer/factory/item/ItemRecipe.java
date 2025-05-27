package se.wilmer.factory.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import se.wilmer.factory.Factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemRecipe {
    private static final Path RECIPES_FILE = Path.of("items").resolve("recipes.yml");

    private final Factory plugin;
    private final ItemManager itemManager;

    public ItemRecipe(Factory plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
    }

    public void registerRecipes() {
        Path resourcePath = plugin.getDataPath().resolve(RECIPES_FILE);
        if (Files.notExists(resourcePath)) {
            plugin.saveResource(RECIPES_FILE.toString(), false);
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(resourcePath)
                .build();

        ConfigurationNode node;
        try {
            node = loader.load();
        } catch (IOException e) {
            plugin.getComponentLogger().error("Failed to load items file", e);
            return;
        }

        node.childrenMap().entrySet().stream()
                .flatMap(entry -> getRecipe(entry.getKey(), entry.getValue()).stream())
                .toList()
                .forEach(shapedRecipe -> plugin.getServer().addRecipe(shapedRecipe));
    }

    public Optional<ShapedRecipe> getRecipe(Object key, ConfigurationNode node) {
        if (!(key instanceof String id)) {
            plugin.getComponentLogger().error("Could not fetch recipe id");
            return Optional.empty();
        }

        NamespacedKey namespacedKey = new NamespacedKey(plugin, id);

        Optional<ItemStack> itemStack = getResultItem(node);
        if (itemStack.isEmpty()) {
            return Optional.empty();
        }

        ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, itemStack.get());
        shapedRecipe.shape(getPattern(node, namespacedKey));

        for (Map.Entry<Object, ? extends ConfigurationNode> keys : node.node("keys").childrenMap().entrySet()) {
            setIngredient(keys.getKey(), keys.getValue(), shapedRecipe, namespacedKey);
        }

        return Optional.of(shapedRecipe);
    }

    private @NotNull Optional<ItemStack> getResultItem(ConfigurationNode configurationNode) {
        String resultItemName = configurationNode.node("result").getString();
        if (resultItemName == null || resultItemName.isEmpty()) {
            plugin.getComponentLogger().error("Could not fetch result item name: {}", resultItemName);
            return Optional.empty();
        }
        Optional<ItemStack> itemStack = itemManager.getItemStackFromKey(resultItemName);
        if (itemStack.isEmpty()) {
            plugin.getComponentLogger().error("Could not fetch result item: {}", resultItemName);
            return Optional.empty();
        }
        return itemStack;
    }

    private String @NotNull [] getPattern(ConfigurationNode configurationNode, NamespacedKey namespacedKey) {
        List<? extends ConfigurationNode> patternNodeList = configurationNode.node("pattern").childrenList();

        String[] pattern = new String[patternNodeList.size()];
        for (int i = 0; i < patternNodeList.size(); i++) {
            ConfigurationNode patternNode = patternNodeList.get(i);
            String patternString = patternNode.getString();
            if (patternString == null) {
                plugin.getComponentLogger().error("Missing pattern for recipe: {}", namespacedKey);
                continue;
            }
            pattern[i] = patternString;
        }
        return pattern;
    }

    private void setIngredient(Object key, ConfigurationNode value, ShapedRecipe shapedRecipe, NamespacedKey namespacedKey) {
        char ingredientKey;

        if (key instanceof String character) {
            if (character.isEmpty()) {
                plugin.getComponentLogger().error("Ingredient character can not be empty, for recipe: {}, found: {}", namespacedKey, key);
                return;
            }
            ingredientKey = character.charAt(0);
        } else {
            plugin.getComponentLogger().error("Could not get ingredient character for recipe: {}, found: {}", namespacedKey, key);
            return;
        }

        String ingredientItemName = value.getString();
        if (ingredientItemName == null) {
            plugin.getComponentLogger().error("Missing ingredient item for recipe: {}", namespacedKey);
            return;
        }

        Optional<ItemStack> ingredientItem = itemManager.getItemStackFromKey(ingredientItemName);
        if (ingredientItem.isEmpty()) {
            plugin.getComponentLogger().error("Missing or invalid ingredient item for recipe: {}", namespacedKey);
            return;
        }
        shapedRecipe.setIngredient(ingredientKey, ingredientItem.get());
    }
}
