package se.wilmer.factory.item;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import se.wilmer.factory.Factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class ItemRegistry {
    protected Factory plugin;
    protected Path itemsFile;

    public ItemRegistry(Factory plugin, Path itemsFile) {
        this.plugin = plugin;
        this.itemsFile = itemsFile;
    }

    public abstract List<Item> fetchItems();

    protected ConfigurationNode createConfigurationNode() {
        Path resourcePath = plugin.getDataPath().resolve(itemsFile);
        if (Files.notExists(resourcePath)) {
            plugin.saveResource(itemsFile.toString(), false);
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(resourcePath)
                .build();

        ConfigurationNode node;
        try {
            node = loader.load();
        } catch (IOException e) {
            plugin.getComponentLogger().error("Failed to load items file", e);
            return null;
        }
        return node;
    }
}