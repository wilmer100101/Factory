package se.wilmer.factory.component.components.blockbreaker;

import org.bukkit.Material;
import org.spongepowered.configurate.ConfigurationNode;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentConfig;
import se.wilmer.factory.component.ComponentInfoSerializer;

import java.util.*;

public class BlockBreakerConfig extends ComponentConfig<BlockBreaker> {

    public BlockBreakerConfig(Factory plugin, String id) {
        super(plugin, id);
    }

    @Override
    public List<BlockBreaker> load() {
        Optional<ConfigurationNode> optionalNode = createNode();
        if (optionalNode.isEmpty()) {
            return List.of();
        }
        ConfigurationNode node = optionalNode.get();

        List<BlockBreaker> blockBreakers = new ArrayList<>();
        for (ConfigurationNode childNode : node.childrenMap().values()) {
            getBlockBreaker(childNode).ifPresent(blockBreakers::add);
        }
        return blockBreakers;
    }

    private Optional<BlockBreaker> getBlockBreaker(ConfigurationNode node) {
        String id = String.valueOf(node.key());
        long maxEnergyConsumption = node.node("max-energy-consumption").getLong(-1);
        Map<Material, Long> materialBlocksDuration = getMaterialBlocksDuration(node.node("material-blocks-duration").childrenMap());

        ComponentInfoSerializer infoSerializer = new ComponentInfoSerializer.Builder(node.node("info"))
                .energy(true)
                .deserialize();

        if (id == null || maxEnergyConsumption == -1 || materialBlocksDuration.isEmpty()) {
            plugin.getComponentLogger().warn("Did not found all configuration values for, {}, values: id: {}, maxEnergyConsumption: {}, materialBlocksDuration: {}", this.id, id, maxEnergyConsumption, materialBlocksDuration);
            return Optional.empty();
        }

        return Optional.of(new BlockBreaker(plugin, id, infoSerializer, maxEnergyConsumption, materialBlocksDuration));
    }

    private Map<Material, Long> getMaterialBlocksDuration(Map<Object, ? extends ConfigurationNode> map) {
        Map<Material, Long> materialBlocksDuration = new HashMap<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
            String materialName = String.valueOf(entry.getKey());
            if (materialName == null) {
                plugin.getComponentLogger().warn("Could not get materialName, while fetching material-block-duration for: {}", id);
                continue;
            }
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getComponentLogger().warn("Could not match material, while fetching material-block-duration for: {}, materialName: {}", id, materialName);
                continue;
            }

            long duration = entry.getValue().getLong(-1);
            if (duration == -1) {
                plugin.getComponentLogger().warn("Could not get duration, while fetching material-block-duration for: {}", id);
                continue;
            }
            materialBlocksDuration.put(material, duration);
        }
        return materialBlocksDuration;
    }
}
