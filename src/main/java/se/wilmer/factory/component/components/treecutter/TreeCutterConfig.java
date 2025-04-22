package se.wilmer.factory.component.components.treecutter;

import org.spongepowered.configurate.ConfigurationNode;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentConfig;
import se.wilmer.factory.component.ComponentInfoSerializer;

import java.util.*;

public class TreeCutterConfig extends ComponentConfig<TreeCutter> {

    public TreeCutterConfig(Factory plugin, String id) {
        super(plugin, id);
    }

    @Override
    public List<TreeCutter> load() {
        Optional<ConfigurationNode> optionalNode = createNode();
        if (optionalNode.isEmpty()) {
            return List.of();
        }
        ConfigurationNode node = optionalNode.get();

        List<TreeCutter> treeCutters = new ArrayList<>();
        for (ConfigurationNode childNode : node.childrenMap().values()) {
            getTreeCutter(childNode).ifPresent(treeCutters::add);
        }
        return treeCutters;
    }

    private Optional<TreeCutter> getTreeCutter(ConfigurationNode node) {
        String id = String.valueOf(node.key());
        long maxEnergyConsumption = node.node("max-energy-consumption").getLong(-1);
        long cuttingTickInterval = node.node("cutting-tick-interval").getLong(-1);

        ComponentInfoSerializer infoSerializer = new ComponentInfoSerializer.Builder(node.node("info"))
                .energy(true)
                .deserialize();

        if (id == null || maxEnergyConsumption == -1 || cuttingTickInterval == -1) {
            plugin.getComponentLogger().warn("Did not found all configuration values for, {}, values: id: {}, maxEnergyConsumption: {}, cuttingTickInterval: {}", this.id, id, maxEnergyConsumption, cuttingTickInterval);
            return Optional.empty();
        }
        
        return Optional.of(new TreeCutter(plugin, id, infoSerializer, maxEnergyConsumption, cuttingTickInterval));
    }
}
