package se.wilmer.factory.component;

import org.bukkit.block.Block;
import se.wilmer.factory.Factory;

public abstract class Component {
    protected final Factory plugin;
    protected final String id;

    public Component(Factory plugin, String id) {
        this.plugin = plugin;
        this.id = id;
    }

    public abstract ComponentEntity<?> createEntity(Block block);

    public final String getId() {
        return id;
    }
}
