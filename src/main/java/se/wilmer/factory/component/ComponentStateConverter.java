package se.wilmer.factory.component;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.customblockdata.events.CustomBlockDataMoveEvent;
import com.jeff_media.customblockdata.events.CustomBlockDataRemoveEvent;
import com.jeff_media.morepersistentdatatypes.DataType;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import se.wilmer.factory.Factory;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ComponentStateConverter {
    private final Factory plugin;
    private final ComponentManager componentManager;

    public ComponentStateConverter(Factory plugin, ComponentManager componentManager) {
        this.plugin = plugin;
        this.componentManager = componentManager;
    }

    public void toNewLocation(CustomBlockDataMoveEvent event) {
        CustomBlockData customBlockData = event.getCustomBlockData();
        if (customBlockData.has(componentManager.getUUIDKey())) {
            event.setCancelled(true);
        }
    }

    public void toBlock(BlockPlaceEvent event) {
        transferDataFromItem(event).flatMap(type -> componentManager.getRegistry().getComponentByID(type)).ifPresent(component -> {
            component.createEntity(event.getBlockPlaced()).spawn();
        });
    }

    public void toItem(CustomBlockDataRemoveEvent event) {
        transferDataFromBlock(event).flatMap(componentManager::getComponentEntity).ifPresent(componentEntity -> {
            plugin.getEnergyNetworkManager().unloadComponent(componentEntity);
            componentEntity.despawn();
        });
    }

    private Optional<UUID> transferDataFromBlock(CustomBlockDataRemoveEvent event) {
        CustomBlockData customBlockData = event.getCustomBlockData();
        String type = customBlockData.get(componentManager.getTypeKey(), DataType.STRING);
        Block block = customBlockData.getBlock();
        UUID uuid = customBlockData.get(componentManager.getUUIDKey(), DataType.UUID);
        if (type == null || block == null || uuid == null) {
            return Optional.empty();
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

        return Optional.of(uuid);
    }

    private void transferBlockToItem(CustomBlockData customBlockData, String type) {
        Block block = Objects.requireNonNull(customBlockData.getBlock());

        plugin.getItemManager().getSerializer().getItemFromBlock(customBlockData).ifPresent(itemStack -> {
            itemStack.editPersistentDataContainer(pdc -> pdc.set(componentManager.getTypeKey(), DataType.STRING, type));
            block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
        });
    }

    private Optional<String> transferDataFromItem(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack itemStack = event.getItemInHand();

        PersistentDataContainerView pdc = itemStack.getPersistentDataContainer();
        String type = pdc.get(componentManager.getTypeKey(), DataType.STRING);
        if (type == null) {
            return Optional.empty();
        }

        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        customBlockData.set(componentManager.getTypeKey(), DataType.STRING, type);
        customBlockData.set(componentManager.getUUIDKey(), DataType.UUID, UUID.randomUUID());

        plugin.getItemManager().getSerializer().saveItemToBlock(itemStack, customBlockData);

        return Optional.of(type);
    }
}
