package se.wilmer.factory.component.components.fuelgenerator;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfo;
import se.wilmer.factory.energy.EnergyNetwork;
import se.wilmer.factory.energy.EnergySupplier;

public class FuelBurnerEntity extends ComponentEntity<FuelBurner> implements EnergySupplier {
    private final FuelBurnerData data;
    private final BukkitTask task;
    private ComponentInfo componentInfo = null;
    private long suppliedEnergy = 0L;
    private NamespacedKey currentRecipeKey = null;

    public FuelBurnerEntity(Factory plugin, FuelBurner component, FuelBurnerData data, Block block) {
        super(plugin, component, block);

        this.data = data;

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, new FuelBurnerTask(this), 0L, 0L);
    }

    @Override
    public void spawn() {
        componentInfo = new ComponentInfo(component.getComponentInfoSerializer(), this);
        componentInfo.spawn(block.getLocation());
    }

    @Override
    public void despawn() {
        if (task != null) {
            task.cancel();
        }
        if (componentInfo != null) {
            componentInfo.despawn(block.getWorld());
        }
    }

    @Override
    public void onBlockChange() {
        if (componentInfo != null) {
            componentInfo.updateLocation();
        }
    }

    @Override
    public FuelBurnerData getData() {
        return data;
    }

    @Override
    public long getSuppliedEnergy() {
        return suppliedEnergy;
    }

    @Override
    public long getMaxSuppliedEnergy() {
        return component.getMaxSuppliedEnergy();
    }

    @Override
    public void setSuppliedEnergy(long energy) {
        if (energy == suppliedEnergy) {
            return;
        }
        this.suppliedEnergy = energy;

        plugin.getEnergyNetworkManager().getComponentFromLoadedNetworks(this).ifPresent(EnergyNetwork::requestEnergyNetworkUpdate);

        if (componentInfo != null) {
            componentInfo.updateEnergy(suppliedEnergy, getMaxSuppliedEnergy());
        }
    }

    public void setCurrentRecipeKey(NamespacedKey currentRecipeKey) {
        this.currentRecipeKey = currentRecipeKey;
    }

    public NamespacedKey getCurrentRecipeKey() {
        return currentRecipeKey;
    }
}
