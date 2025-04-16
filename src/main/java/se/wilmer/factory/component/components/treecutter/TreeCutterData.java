package se.wilmer.factory.component.components.treecutter;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentData;

import java.util.Optional;
import java.util.UUID;

public class TreeCutterData extends ComponentData {

    public TreeCutterData(Factory plugin, Block block) {
        super(plugin, block);
    }
}
