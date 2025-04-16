package se.wilmer.factory.component.components.solarpanel;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.DaylightDetector;
import se.wilmer.factory.Factory;
import se.wilmer.factory.energy.EnergyNetwork;
import se.wilmer.factory.energy.EnergyNetworkManager;

import java.util.Optional;


public class SolarPanelTask implements Runnable {
    private final Factory plugin;
    private final SolarPanelEntity solarPanelEntity;
    private boolean isDay = false;

    public SolarPanelTask(Factory plugin, SolarPanelEntity solarPanelEntity) {
        this.plugin = plugin;
        this.solarPanelEntity = solarPanelEntity;
    }

    @Override
    public void run() {
        EnergyNetworkManager energyNetworkManager = plugin.getEnergyNetworkManager();

        World world = solarPanelEntity.getBlock().getWorld();
        final boolean isDayTime = world.isDayTime();

        if (isDayTime && !isDay) {
            isDay = true;

            solarPanelEntity.setSuppliedEnergy(solarPanelEntity.getComponent().getSuppliedEnergy());
            energyNetworkManager.getComponentFromLoadedNetworks(solarPanelEntity).ifPresent(EnergyNetwork::requestEnergyNetworkUpdate);
        } else if (!isDayTime && isDay) {
            isDay = false;

            solarPanelEntity.setSuppliedEnergy(0L);
            energyNetworkManager.getComponentFromLoadedNetworks(solarPanelEntity).ifPresent(EnergyNetwork::requestEnergyNetworkUpdate);
        }
    }
}
