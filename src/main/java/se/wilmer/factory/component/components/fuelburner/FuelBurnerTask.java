package se.wilmer.factory.component.components.fuelburner;

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
        NamespacedKey namespacedKey = fuelBurnerEntity.getCurrentRecipeKey();
        if (namespacedKey == null) {
            fuelBurnerEntity.setSuppliedEnergy(0);
            return;
        }

        Block block = fuelBurnerEntity.getBlock();
        if (!(block.getState() instanceof Furnace furnace) || furnace.getCookTime() <= 0) {
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
