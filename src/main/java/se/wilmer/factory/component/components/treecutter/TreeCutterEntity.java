package se.wilmer.factory.component.components.treecutter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Interaction;
import org.bukkit.scheduler.BukkitTask;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentData;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.energy.EnergyConsumer;

import java.util.Optional;
import java.util.UUID;

public class TreeCutterEntity extends ComponentEntity<TreeCutter> implements EnergyConsumer {
    private BukkitTask task = null;
    private long currentEnergyLimit = 0L;
    private Block targetBlock = null;

    public TreeCutterEntity(Factory plugin, TreeCutter component, ComponentData data, Block block) {
        super(plugin, component, data, block);
    }

    @Override
    public void spawn() {
        updateTargetBlock();

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, new TreeCutterTask(this), 0L, 0L);
    }

    @Override
    public void despawn() {
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void onBlockChange() {
        updateTargetBlock();
    }

    @Override
    public long getMaxEnergyConsumption() {
        return 100L;
    }

    @Override
    public void setCurrentEnergyLimit(long currentEnergyLimit) {
        this.currentEnergyLimit = currentEnergyLimit;
    }

    /**
     * Gets the target block that should be broken.
     *
     * @return The target block if {@link #spawn()} has been run, otherwise empty.
     */
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
