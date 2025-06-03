package se.wilmer.factory.component.components.blockplacer;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfoSerializer;
import se.wilmer.factory.component.components.blockbreaker.BlockBreaker;
import se.wilmer.factory.component.components.blockbreaker.BlockBreakerData;
import se.wilmer.factory.component.components.blockbreaker.BlockBreakerEntity;

import java.util.List;

public class BlockPlacer extends Component {
    private final long maxEnergyConsumption;
    private final List<Material> allowedMaterials;

    public BlockPlacer(Factory plugin, String id, ComponentInfoSerializer componentInfoSerializer, long maxEnergyConsumption, List<Material> allowedMaterials) {
        super(plugin, id, componentInfoSerializer);

        this.maxEnergyConsumption = maxEnergyConsumption;
        this.allowedMaterials = allowedMaterials;
    }

    @Override
    public ComponentEntity<BlockPlacer> createEntity(Block block) {
        return new BlockPlacerEntity(plugin, this, new BlockPlacerData(plugin, block), block);
    }

    public long getMaxEnergyConsumption() {
        return maxEnergyConsumption;
    }

    public List<Material> getAllowedMaterials() {
        return allowedMaterials;
    }
}
