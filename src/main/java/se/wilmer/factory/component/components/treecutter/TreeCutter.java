package se.wilmer.factory.component.components.treecutter;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.components.blockplacer.BlockPlacer;
import se.wilmer.factory.component.components.blockplacer.BlockPlacerData;
import se.wilmer.factory.component.components.blockplacer.BlockPlacerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TreeCutter extends Component {
    private final Map<Material, Long> materialBreakDurationTicks = new HashMap<>();

    public TreeCutter(Factory plugin) {
        super(plugin);

        materialBreakDurationTicks.put(Material.DIRT, 10L);
        materialBreakDurationTicks.put(Material.STONE, 1L);
        materialBreakDurationTicks.put(Material.ORANGE_WOOL, 0L);
        materialBreakDurationTicks.put(Material.GREEN_WOOL, 100L);
    }

    @Override
    public String getId() {
        return "treecutter";
    }

    @Override
    public ComponentEntity<TreeCutter> createEntity(Block block) {
        return new TreeCutterEntity(plugin, this, new TreeCutterData(plugin, block), block);
    }

    /**
     * Retrieves the breaking duration in ticks for a given material.
     *
     * @param material The material to get the duration for.
     * @return Duration in ticks, or empty if the material was not found.
     */
    public Optional<Long> getMaterialBreakingDuration(Material material) {
        return Optional.ofNullable(materialBreakDurationTicks.get(material));
    }

}
