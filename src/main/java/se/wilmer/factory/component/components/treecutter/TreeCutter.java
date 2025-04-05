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

    public TreeCutter(Factory plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "treecutter";
    }

    @Override
    public ComponentEntity<TreeCutter> createEntity(Block block) {
        return new TreeCutterEntity(plugin, this, new TreeCutterData(plugin, block), block);
    }
}
