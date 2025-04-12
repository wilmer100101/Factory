package se.wilmer.factory.tools;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import se.wilmer.factory.Factory;

public abstract class Tool {
    protected final Factory plugin;
    protected final String id;

    public Tool(Factory plugin, String id) {
        this.plugin = plugin;
        this.id = id;
    }

    public abstract void load();

    public boolean isToolSelected(Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        PersistentDataContainerView container = itemStack.getPersistentDataContainer();
        String typeKey = container.get(plugin.getToolManager().getTypeKey(), PersistentDataType.STRING);
        return id.equals(typeKey);
    }
}
