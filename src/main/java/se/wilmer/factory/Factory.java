package se.wilmer.factory;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.plugin.java.JavaPlugin;
import se.wilmer.factory.command.CommandManager;
import se.wilmer.factory.component.ComponentManager;
import se.wilmer.factory.energy.EnergyNetworkManager;
import se.wilmer.factory.item.ItemManager;
import se.wilmer.factory.tools.ToolManager;

public final class Factory extends JavaPlugin {
    private final EnergyNetworkManager energyNetworkManager;
    private final ComponentManager componentManager;
    private final ToolManager toolManager;
    private final ItemManager itemManager;
    private final CommandManager commandManager;

    public Factory() {
        energyNetworkManager = new EnergyNetworkManager(this);
        componentManager = new ComponentManager(this);
        toolManager = new ToolManager(this);
        itemManager = new ItemManager(this);
        commandManager = new CommandManager(this);
    }

    @Override
    public void onEnable() {
        CustomBlockData.registerListener(this);

        componentManager.load();
        toolManager.load();
        itemManager.register();
        commandManager.register();
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

    public ToolManager getToolManager() {
        return toolManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }
}