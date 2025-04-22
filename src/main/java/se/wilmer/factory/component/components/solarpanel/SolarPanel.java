package se.wilmer.factory.component.components.solarpanel;

import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfoSerializer;

public class SolarPanel extends Component {
    private final long suppliedEnergy;

    public SolarPanel(Factory plugin, String id, ComponentInfoSerializer componentInfoSerializer, long suppliedEnergy) {
        super(plugin, id, componentInfoSerializer);

        this.suppliedEnergy = suppliedEnergy;
    }

    @Override
    public ComponentEntity<SolarPanel> createEntity(Block block) {
        return new SolarPanelEntity(plugin, this, new SolarPanelData(plugin, block), block);
    }

    public long getSuppliedEnergy() {
        return suppliedEnergy;
    }
}
