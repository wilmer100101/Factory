package se.wilmer.factory.component;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
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
    protected final ComponentInfo componentInfo;

    public ComponentEntity(Factory plugin, T component, Block block) {
        this.plugin = plugin;
        this.component = component;
        this.block = block;

        componentInfo = new ComponentInfo(component.getComponentInfoSerializer(), this);
    }

    public void spawn() {
        plugin.getComponentManager().getComponentEntities().add(this);

        componentInfo.spawn(block.getLocation());
    }

    public void despawn() {
        plugin.getComponentManager().getComponentEntities().remove(this);

        componentInfo.despawn(block.getWorld());
    }

    public void onBlockChange() {
        componentInfo.updateLocation();
    }

    public Location getOffsetLocation() {
        return block.getLocation().clone().add(0.5, block.getBoundingBox().getHeight(), 0.5);
    }

    public abstract ComponentData getData();

    public final Block getBlock() {
        return block;
    }

    public final T getComponent() {
        return component;
    }

    @Override
    public final UUID getUUID() {
        return getData().getUUID();
    }
}
