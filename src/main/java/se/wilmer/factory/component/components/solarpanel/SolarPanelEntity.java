package se.wilmer.factory.component.components.solarpanel;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentData;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.energy.EnergySupplier;

import java.util.Optional;

public class SolarPanelEntity extends ComponentEntity<SolarPanel> implements EnergySupplier {

    public SolarPanelEntity(Factory plugin, SolarPanel component, ComponentData data, Block block) {
        super(plugin, component, data, block);
    }

    @Override
    public void spawn() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, new SolarPanelTask(this), 0L, 0L);
    }

    @Override
    public void despawn() {
    }

    @Override
    public void onBlockChange() {
    }

    @Override
    public long getSuppliedEnergy() {
        return 0;
    }
}
