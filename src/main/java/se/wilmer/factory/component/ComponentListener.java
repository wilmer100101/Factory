package se.wilmer.factory.component;

import com.jeff_media.customblockdata.events.CustomBlockDataRemoveEvent;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import se.wilmer.factory.component.wire.WireManager;

import java.util.UUID;


public class ComponentListener implements Listener {
    private final ComponentManager componentManager;

    public ComponentListener(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    @EventHandler
    public void onCustomBlockDataRemove(CustomBlockDataRemoveEvent event) {
        componentManager.getItemConverter().transferDataFromBlock(event);

        UUID uuid = event.getCustomBlockData().get(componentManager.getUUIDKey(), DataType.UUID);
        if (uuid != null) {
            componentManager.getComponentEntity(uuid).ifPresent(componentEntity -> componentManager.getWireManager().getWireDisconnector().disconnectAllComponents(componentEntity));
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        componentManager.getItemConverter().transferDataFromItemStack(event);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        componentManager.getLoader().loadComponentsInChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        componentManager.getLoader().unloadComponentsInChunk(event.getChunk());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        if (itemStack == null) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        componentManager.getWireManager().getWireSelector().selectComponent(event.getPlayer(), block, itemStack);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        WireManager wireManager = componentManager.getWireManager();
        wireManager.getWireSelector().removePlayerSelection(uuid);
        wireManager.getWireDisplay().removeWire(player);
    }
}
