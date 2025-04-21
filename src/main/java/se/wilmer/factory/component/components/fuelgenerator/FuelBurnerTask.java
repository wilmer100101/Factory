package se.wilmer.factory.component.components.fuelgenerator;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;

public class FuelBurnerTask implements Runnable {
    private final FuelBurnerEntity fuelBurnerEntity;

    public FuelBurnerTask(FuelBurnerEntity fuelBurnerEntity) {
        this.fuelBurnerEntity = fuelBurnerEntity;
    }

    @Override
    public void run() {
        Block block = fuelBurnerEntity.getBlock();
        if (!(block.getState() instanceof Furnace furnace)) {
            return;
        }

        NamespacedKey namespacedKey = fuelBurnerEntity.getCurrentRecipeKey();
        if (namespacedKey == null || furnace.getCookTime() <= 0) {
            fuelBurnerEntity.setSuppliedEnergy(0);
            return;
        }

        fuelBurnerEntity.getComponent().getEnergyRecipes().entrySet().stream()
                .filter(energyRecipe -> energyRecipe.getKey().getKey().equals(namespacedKey))
                .findAny()
                .ifPresent(energyRecipe -> {
                    long suppliedEnergy = energyRecipe.getValue();
                    fuelBurnerEntity.setSuppliedEnergy(suppliedEnergy);
                });
    }
}
