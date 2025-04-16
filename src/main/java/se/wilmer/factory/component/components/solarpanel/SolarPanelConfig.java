package se.wilmer.factory.component.components.solarpanel;

import org.spongepowered.configurate.ConfigurationNode;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentConfig;
import se.wilmer.factory.component.ComponentInfoSerializer;
import se.wilmer.factory.component.components.treecutter.TreeCutter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SolarPanelConfig extends ComponentConfig<SolarPanel> {

    public SolarPanelConfig(Factory plugin, String id) {
        super(plugin, id);
    }

    @Override
    public List<SolarPanel> load() {
        Optional<ConfigurationNode> optionalNode = createNode();
        if (optionalNode.isEmpty()) {
            return List.of();
        }
        ConfigurationNode node = optionalNode.get();

        List<SolarPanel> solarPanels = new ArrayList<>();
        for (ConfigurationNode childNode : node.childrenMap().values()) {
            getSolarPanel(childNode).ifPresent(solarPanels::add);
        }
        return solarPanels;
    }

    private Optional<SolarPanel> getSolarPanel(ConfigurationNode node) {
        String id = String.valueOf(node.key());
        long suppliedEnergy = node.node("supplied-energy").getLong(-1);

        ComponentInfoSerializer infoSerializer = new ComponentInfoSerializer.Builder(node.node("info"))
                .energy(true)
                .deserialize();

        if (id == null || suppliedEnergy == -1) {
            plugin.getComponentLogger().warn("Did not found all configuration values for, {}, values: id: {}, suppliedEnergy: {}", this.id, id, suppliedEnergy);
            return Optional.empty();
        }

        return Optional.of(new SolarPanel(plugin, id, infoSerializer, suppliedEnergy));
    }
}
