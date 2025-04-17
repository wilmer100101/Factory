package se.wilmer.factory.component.components.treecutter;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfo;
import se.wilmer.factory.component.ComponentInfoSerializer;
import se.wilmer.factory.component.components.blockplacer.BlockPlacer;
import se.wilmer.factory.component.components.blockplacer.BlockPlacerData;
import se.wilmer.factory.component.components.blockplacer.BlockPlacerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TreeCutter extends Component {
    private final ComponentInfoSerializer componentInfoSerializer;
    private final long maxEnergyConsumption;
    private final long cuttingDuration;

    public TreeCutter(Factory plugin, String id, ComponentInfoSerializer componentInfoSerializer, long maxEnergyConsumption, long cuttingDuration) {
        super(plugin, id);

        this.maxEnergyConsumption = maxEnergyConsumption;
        this.cuttingDuration = cuttingDuration;
        this.componentInfoSerializer = componentInfoSerializer;
    }

    @Override
    public ComponentEntity<TreeCutter> createEntity(Block block) {
        return new TreeCutterEntity(plugin, this, new TreeCutterData(plugin, block), block);
    }

    public long getMaxEnergyConsumption() {
        return maxEnergyConsumption;
    }

    public long getCuttingDuration() {
        return cuttingDuration;
    }

    public ComponentInfoSerializer getComponentInfoSerializer() {
        return componentInfoSerializer;
    }
}
