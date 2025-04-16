package se.wilmer.factory.component.components.solarpanel;

import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfo;
import se.wilmer.factory.energy.EnergySupplier;

import java.util.Optional;
import java.util.UUID;

public class SolarPanelEntity extends ComponentEntity<SolarPanel> implements EnergySupplier {
    private final SolarPanelData data;
    private ComponentInfo componentInfo = null;
    private BukkitTask task = null;
    private long suppliedEnergy = 0L;

    public SolarPanelEntity(Factory plugin, SolarPanel component, SolarPanelData data, Block block) {
        super(plugin, component, block);

        this.data = data;
    }

    @Override
    public void spawn() {
        componentInfo = new ComponentInfo(component.getComponentInfoSerializer(), this);
        componentInfo.spawn(block.getLocation());

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, new SolarPanelTask(plugin, this), 0L, 0L);
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
        this.suppliedEnergy = energy;

        if (componentInfo != null) {
            componentInfo.updateEnergy(suppliedEnergy, getMaxSuppliedEnergy());
        }
    }
}
