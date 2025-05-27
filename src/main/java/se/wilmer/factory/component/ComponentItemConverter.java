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
import se.wilmer.factory.Factory;

import java.util.Objects;
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
        Block block = customBlockData.getBlock();
        if (type == null || block == null) {
            return;
        }

        Event originalEvent = event.getBukkitEvent();
        switch (originalEvent) {
            case BlockBreakEvent blockBreakEvent -> {
                blockBreakEvent.setDropItems(false);
                transferBlockToItem(customBlockData, type);
            }
            case BlockPlaceEvent blockPlaceEvent -> {
                blockPlaceEvent.setCancelled(true);
                transferBlockToItem(customBlockData, type);

                block.setBlockData(blockPlaceEvent.getBlockPlaced().getBlockData());
            }
            case BlockExplodeEvent blockExplodeEvent -> {
                transferBlockToItem(customBlockData, type);
                blockExplodeEvent.blockList().remove(block);
                block.setType(Material.AIR);
            }
            case EntityExplodeEvent entityExplodeEvent -> {
                entityExplodeEvent.blockList().remove(block);
                transferBlockToItem(customBlockData, type);
                block.setType(Material.AIR);
            }
            default -> {
            }
        }
    }

    private void transferBlockToItem(CustomBlockData customBlockData, String type) {
        Block block = Objects.requireNonNull(customBlockData.getBlock());

        plugin.getItemManager().getSerializer().getItemFromBlock(customBlockData).ifPresent(itemStack -> {
            itemStack.editPersistentDataContainer(pdc -> pdc.set(componentManager.getTypeKey(), DataType.STRING, type));
            block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
        });
    }

    public void transferDataFromItem(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack itemStack = event.getItemInHand();

        PersistentDataContainerView pdc = itemStack.getPersistentDataContainer();
        String type = pdc.get(componentManager.getTypeKey(), DataType.STRING);
        if (type == null) {
            return;
        }

        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        customBlockData.set(componentManager.getTypeKey(), DataType.STRING, type);
        customBlockData.set(componentManager.getUUIDKey(), DataType.UUID, UUID.randomUUID());

        plugin.getItemManager().getSerializer().saveItemToBlock(itemStack, customBlockData);
    }
}
