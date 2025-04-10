package se.wilmer.factory.component.components.blockbreaker;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Optional;


public class BlockBreakerTask implements Runnable {
    private final BlockBreakerEntity blockBreakerEntity;

    public BlockBreakerTask(BlockBreakerEntity blockBreakerEntity) {
        this.blockBreakerEntity = blockBreakerEntity;
    }

    @Override
    public void run() {
        Optional<Block> optionalTargetBlock = blockBreakerEntity.getTargetBlock();
        if (optionalTargetBlock.isEmpty()) {
            return;
        }
        Block targetBlock = optionalTargetBlock.get();

        float progress = getBlockProgress(targetBlock);
        if (progress == -1) {
            return;
        }

        Location location = targetBlock.getLocation();
        blockBreakerEntity.getBlock().getWorld().getPlayersSeeingChunk(targetBlock.getChunk()).forEach(player -> player.sendBlockDamage(location, progress));
    }

    public float getBlockProgress(Block targetBlock) {
        Optional<Long> optionalTotalBreakingDuration = blockBreakerEntity.getComponent().getMaterialBreakingDuration(targetBlock.getType());
        if (optionalTotalBreakingDuration.isEmpty()) {
            return -1;
        }
        final float totalBreakingDuration = calculateTotalBreakingDuration(optionalTotalBreakingDuration.get());
        final float newBreakingProgress = 1 / totalBreakingDuration;

        float currentBreakingProgress = blockBreakerEntity.getCurrentBreakingProgress();
        currentBreakingProgress += newBreakingProgress;
        blockBreakerEntity.setCurrentBreakingProgress(currentBreakingProgress);

        if (currentBreakingProgress > totalBreakingDuration) {
            targetBlock.breakNaturally();
            blockBreakerEntity.setCurrentBreakingProgress(0);
            return -1;
        }

        return currentBreakingProgress;
    }

    public float calculateTotalBreakingDuration(long number) {
        float energyPercentageLimit = (float) blockBreakerEntity.getCurrentEnergyLimit() / blockBreakerEntity.getMaxEnergyConsumption();

        float percentageIncrease = (1 - energyPercentageLimit) * 100;
        return number + (number * (percentageIncrease / 100));
    }
}
