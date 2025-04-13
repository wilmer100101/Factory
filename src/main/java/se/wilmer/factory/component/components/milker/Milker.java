package se.wilmer.factory.component.components.milker;

import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;

public class Milker extends Component {
    private static final long MILKING_DURATION = 10L;

    public Milker(Factory plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "milker";
    }

    @Override
    public ComponentEntity<Milker> createEntity(Block block) {
        return new MilkerEntity(plugin, this, new MilkerData(plugin, block), block);
    }

    public long getMilkingDuration() {
        return MILKING_DURATION;
    }
}
