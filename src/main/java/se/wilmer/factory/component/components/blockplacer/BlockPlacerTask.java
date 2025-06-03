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
        if (blockPlacerEntity.getMaxEnergyConsumption() > blockPlacerEntity.getCurrentEnergyLimit()) {
            return;
        }

        Optional<Block> optionalTargetBlock = blockPlacerEntity.getTargetBlock();
        if (optionalTargetBlock.isEmpty()) {
            return;
        }
        Block targetBlock = optionalTargetBlock.get();
        if (!targetBlock.isEmpty()) {
            return;
        }

        Block block = blockPlacerEntity.getBlock();
        if (!(block.getState() instanceof Container container)) {
            return;
        }

        Inventory inventory = container.getInventory();
        for (int i = 0; i < inventory.getContents().length; i++) {
            ItemStack content = container.getInventory().getContents()[i];
            if (content == null) {
                continue;
            }
            Material type = content.getType();
            if (!targetBlock.canPlace(type.createBlockData()) || !blockPlacerEntity.getComponent().getAllowedMaterials().contains(content.getType())) {
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
