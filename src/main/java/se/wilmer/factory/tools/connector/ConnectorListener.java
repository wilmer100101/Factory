package se.wilmer.factory.tools.connector;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import se.wilmer.factory.Factory;
import se.wilmer.factory.tools.multimeter.Multimeter;

public class ConnectorListener implements Listener {
    private final Factory plugin;
    private final Connector connector;

    public ConnectorListener(Factory plugin, Connector connector) {
        this.plugin = plugin;
        this.connector = connector;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!connector.isToolSelected(player)) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block != null) {
            plugin.getComponentManager().getWireManager().getWireSelector().selectComponent(player, block);
            event.setCancelled(true);
        }
    }
}
