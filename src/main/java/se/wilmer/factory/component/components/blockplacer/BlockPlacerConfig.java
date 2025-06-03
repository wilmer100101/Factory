package se.wilmer.factory.component.components.blockplacer;

import org.bukkit.Material;
import org.bukkit.block.Block;
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

        return Optional.of(new BlockPlacer(plugin, id, infoSerializer, maxEnergyConsumption, getAllowedMaterials(node, id)));
    }

    private List<Material> getAllowedMaterials(ConfigurationNode node, String id) {
        List<Material> allowedMaterials = new ArrayList<>();

        for (ConfigurationNode childrenNode : node.node("allowed-materials").childrenList()) {
            String materialName = childrenNode.getString();
            if (materialName == null) {
                plugin.getComponentLogger().warn("Could not get materialName, while fetching material-blocks for: {}", id);
                continue;
            }
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getComponentLogger().warn("Could not match material, while fetching material-blocks for: {}, materialName: {}", id, materialName);
                continue;
            }

            allowedMaterials.add(material);
        }

        return allowedMaterials;
    }
}
