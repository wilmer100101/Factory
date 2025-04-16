package se.wilmer.factory.component.components.milker;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitTask;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentData;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfo;
import se.wilmer.factory.energy.EnergyConsumer;

import java.util.Optional;
import java.util.UUID;

public class MilkerEntity extends ComponentEntity<Milker> implements EnergyConsumer {
    private final MilkerData data;
    private final MilkerTask milkerTask;
    private Block targetBlock = null;
    private BukkitTask task = null;
    private ComponentInfo componentInfo;
    private long currentMilkingDuration = 0L;
    private long currentEnergyLimit = 0L;

    public MilkerEntity(Factory plugin, Milker component, MilkerData data, Block block) {
        super(plugin, component, block);

        this.data = data;

        milkerTask = new MilkerTask(plugin, this);
    }

    @Override
    public void spawn() {
        componentInfo = new ComponentInfo(component.getComponentInfoSerializer(), this);
        componentInfo.spawn(block.getLocation());

        updateTargetBlock();
        updateMilkScheduler();
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
    public ComponentData getData() {
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
        updateMilkScheduler();
    }

    @Override
    public long getCurrentEnergyLimit() {
        return currentEnergyLimit;
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

    private void updateMilkScheduler() {
        long newMilkingDuration = calculateCurrentMilkingDuration(component.getMilkingDuration());
        if (newMilkingDuration == -1 || newMilkingDuration == currentMilkingDuration) {
            return;
        }

        long ticksLeft = 0;
        if (currentMilkingDuration != -1) {
            ticksLeft = currentMilkingDuration - (plugin.getServer().getCurrentTick() - milkerTask.getLastTimeRun());
        }
        currentMilkingDuration = newMilkingDuration;

        if (task != null) {
            task.cancel();
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, milkerTask, ticksLeft, newMilkingDuration);
    }

    public long calculateCurrentMilkingDuration(long number) {
        if (currentEnergyLimit == 0L) {
            return -1;
        }

        float energyPercentageLimit = (float) currentEnergyLimit / getMaxEnergyConsumption();

        float percentageIncrease = (1 - energyPercentageLimit) * 100;
        return (long) (number + (number * (percentageIncrease / 100)));
    }
}
