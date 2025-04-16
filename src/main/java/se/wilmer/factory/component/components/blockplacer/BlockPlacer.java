package se.wilmer.factory.component.components.blockplacer;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfoSerializer;
import se.wilmer.factory.component.components.blockbreaker.BlockBreaker;
import se.wilmer.factory.component.components.blockbreaker.BlockBreakerData;
import se.wilmer.factory.component.components.blockbreaker.BlockBreakerEntity;

public class BlockPlacer extends Component {
    private final ComponentInfoSerializer componentInfoSerializer;
    private final long maxEnergyConsumption;

    public BlockPlacer(Factory plugin, String id, ComponentInfoSerializer componentInfoSerializer, long maxEnergyConsumption) {
        super(plugin, id);

        this.componentInfoSerializer = componentInfoSerializer;
        this.maxEnergyConsumption = maxEnergyConsumption;
    }

    @Override
    public ComponentEntity<BlockPlacer> createEntity(Block block) {
        return new BlockPlacerEntity(plugin, this, new BlockPlacerData(plugin, block), block);
    }

    public ComponentInfoSerializer getComponentInfoSerializer() {
        return componentInfoSerializer;
    }

    public long getMaxEnergyConsumption() {
        return maxEnergyConsumption;
    }
}
