package se.wilmer.factory.component.components.fuelburner;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentEntity;

import java.util.Optional;
import java.util.UUID;

public class FuelBurnerListener implements Listener {
    private final Factory plugin;
    private final FuelBurner fuelBurner;

    public FuelBurnerListener(Factory plugin, FuelBurner fuelBurner) {
        this.plugin = plugin;
        this.fuelBurner = fuelBurner;
    }


    /**
     * Allows only recipes that is registered in the config.
     */
    @EventHandler
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent event) {
        NamespacedKey recipeKey = event.getRecipe().getKey();

        CustomBlockData customBlockData = new CustomBlockData(event.getBlock(), plugin);
        UUID uuid = customBlockData.get(plugin.getComponentManager().getUUIDKey(), DataType.UUID);
        String blockType = customBlockData.get(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING);
        if (uuid == null || blockType == null || !blockType.equals(fuelBurner.getId())) {
            return;
        }

        Optional<ComponentEntity<?>> optionalComponentEntity = plugin.getComponentManager().getComponentEntity(uuid);
        if (optionalComponentEntity.isEmpty() || !(optionalComponentEntity.get() instanceof FuelBurnerEntity fuelBurnerEntity)) {
            return;
        }

        if (fuelBurner.getEnergyRecipes().keySet().stream().noneMatch(recipe -> recipe.getKey().equals(recipeKey))) {
            event.setTotalCookTime(Integer.MAX_VALUE);
            fuelBurnerEntity.setCurrentRecipeKey(null);
            return;
        }

        fuelBurnerEntity.setCurrentRecipeKey(recipeKey);
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        CookingRecipe<?> recipe = event.getRecipe();
        if (recipe == null) {
            return;
        }

        CustomBlockData customBlockData = new CustomBlockData(event.getBlock(), plugin);
        String blockType = customBlockData.get(plugin.getComponentManager().getTypeKey(), DataType.STRING);
        UUID uuid = customBlockData.get(plugin.getComponentManager().getUUIDKey(), DataType.UUID);
        if (uuid == null || blockType == null || !blockType.equals(fuelBurner.getId())) {
            return;
        }

        NamespacedKey recipeKey = recipe.getKey();
        if (fuelBurner.getEnergyRecipes().keySet().stream().noneMatch(energyRecipe -> energyRecipe.getKey().equals(recipeKey))) {
            event.setCancelled(true);
            return;
        }

        event.setResult(new ItemStack(Material.AIR));

        plugin.getComponentManager().getComponentEntity(uuid).ifPresent(componentEntity -> {
            if (componentEntity instanceof FuelBurnerEntity fuelBurnerEntity) {
                fuelBurnerEntity.setCurrentRecipeKey(null);
            }
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlot() != 2 || !(event.getClickedInventory() instanceof FurnaceInventory furnaceInventory)) {
            return;
        }
        Furnace furnace = furnaceInventory.getHolder();
        if (furnace == null) {
            return;
        }
        CustomBlockData customBlockData = new CustomBlockData(furnace.getBlock(), plugin);
        String blockType = customBlockData.get(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING);
        if (blockType == null || !blockType.equals(fuelBurner.getId())) {
            return;
        }

        event.setCancelled(true);
    }
}
