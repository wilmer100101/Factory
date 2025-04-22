package se.wilmer.factory.component.components.solarpanel;

import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfo;
import se.wilmer.factory.energy.EnergyNetwork;
import se.wilmer.factory.energy.EnergySupplier;

public class SolarPanelEntity extends ComponentEntity<SolarPanel> implements EnergySupplier {
    private final SolarPanelData data;
    private BukkitTask task = null;
    private long suppliedEnergy = 0L;

    public SolarPanelEntity(Factory plugin, SolarPanel component, SolarPanelData data, Block block) {
        super(plugin, component, block);

        this.data = data;
    }

    @Override
    public void spawn() {
        super.spawn();

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, new SolarPanelTask(plugin, this), 0L, 0L);
    }

    @Override
    public void despawn() {
        super.despawn();

        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public SolarPanelData getData() {
        return data;
    }

    @Override
    public long getSuppliedEnergy() {
        return suppliedEnergy;
    }

    @Override
    public long getMaxSuppliedEnergy() {
        return component.getSuppliedEnergy();
    }

    @Override
    public void setSuppliedEnergy(long energy) {
        if (energy == suppliedEnergy) {
            return;
        }
        this.suppliedEnergy = energy;

        plugin.getEnergyNetworkManager().getComponentFromLoadedNetworks(this).ifPresent(EnergyNetwork::requestEnergyNetworkUpdate);

        componentInfo.updateEnergy(suppliedEnergy, getMaxSuppliedEnergy());
    }
}
