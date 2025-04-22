package se.wilmer.factory.component.components.blockplacer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.scheduler.BukkitTask;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentData;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfo;
import se.wilmer.factory.energy.EnergyConsumer;

import java.util.Optional;
import java.util.UUID;

/**
 * @implNote Using a task that runs every tick is more performance friendly in this cause, because if we would use event, they would call more then one time every tick.
 */
public class BlockPlacerEntity extends ComponentEntity<BlockPlacer> implements EnergyConsumer {
    private final BlockPlacerData data;
    private BukkitTask task = null;
    private Block targetBlock = null;
    private long currentEnergyLimit = 0L;

    public BlockPlacerEntity(Factory plugin, BlockPlacer component, BlockPlacerData data, Block block) {
        super(plugin, component, block);

        this.data = data;
    }

    @Override
    public void spawn() {
        super.spawn();

        updateTargetBlock();

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, new BlockPlacerTask(this), 0L, 0L);
    }

    @Override
    public void despawn() {
        super.despawn();

        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void onBlockChange() {
        super.onBlockChange();

        updateTargetBlock();
    }

    @Override
    public ComponentData getData() {
        return data;
    }

    @Override
    public long getMaxEnergyConsumption() {
        return component.getMaxEnergyConsumption();
    }

    @Override
    public long getCurrentEnergyLimit() {
        return currentEnergyLimit;
    }

    @Override
    public void setCurrentEnergyLimit(long currentEnergyLimit) {
        this.currentEnergyLimit = currentEnergyLimit;

        componentInfo.updateEnergy(currentEnergyLimit, getMaxEnergyConsumption());
    }

    public Optional<Block> getTargetBlock() {
        return Optional.ofNullable(targetBlock);
    }

    private void updateTargetBlock() {
        final World world = block.getWorld();
        final BlockData blockData = block.getBlockData();
        final Location clonedLocation = block.getLocation().clone();
        if (blockData instanceof Directional directional) {
            targetBlock = world.getBlockAt(clonedLocation.add(directional.getFacing().getDirection()));
            return;
        }

        targetBlock = world.getBlockAt(clonedLocation.add(0, 1, 0));
    }
}
