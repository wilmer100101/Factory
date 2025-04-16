package se.wilmer.factory.component.components.solarpanel;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentData;

import java.util.Optional;
import java.util.UUID;

public class SolarPanelData extends ComponentData {

    public SolarPanelData(Factory plugin, Block block) {
        super(plugin, block);
    }
}
