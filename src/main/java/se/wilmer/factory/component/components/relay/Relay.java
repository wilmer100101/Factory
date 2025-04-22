package se.wilmer.factory.component.components.relay;

import org.bukkit.block.Block;
import org.bukkit.inventory.FurnaceRecipe;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfoSerializer;
import se.wilmer.factory.component.components.fuelburner.FuelBurnerListener;

import java.util.Map;

public class Relay extends Component {

    public Relay(Factory plugin, String id, ComponentInfoSerializer componentInfoSerializer) {
        super(plugin, id, componentInfoSerializer);
    }

    @Override
    public ComponentEntity<Relay> createEntity(Block block) {
        return new RelayEntity(plugin, this, new RelayData(plugin, block), block);
    }
}
