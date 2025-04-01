package se.wilmer.factory;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.plugin.java.JavaPlugin;
import se.wilmer.factory.component.ComponentManager;
import se.wilmer.factory.energy.EnergyNetworkManager;

public final class Factory extends JavaPlugin {
    private final EnergyNetworkManager energyNetworkManager;
    private final ComponentManager componentManager;

    public Factory() {
        energyNetworkManager = new EnergyNetworkManager(this);
        componentManager = new ComponentManager(this);
    }

    @Override
    public void onEnable() {
        CustomBlockData.registerListener(this);

        componentManager.load();
    }

    @Override
    public void onDisable() {
        componentManager.unload();
    }

    public EnergyNetworkManager getEnergyNetworkManager() {
        return energyNetworkManager;
    }

    public ComponentManager getComponentManager() {
        return componentManager;
    }
}