package se.wilmer.factory.component.components.blockplacer;

import org.spongepowered.configurate.ConfigurationNode;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentConfig;
import se.wilmer.factory.component.ComponentInfoSerializer;
import se.wilmer.factory.component.components.treecutter.TreeCutter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockPlacerConfig extends ComponentConfig<BlockPlacer> {

    public BlockPlacerConfig(Factory plugin, String id) {
        super(plugin, id);
    }

    @Override
    public List<BlockPlacer> load() {
        Optional<ConfigurationNode> optionalNode = createNode();
        if (optionalNode.isEmpty()) {
            return List.of();
        }
        ConfigurationNode node = optionalNode.get();

        List<BlockPlacer> blockPlacers = new ArrayList<>();
        for (ConfigurationNode childNode : node.childrenMap().values()) {
            getBlockPlacer(childNode).ifPresent(blockPlacers::add);
        }
        return blockPlacers;
    }

    private Optional<BlockPlacer> getBlockPlacer(ConfigurationNode node) {
        String id = String.valueOf(node.key());
        long maxEnergyConsumption = node.node("max-energy-consumption").getLong(-1);

        ComponentInfoSerializer infoSerializer = new ComponentInfoSerializer.Builder(node.node("info"))
                .energy(true)
                .deserialize();

        if (id == null || maxEnergyConsumption == -1) {
            plugin.getComponentLogger().warn("Did not found all configuration values for, {}, values: id: {}, maxEnergyConsumption: {}", this.id, id, maxEnergyConsumption);
            return Optional.empty();
        }

        return Optional.of(new BlockPlacer(plugin, id, infoSerializer, maxEnergyConsumption));
    }
}
