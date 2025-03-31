package se.wilmer.factory;

import com.jeff_media.customblockdata.CustomBlockData;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import se.wilmer.factory.component.ComponentManager;
import se.wilmer.factory.component.components.blockbreaker.BlockBreaker;
import se.wilmer.factory.component.components.blockbreaker.BlockBreakerEntity;
import se.wilmer.factory.component.components.blockplacer.BlockPlacer;
import se.wilmer.factory.component.components.blockplacer.BlockPlacerEntity;
import se.wilmer.factory.component.components.treecutter.TreeCutter;
import se.wilmer.factory.component.components.treecutter.TreeCutterEntity;
import se.wilmer.factory.energy.EnergyNetwork;
import se.wilmer.factory.energy.EnergyNetworkManager;
import se.wilmer.factory.test.TestCommand;
import se.wilmer.factory.test.TestCommand2;

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

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> {
                    event.registrar().register("item_get", new TestCommand(this));
                    event.registrar().register("item_lookup", new TestCommand2(this));
                }
        );
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

//TODO:
// - Ett block placeras
// -
