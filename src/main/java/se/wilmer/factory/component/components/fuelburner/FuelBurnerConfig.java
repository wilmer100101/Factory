package se.wilmer.factory.component.components.fuelburner;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentConfig;
import se.wilmer.factory.component.ComponentInfoSerializer;

import java.util.*;

public class FuelBurnerConfig extends ComponentConfig<FuelBurner> {
    private static final ItemStack RESULT_ITEM = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);

    public FuelBurnerConfig(Factory plugin, String id) {
        super(plugin, id);
    }

    @Override
    public List<FuelBurner> load() {
        Optional<ConfigurationNode> optionalNode = createNode();
        if (optionalNode.isEmpty()) {
            return List.of();
        }
        ConfigurationNode node = optionalNode.get();

        List<FuelBurner> fuelBurners = new ArrayList<>();
        for (ConfigurationNode childNode : node.childrenMap().values()) {
            getFuelBurner(childNode).ifPresent(fuelBurners::add);
        }
        return fuelBurners;
    }

    private Optional<FuelBurner> getFuelBurner(ConfigurationNode node) {
        String id = String.valueOf(node.key());
        ComponentInfoSerializer infoSerializer = new ComponentInfoSerializer.Builder(node.node("info"))
                .energy(true)
                .deserialize();

        if (id == null) {
            plugin.getComponentLogger().warn("Did not found id for, {}", this.id);
            return Optional.empty();
        }

        return Optional.of(new FuelBurner(plugin, id, infoSerializer, getEnergyRecipes(node.node("energy-recipes"), id)));
    }

    private Map<FurnaceRecipe, Long> getEnergyRecipes(ConfigurationNode node, String componentID) {
        Map<FurnaceRecipe, Long> energyRecipes = new HashMap<>();

        NamespacedKey namespacedKey = new NamespacedKey(plugin, componentID);

        for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
            String id = String.valueOf(entry.getKey());

            ConfigurationNode childNode = entry.getValue();
            String materialName = childNode.node("material").getString();
            int cookingDurationTicks = childNode.node("cooking-duration-ticks").getInt(-1);
            long suppliedEnergy = childNode.node("supplied-energy").getLong(-1);

            if (id == null || materialName == null || cookingDurationTicks == -1 || suppliedEnergy == -1) {
                plugin.getComponentLogger().warn("Did not found all recipe configuration values for, {}, values: id: {}, recipe-id: {}, cooking-duration-ticks: {}, supplied-energy: {}", this.id, id, materialName, cookingDurationTicks, suppliedEnergy);
                continue;
            }
            if (cookingDurationTicks < 2) {
                plugin.getComponentLogger().warn("cooking-duration-ticks needs to be more then 1, id: {}, recipe-id: {}", this.id, id);
                continue;
            }
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getComponentLogger().warn("Could not match material, while fetching material for: {}, got: {}", id, materialName);
                continue;
            }

            FurnaceRecipe cookingRecipe = new FurnaceRecipe(namespacedKey, RESULT_ITEM, material, 0, cookingDurationTicks);
            plugin.getServer().addRecipe(cookingRecipe);

            energyRecipes.put(cookingRecipe, suppliedEnergy);
        }

        return energyRecipes;
    }
}
