package se.wilmer.factory.component.components.blockplacer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Interaction;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentData;
import se.wilmer.factory.component.ComponentEntity;

import java.util.Optional;

/**
 * @implNote Using a task that runs every tick is more performance friendly in this cause, because if we would use event, they would call more then one time every tick.
 */
public class BlockPlacerEntity extends ComponentEntity<BlockPlacer> {
    private Block targetBlock = null;

    public BlockPlacerEntity(Factory plugin, BlockPlacer component, ComponentData data, Block block) {
        super(plugin, component, data, block);
    }

    @Override
    public void spawn() {
        updateTargetBlock();

        plugin.getServer().getScheduler().runTaskTimer(plugin, new BlockPlacerTask(this), 0L, 0L);
    }

    @Override
    public void despawn() {

    }

    @Override
    public void onBlockChange() {
        updateTargetBlock();
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
