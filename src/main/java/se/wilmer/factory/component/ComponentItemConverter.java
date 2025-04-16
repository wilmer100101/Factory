package se.wilmer.factory.component;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.customblockdata.events.CustomBlockDataRemoveEvent;
import com.jeff_media.morepersistentdatatypes.DataType;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import se.wilmer.factory.Factory;

import java.util.UUID;

public class ComponentItemConverter {
    private final Factory plugin;
    private final ComponentManager componentManager;

    public ComponentItemConverter(Factory plugin, ComponentManager componentManager) {
        this.plugin = plugin;
        this.componentManager = componentManager;
    }

    public void transferDataFromBlock(CustomBlockDataRemoveEvent event) {
        CustomBlockData customBlockData = event.getCustomBlockData();
        String type = customBlockData.get(componentManager.getTypeKey(), DataType.STRING);
        UUID uuid = customBlockData.get(componentManager.getUUIDKey(), DataType.UUID);
        if (type == null || uuid == null) {
            return;
        }

        Block block = customBlockData.getBlock();
        if (block == null) {
            return;
        }

        Event originalEvent = event.getBukkitEvent();
        Class<? extends @NotNull Event> originalEventClass = event.getBukkitEvent().getClass();

        plugin.getComponentLogger().info("Event: " + originalEvent.getEventName());

        if (originalEventClass.isAssignableFrom(BlockBreakEvent.class)) {
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) originalEvent;
            blockBreakEvent.setDropItems(false);

            transferAndDrop(block, type, uuid);
        } else if (originalEventClass.isAssignableFrom(BlockPlaceEvent.class)) {
            BlockPlaceEvent blockPlaceEvent = (BlockPlaceEvent) originalEvent;
            blockPlaceEvent.setCancelled(true);

            transferAndDrop(block, type, uuid);

            block.setBlockData(blockPlaceEvent.getBlockPlaced().getBlockData());
        } else if (originalEventClass.isAssignableFrom(BlockExplodeEvent.class)) {
            BlockExplodeEvent blockExplodeEvent = (BlockExplodeEvent) originalEvent;

            transferAndDrop(block, type, uuid);

            blockExplodeEvent.blockList().remove(block);
            block.setType(Material.AIR);

            plugin.getComponentLogger().info("BlockExplodeEvent");
        } else if (originalEventClass.isAssignableFrom(EntityExplodeEvent.class)) {
            EntityExplodeEvent entityExplodeEvent = (EntityExplodeEvent) originalEvent;

            entityExplodeEvent.blockList().remove(block);
            plugin.getComponentLogger().info("EntityExplodeEvents: " + block);

            transferAndDrop(block, type, uuid);

            block.setType(Material.AIR);

            plugin.getComponentLogger().info("EntityExplodeEvent: " + block);
        }
    }

    private void transferAndDrop(Block block, String type, UUID uuid) {
        ItemStack itemStack = new ItemStack(block.getType());
        itemStack.editPersistentDataContainer(pdc -> {
            pdc.set(componentManager.getTypeKey(), DataType.STRING, type);
            pdc.set(componentManager.getUUIDKey(), DataType.UUID, uuid);
        });

        block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
    }

    public void transferDataFromItemStack(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack itemStack = event.getItemInHand();

        PersistentDataContainerView pdc = itemStack.getPersistentDataContainer();
        String type = pdc.get(componentManager.getTypeKey(), DataType.STRING);
        UUID uuid = pdc.get(componentManager.getUUIDKey(), DataType.UUID);
        if (type == null || uuid == null) {
            return;
        }

        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        customBlockData.set(componentManager.getTypeKey(), DataType.STRING, type);
        customBlockData.set(componentManager.getUUIDKey(), DataType.UUID, uuid);
    }
}
