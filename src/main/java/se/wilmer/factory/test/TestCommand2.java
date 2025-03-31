package se.wilmer.factory.test;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.wire.Wire;
import se.wilmer.factory.component.wire.WireDataType;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("ALL")
@NullMarked
public class TestCommand2 implements BasicCommand {
    private final Factory plugin;

    public TestCommand2(Factory plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            return;
        }
        Block block = player.getTargetBlockExact(100);
        if (block == null) {
            player.sendMessage("Block is null");
            return;
        }

        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        String string = customBlockData.get(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING);
        if (string != null) {
            player.sendMessage("String is: " + string);
        } else {
            player.sendMessage("String is null");
        }

        UUID uuid = customBlockData.get(plugin.getComponentManager().getUUIDKey(), DataType.UUID);
        if (uuid != null) {
            player.sendMessage("UUID is: " + uuid);
        } else {
            player.sendMessage("UUID is null");
        }

        Map<UUID, Wire> s = customBlockData.get(plugin.getComponentManager().getConnectionsKey(), DataType.asMap(DataType.UUID, new WireDataType()));
        if (s == null) {
            player.sendMessage("Connections is null");
        } else {
            player.sendMessage("Connections is: ");
            for (Map.Entry<UUID, Wire> uuidWireEntry : s.entrySet()) {
                player.sendMessage("UUID: " + uuidWireEntry.getKey() + " V: " + uuidWireEntry.getValue());
            }
        }
    }
}
