package se.wilmer.factory.component.components.treecutter;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import se.wilmer.factory.Factory;

import java.util.*;


public class TreeCutterTask implements Runnable {
    private static final int MAX_ITERATION_ATTEMPTS = 500;

    private static final List<Material> TREE_MATERIALS = List.of(
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.CHERRY_LOG,
            Material.CRIMSON_HYPHAE,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.MANGROVE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.WARPED_HYPHAE,
            Material.ACACIA_LEAVES,
            Material.BIRCH_LEAVES,
            Material.CHERRY_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.MANGROVE_LEAVES,
            Material.OAK_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.AZALEA_LEAVES,
            Material.FLOWERING_AZALEA_LEAVES
    );

    private static final List<BlockFace> DIRECTIONS = List.of(
            BlockFace.UP,
            BlockFace.SOUTH,
            BlockFace.NORTH,
            BlockFace.WEST,
            BlockFace.EAST
    );

    private final Factory plugin;
    private final TreeCutterEntity treeCutterEntity;
    private final List<Block> blocks = new ArrayList<>();
    private long lastTimeRun = 0L;

    public TreeCutterTask(Factory plugin, TreeCutterEntity treeCutterEntity) {
        this.plugin = plugin;
        this.treeCutterEntity = treeCutterEntity;
    }

    @Override
    public void run() {
        lastTimeRun = plugin.getServer().getCurrentTick();

        Optional<Block> optionalTargetBlock = treeCutterEntity.getTargetBlock();
        if (optionalTargetBlock.isEmpty()) {
            return;
        }
        Block targetBlock = optionalTargetBlock.get();
        if (targetBlock.isEmpty()) {
            return;
        }

        if (blocks.isEmpty()) {
            if (!TREE_MATERIALS.contains(targetBlock.getType())) {
                return;
            }
            blocks.add(targetBlock);
            addTreeBlocks(targetBlock, 0);
            blocks.sort(Comparator.comparingDouble(Block::getY).reversed());
            return;
        }

        Block currentBlock = blocks.removeFirst();
        currentBlock.breakNaturally();

        if (!blocks.isEmpty()) {
            startBreakingTask(blocks.getFirst());
        }
    }

    private void startBreakingTask(Block targetBlock) {
        final long duration = treeCutterEntity.getCurrentCuttingDuration();
        final long initialTime = plugin.getServer().getCurrentTick();

        final long endTime = initialTime + duration;

        plugin.getServer().getScheduler().runTaskTimer(plugin, (task) -> {
            if (plugin.getServer().getCurrentTick() >= endTime) {
                task.cancel();
                return;
            }

            long currentTick = plugin.getServer().getCurrentTick();
            float progress = (float) (currentTick - initialTime) / duration;

            Location location = targetBlock.getLocation();
            treeCutterEntity.getBlock().getWorld().getPlayersSeeingChunk(targetBlock.getChunk()).forEach(player -> player.sendBlockDamage(location, progress, -location.hashCode()));
        }, 0L, 0L);
    }

    private void addTreeBlocks(Block block, int attempts) {
        for (BlockFace direction : DIRECTIONS) {
            attempts++;
            if (attempts > MAX_ITERATION_ATTEMPTS) {
                break;
            }

            Block currentBlock = block.getRelative(direction);
            if (!TREE_MATERIALS.contains(currentBlock.getType())) {
                continue;
            }
            if (blocks.contains(currentBlock)) {
                continue;
            }
            blocks.add(currentBlock);

            addTreeBlocks(currentBlock, attempts);
        }
    }

    public long getLastTimeRun() {
        return lastTimeRun;
    }
}
