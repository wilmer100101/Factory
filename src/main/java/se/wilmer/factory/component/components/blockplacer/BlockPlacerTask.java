package se.wilmer.factory.component.components.blockplacer;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class BlockPlacerTask implements Runnable {
    private final BlockPlacerEntity blockPlacerEntity;

    public BlockPlacerTask(BlockPlacerEntity blockPlacerEntity) {
        this.blockPlacerEntity = blockPlacerEntity;
    }

    @Override
    public void run() {
        Block block = blockPlacerEntity.getBlock();
        if (block.isBlockPowered()) {
            return;
        }
        if (!(block.getState() instanceof Container container)) {
            return;
        }

        Optional<Block> optionalTargetBlock = blockPlacerEntity.getTargetBlock();
        if (optionalTargetBlock.isEmpty()) {
            return;
        }
        Block targetBlock = optionalTargetBlock.get();

        Inventory inventory = container.getInventory();
        for (int i = 0; i < inventory.getContents().length; i++) {
            ItemStack content = container.getInventory().getContents()[i];
            if (content == null) {
                continue;
            }
            Material type = content.getType();
            if (!targetBlock.isEmpty() || !targetBlock.canPlace(type.createBlockData())) {
                continue;
            }
            targetBlock.setType(type);

            removeItemFromInventory(content, inventory, i);
            break;
        }
    }

    private void removeItemFromInventory(ItemStack content, Inventory inventory, int i) {
        int amount = content.getAmount();
        if (amount > 1) {
            content.setAmount(amount - 1);
            inventory.setItem(i, content);
            return;
        }
        inventory.setItem(i, ItemStack.empty());
    }
}
