package se.wilmer.factory.component.components.milker;

import io.papermc.paper.entity.Shearable;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import se.wilmer.factory.Factory;

import java.util.*;


public class MilkerTask implements Runnable {
    private static final List<EntityType> MILKING_ENTITIES = List.of(
            EntityType.COW,
            EntityType.GOAT,
            EntityType.MOOSHROOM
    );

    private final Factory plugin;
    private final MilkerEntity milkerEntity;
    private long lastTimeRun = 0L;

    public MilkerTask(Factory plugin, MilkerEntity milkerEntity) {
        this.plugin = plugin;
        this.milkerEntity = milkerEntity;
    }

    @Override
    public void run() {
        lastTimeRun = plugin.getServer().getCurrentTick();

        Block block = milkerEntity.getBlock();
        World world = block.getWorld();
        Location location = block.getLocation();


        Optional<Block> optionalTargetBlock = milkerEntity.getTargetBlock();
        if (optionalTargetBlock.isEmpty() || !(block.getState() instanceof Container container) || world.getNearbyEntities(optionalTargetBlock.get().getBoundingBox()).stream().noneMatch(livingEntity -> MILKING_ENTITIES.contains(livingEntity.getType()))) {
            return;
        }

        Inventory inventory = container.getInventory();
        int bucketSlot = inventory.first(Material.BUCKET);
        if (bucketSlot == -1 || !inventory.addItem(new ItemStack(Material.MILK_BUCKET)).isEmpty()) {
            return;
        }

        removeBucketItem(inventory, bucketSlot);
        world.playSound(location, Sound.ENTITY_COW_MILK, 1f, 1f);
    }

    private void removeBucketItem(Inventory inventory, int bucketSlot) {
        ItemStack bucket = Objects.requireNonNull(inventory.getItem(bucketSlot));
        if (bucket.getAmount() >= 1) {
            bucket.subtract();
        } else {
            inventory.setItem(bucketSlot, new ItemStack(Material.AIR));
        }
    }

    public long getLastTimeRun() {
        return lastTimeRun;
    }
}
