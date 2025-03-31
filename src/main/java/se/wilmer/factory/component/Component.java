package se.wilmer.factory.component;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Interaction;
import se.wilmer.factory.Factory;

public abstract class Component {
    protected final Factory plugin;

    public Component(Factory plugin) {
        this.plugin = plugin;
    }

    public abstract String getId();

    public abstract ComponentEntity<?> createEntity(Block block);
}
