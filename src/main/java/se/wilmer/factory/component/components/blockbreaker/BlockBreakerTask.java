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
        Block block = blockBreakerEntity.getBlock();
        if (block.isBlockPowered()) {
            return;
        }

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
        final long totalBreakingDuration = optionalTotalBreakingDuration.get();

        long currentBreakingDuration = blockBreakerEntity.getCurrentBreakingDuration();
        currentBreakingDuration += 1;
        blockBreakerEntity.setCurrentBreakingDuration(currentBreakingDuration);

        if (currentBreakingDuration > totalBreakingDuration) {
            targetBlock.breakNaturally();
            blockBreakerEntity.setCurrentBreakingDuration(0);
            return -1;
        }

        return (float) currentBreakingDuration / totalBreakingDuration;
    }
}
