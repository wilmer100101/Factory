package se.wilmer.factory.component;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import se.wilmer.factory.Factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public abstract class ComponentConfig<T extends Component> {
    private static final Path COMPONENTS_PATH = Path.of("components");

    protected final Factory plugin;
    protected final String id;

    public ComponentConfig(Factory plugin, String id) {
        this.plugin = plugin;
        this.id = id;
    }

    public abstract List<? extends T> load();

    public Optional<ConfigurationNode> createNode() {
        Path path = COMPONENTS_PATH.resolve(id + ".yml");
        Path resourcePath = plugin.getDataPath().resolve(path);
        if (Files.notExists(resourcePath)) {
            plugin.saveResource(path.toString(), false);
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(resourcePath)
                .build();

        ConfigurationNode node;
        try {
            node = loader.load();
        } catch (IOException e) {
            plugin.getComponentLogger().error("Failed to load component config", e);
            return Optional.empty();
        }

        return Optional.of(node);
    }
}
