package se.wilmer.factory.test;

import com.jeff_media.morepersistentdatatypes.DataType;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import se.wilmer.factory.Factory;

import java.util.UUID;

@SuppressWarnings("ALL")
@NullMarked
public class TestCommand implements BasicCommand {
    private final Factory plugin;

    public TestCommand(Factory plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            return;
        }

        ItemStack item = new ItemStack(Material.DIRT);
        item.editPersistentDataContainer(pdc -> {
            pdc.set(plugin.getComponentManager().getTypeKey(), PersistentDataType.STRING, "blockplacer");
            pdc.set(plugin.getComponentManager().getUUIDKey(), DataType.UUID, UUID.randomUUID());
        });

        player.getInventory().addItem(item);
    }
}
