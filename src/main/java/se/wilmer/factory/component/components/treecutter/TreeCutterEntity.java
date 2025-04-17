package se.wilmer.factory.component.components.treecutter;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfo;
import se.wilmer.factory.energy.EnergyConsumer;

import java.util.Optional;
import java.util.UUID;

public class TreeCutterEntity extends ComponentEntity<TreeCutter> implements EnergyConsumer {
    private final TreeCutterData data;
    private final TreeCutterTask treeCutterTask;
    private ComponentInfo componentInfo;
    private long currentCuttingDuration;
    private BukkitTask task = null;
    private long currentEnergyLimit = 0L;
    private Block targetBlock = null;

    public TreeCutterEntity(Factory plugin, TreeCutter component, TreeCutterData data, Block block) {
        super(plugin, component, block);

        this.data = data;

        treeCutterTask = new TreeCutterTask(plugin, this);
    }

    @Override
    public void spawn() {
        componentInfo = new ComponentInfo(component.getComponentInfoSerializer(), this);
        componentInfo.spawn(block.getLocation());

        updateTargetBlock();
        updateCutterScheduler();
    }

    @Override
    public void despawn() {
        if (task != null) {
            task.cancel();
        }
        if (componentInfo != null) {
            componentInfo.despawn(block.getWorld());
        }
    }

    @Override
    public void onBlockChange() {
        if (componentInfo != null) {
            componentInfo.updateLocation();
        }
        updateTargetBlock();
    }

    @Override
    public TreeCutterData getData() {
        return data;
    }

    @Override
    public long getMaxEnergyConsumption() {
        return component.getMaxEnergyConsumption();
    }

    @Override
    public void setCurrentEnergyLimit(long currentEnergyLimit) {
        this.currentEnergyLimit = currentEnergyLimit;

        if (componentInfo != null) {
            componentInfo.updateEnergy(currentEnergyLimit, getMaxEnergyConsumption());
        }
        updateCutterScheduler();
    }

    @Override
    public long getCurrentEnergyLimit() {
        return currentEnergyLimit;
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

    private void updateCutterScheduler() {
        long newCutterDuration = calculateCurrentCutterDuration(component.getCuttingDuration());
        if (newCutterDuration == -1 || newCutterDuration == currentCuttingDuration) {
            if (task != null) {
                task.cancel();
            }
            currentCuttingDuration = newCutterDuration;
            return;
        }

        long ticksLeft = 0;
        if (currentCuttingDuration != -1) {
            ticksLeft = currentCuttingDuration - (plugin.getServer().getCurrentTick() - treeCutterTask.getLastTimeRun());
        }
        currentCuttingDuration = newCutterDuration;

        if (task != null) {
            task.cancel();
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, treeCutterTask, ticksLeft, newCutterDuration);
    }

    public long calculateCurrentCutterDuration(long number) {
        if (currentEnergyLimit == 0L) {
            return -1;
        }

        float energyPercentageLimit = (float) currentEnergyLimit / getMaxEnergyConsumption();

        float percentageIncrease = (1 - energyPercentageLimit) * 100;
        return (long) (number + (number * (percentageIncrease / 100)));
    }

    public long getCurrentCuttingDuration() {
        return currentCuttingDuration;
    }
}
