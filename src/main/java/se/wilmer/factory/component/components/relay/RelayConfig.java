package se.wilmer.factory.component.components.relay;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentConfig;
import se.wilmer.factory.component.ComponentInfoSerializer;

import java.util.*;

public class RelayConfig extends ComponentConfig<Relay> {

    public RelayConfig(Factory plugin, String id) {
        super(plugin, id);
    }

    @Override
    public List<Relay> load() {
        Optional<ConfigurationNode> optionalNode = createNode();
        if (optionalNode.isEmpty()) {
            return List.of();
        }
        ConfigurationNode node = optionalNode.get();

        List<Relay> relays = new ArrayList<>();
        for (ConfigurationNode childNode : node.childrenMap().values()) {
            getRelay(childNode).ifPresent(relays::add);
        }
        return relays;
    }

    private Optional<Relay> getRelay(ConfigurationNode node) {
        String id = String.valueOf(node.key());
        ComponentInfoSerializer infoSerializer = new ComponentInfoSerializer.Builder(node.node("info"))
                .energy(false)
                .deserialize();

        if (id == null) {
            plugin.getComponentLogger().warn("Did not found id for, {}", this.id);
            return Optional.empty();
        }

        return Optional.of(new Relay(plugin, id, infoSerializer));
    }
}
