package se.wilmer.factory.component;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.units.qual.C;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.wire.Wire;
import se.wilmer.factory.component.wire.WireDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ComponentData {
    protected final Factory plugin;
    protected final Block block;

    public ComponentData(Factory plugin, Block block) {
        this.plugin = plugin;
        this.block = block;
    }

    public UUID getUUID() {
        return new CustomBlockData(block, plugin).get(plugin.getComponentManager().getUUIDKey(), DataType.UUID);
    }

    public String getType() {
        return new CustomBlockData(block, plugin).get(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING);
    }

    public Map<UUID, Wire> getConnections() {
        Map<UUID, Wire> connections = new CustomBlockData(block, plugin).get(plugin.getComponentManager().getConnectionsKey(), DataType.asMap(DataType.UUID, new WireDataType()));
        if (connections != null) {
            return connections;
        }
        return new HashMap<>();
    }

    public void setConnections(Map<UUID, Wire> connections) {
        new CustomBlockData(block, plugin).set(plugin.getComponentManager().getConnectionsKey(), DataType.asMap(DataType.UUID, new WireDataType()), connections);
    }
}
