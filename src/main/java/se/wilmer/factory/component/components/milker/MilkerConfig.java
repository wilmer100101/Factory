package se.wilmer.factory.component.components.milker;

import org.spongepowered.configurate.ConfigurationNode;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentConfig;
import se.wilmer.factory.component.ComponentInfoSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MilkerConfig extends ComponentConfig<Milker> {

    public MilkerConfig(Factory plugin, String id) {
        super(plugin, id);
    }

    @Override
    public List<Milker> load() {
        Optional<ConfigurationNode> optionalNode = createNode();
        if (optionalNode.isEmpty()) {
            return List.of();
        }
        ConfigurationNode node = optionalNode.get();

        List<Milker> milkers = new ArrayList<>();
        for (ConfigurationNode childNode : node.childrenMap().values()) {
            getMilker(childNode).ifPresent(milkers::add);
        }
        return milkers;
    }

    private Optional<Milker> getMilker(ConfigurationNode node) {
        String id = String.valueOf(node.key());
        long maxEnergyConsumption = node.node("max-energy-consumption").getLong(-1);
        long milkingTickInterval = node.node("milking-tick-interval").getLong(-1);

        ComponentInfoSerializer infoSerializer = new ComponentInfoSerializer.Builder(node.node("info"))
                .energy(true)
                .deserialize();

        if (id == null || maxEnergyConsumption == -1 || milkingTickInterval == -1) {
            plugin.getComponentLogger().warn("Did not found all configuration values for, {}, values: id: {}, maxEnergyConsumption: {}, milkingTickInterval: {}", this.id, id, maxEnergyConsumption, milkingTickInterval);
            return Optional.empty();
        }

        return Optional.of(new Milker(plugin, id, infoSerializer, maxEnergyConsumption, milkingTickInterval));
    }
}
