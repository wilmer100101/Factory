package se.wilmer.factory.component.components.fuelburner;

import org.bukkit.block.Block;
import org.bukkit.inventory.FurnaceRecipe;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfoSerializer;

import java.util.Map;

public class FuelBurner extends Component {
    private final Map<FurnaceRecipe, Long> energyRecipes;
    private final long maxSuppliedEnergy;

    public FuelBurner(Factory plugin, String id, ComponentInfoSerializer componentInfoSerializer, Map<FurnaceRecipe, Long> energyRecipes) {
        super(plugin, id, componentInfoSerializer);

        this.energyRecipes = energyRecipes;
        this.maxSuppliedEnergy = energyRecipes.values().stream().max(Long::compareTo).orElse(0L);

        plugin.getServer().getPluginManager().registerEvents(new FuelBurnerListener(plugin, this), plugin);
    }

    @Override
    public ComponentEntity<FuelBurner> createEntity(Block block) {
        return new FuelBurnerEntity(plugin, this, new FuelBurnerData(plugin, block), block);
    }

    public Map<FurnaceRecipe, Long> getEnergyRecipes() {
        return energyRecipes;
    }

    public long getMaxSuppliedEnergy() {
        return maxSuppliedEnergy;
    }
}
