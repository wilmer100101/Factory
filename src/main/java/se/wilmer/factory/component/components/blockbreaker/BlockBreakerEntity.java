package se.wilmer.factory.component.components.blockbreaker;

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

import java.util.Optional;
/**
 * @implNote Using a task that runs every tick is more performance friendly in this cause, because if we would use event, they would call more then one time every tick.
 */
public class BlockBreakerEntity extends ComponentEntity<BlockBreaker> {
    private BukkitTask task = null;
    private Block targetBlock = null;
    private long currentBreakingDuration = 0;

    public BlockBreakerEntity(Factory plugin, BlockBreaker component, ComponentData data, Block block) {
        super(plugin, component, data, block);
    }

    @Override
    public void spawn() {
        updateTargetBlock();

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, new BlockBreakerTask(this), 0L, 0L);
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

    /**
     * Gets the target block that should be broken.
     *
     * @return The target block if {@link #spawn()} has been run, otherwise empty.
     */
    public Optional<Block> getTargetBlock() {
        return Optional.ofNullable(targetBlock);
    }

    public long getCurrentBreakingDuration() {
        return currentBreakingDuration;
    }

    public void setCurrentBreakingDuration(long currentBreakingDuration) {
        this.currentBreakingDuration = currentBreakingDuration;
    }

    /**
     * Updates the target block breaking processes.
     *
     * @return The progress that have been done, 0 is "no progress" and 1 is "done" and -1 if the block is currently not processed.
     */
    public float tickBreakingProcesses() {
        if (block.isBlockPowered()) {
            return -1;
        }
        if (targetBlock == null || targetBlock.isEmpty()) {
            return -1;
        }
        Optional<Long> optionalTotalBreakingDuration = component.getMaterialBreakingDuration(targetBlock.getType());
        if (optionalTotalBreakingDuration.isEmpty()) {
            return -1;
        }
        currentBreakingDuration += 1;

        final long totalBreakingDuration = optionalTotalBreakingDuration.get();
        if (currentBreakingDuration > totalBreakingDuration) {
            targetBlock.breakNaturally();
            currentBreakingDuration = 0;
            return -1;
        }

        return (float) currentBreakingDuration / totalBreakingDuration;
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
