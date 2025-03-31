package se.wilmer.factory.component.components.blockplacer;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.components.blockbreaker.BlockBreaker;
import se.wilmer.factory.component.components.blockbreaker.BlockBreakerData;
import se.wilmer.factory.component.components.blockbreaker.BlockBreakerEntity;

public class BlockPlacer extends Component {

    public BlockPlacer(Factory plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "blockplacer";
    }

    @Override
    public ComponentEntity<BlockPlacer> createEntity(Block block) {
        return new BlockPlacerEntity(plugin, this, new BlockPlacerData(plugin, block), block);
    }
}
