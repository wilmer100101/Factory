package se.wilmer.factory.component.components.milker;

import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfoSerializer;

public class Milker extends Component {
    private final ComponentInfoSerializer componentInfoSerializer;
    private final long maxEnergyConsumption;
    private final long milkingDuration;

    public Milker(Factory plugin, String id, ComponentInfoSerializer componentInfoSerializer, long maxEnergyConsumption, long milkingDuration) {
        super(plugin, id);

        this.componentInfoSerializer = componentInfoSerializer;
        this.maxEnergyConsumption = maxEnergyConsumption;
        this.milkingDuration = milkingDuration;
    }

    @Override
    public ComponentEntity<Milker> createEntity(Block block) {
        return new MilkerEntity(plugin, this, new MilkerData(plugin, block), block);
    }

    public long getMilkingDuration() {
        return milkingDuration;
    }

    public long getMaxEnergyConsumption() {
        return maxEnergyConsumption;
    }

    public ComponentInfoSerializer getComponentInfoSerializer() {
        return componentInfoSerializer;
    }
}
