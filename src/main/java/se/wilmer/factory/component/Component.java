package se.wilmer.factory.component;

import org.bukkit.block.Block;
import se.wilmer.factory.Factory;

public abstract class Component {
    protected final Factory plugin;
    protected final String id;
    protected final ComponentInfoSerializer componentInfoSerializer;

    public Component(Factory plugin, String id, ComponentInfoSerializer componentInfoSerializer) {
        this.plugin = plugin;
        this.id = id;
        this.componentInfoSerializer = componentInfoSerializer;
    }

    public abstract ComponentEntity<?> createEntity(Block block);

    public final String getId() {
        return id;
    }

    public final ComponentInfoSerializer getComponentInfoSerializer() {
        return componentInfoSerializer;
    }
}
