package se.wilmer.factory.component.components.milker;

import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentData;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.energy.EnergyConsumer;

public class MilkerEntity extends ComponentEntity<Milker> implements EnergyConsumer {
    private final MilkerTask milkerTask;
    private BukkitTask task = null;
    private long currentMilkingDuration = 0L;
    private long currentEnergyLimit = 0L;

    public MilkerEntity(Factory plugin, Milker component, ComponentData data, Block block) {
        super(plugin, component, data, block);

        milkerTask = new MilkerTask(plugin, this);
    }

    @Override
    public void spawn() {
        updateMilkScheduler();
    }

    @Override
    public void despawn() {
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void onBlockChange() {
    }

    @Override
    public long getMaxEnergyConsumption() {
        return 10L;
    }

    @Override
    public void setCurrentEnergyLimit(long currentEnergyLimit) {
        this.currentEnergyLimit = currentEnergyLimit;

        updateMilkScheduler();
    }

    @Override
    public long getCurrentEnergyLimit() {
        return currentEnergyLimit;
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
