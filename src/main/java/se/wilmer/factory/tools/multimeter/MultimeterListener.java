package se.wilmer.factory.tools.multimeter;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class MultimeterListener implements Listener {
    private final Multimeter multimeter;

    public MultimeterListener(Multimeter multimeter) {
        this.multimeter = multimeter;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!multimeter.isToolSelected(player)) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block != null) {
            multimeter.getEnergyInformation(block).ifPresent(message -> event.getPlayer().sendMessage(message));
        }
    }
}
