package se.wilmer.factory.component;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.wire.Wire;
import se.wilmer.factory.component.wire.WireDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class ComponentData {
    protected final Factory plugin;
    protected final Block block;

    public ComponentData(Factory plugin, Block block) {
        this.plugin = plugin;
        this.block = block;
    }

    public final UUID getUUID() {
        return new CustomBlockData(block, plugin).get(plugin.getComponentManager().getUUIDKey(), DataType.UUID);
    }

    public final String getType() {
        return new CustomBlockData(block, plugin).get(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING);
    }

    public final void setInformationEntityUUID(UUID uuid) {
        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        if (uuid == null) {
            customBlockData.remove(plugin.getComponentManager().getInfoKey());
            return;
        }
        customBlockData.set(plugin.getComponentManager().getInfoKey(), DataType.UUID, uuid);
    }

    public final Optional<UUID> getInformationEntityUUID() {
        return Optional.ofNullable(new CustomBlockData(block, plugin).get(plugin.getComponentManager().getInfoKey(), DataType.UUID));
    }

    public final Map<UUID, Wire> getConnections() {
        Map<UUID, Wire> connections = new CustomBlockData(block, plugin).get(plugin.getComponentManager().getConnectionsKey(), DataType.asMap(DataType.UUID, new WireDataType()));
        if (connections != null) {
            return connections;
        }
        return new HashMap<>();
    }

    public final void setConnections(Map<UUID, Wire> connections) {
        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        if (connections == null) {
            customBlockData.remove(plugin.getComponentManager().getConnectionsKey());
        }
        customBlockData.set(plugin.getComponentManager().getConnectionsKey(), DataType.asMap(DataType.UUID, new WireDataType()), connections);
    }
}
