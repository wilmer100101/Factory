package se.wilmer.factory.component;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.components.treecutter.TreeCutterData;
import se.wilmer.factory.energy.EnergyComponent;
import se.wilmer.factory.energy.EnergyNetwork;

import java.util.UUID;

public abstract class ComponentEntity<T extends Component> implements EnergyComponent {
    protected final Factory plugin;
    protected final T component;
    protected final Block block;

    public ComponentEntity(Factory plugin, T component, Block block) {
        this.plugin = plugin;
        this.component = component;
        this.block = block;
    }

    public final void load() {
        plugin.getComponentManager().getComponentEntities().add(this);
        spawn();
    }

    public final void unload() {
        plugin.getComponentManager().getComponentEntities().remove(this);
        despawn();
    }

    protected abstract void spawn();

    protected abstract void despawn();

    public abstract void onBlockChange();

    //TODO: (TEMP) Make this return a configurable value
    public Location getOffsetLocation() {
        return block.getLocation().clone().add(0.5, block.getBoundingBox().getHeight(), 0.5);
    }

    public abstract ComponentData getData();

    public final Block getBlock() {
        return block;
    }

    public final UUID getUUID() {
        return getData().getUUID();
    }

    public final T getComponent() {
        return component;
    }
}
