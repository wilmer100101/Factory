package se.wilmer.factory.component;

import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.energy.EnergyComponent;

import java.util.UUID;

public abstract class ComponentEntity<T extends Component> implements EnergyComponent {
    protected final Factory plugin;
    protected final T component;
    protected final Block block;
    protected final ComponentData data;

    public ComponentEntity(Factory plugin, T component, ComponentData data, Block block) {
        this.plugin = plugin;
        this.component = component;
        this.block = block;
        this.data = data;
    }

    public void load() {
        plugin.getComponentManager().getComponentEntities().add(this);
        spawn();
    }

    public void unload() {
        plugin.getComponentManager().getComponentEntities().remove(this);
        despawn();
    }

    protected abstract void spawn();

    protected abstract void despawn();

    public abstract void onBlockChange();

    public Block getBlock() {
        return block;
    }

    public UUID getUUID() {
        return data.getUUID();
    }

    public ComponentData getData() {
        return data;
    }

    public T getComponent() {
        return component;
    }
}
