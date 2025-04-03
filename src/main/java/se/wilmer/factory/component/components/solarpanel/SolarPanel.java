package se.wilmer.factory.component.components.solarpanel;

import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;

public class SolarPanel extends Component {

    public SolarPanel(Factory plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "solarpanel";
    }

    @Override
    public ComponentEntity<SolarPanel> createEntity(Block block) {
        return new SolarPanelEntity(plugin, this, new SolarPanelData(plugin, block), block);
    }
}
