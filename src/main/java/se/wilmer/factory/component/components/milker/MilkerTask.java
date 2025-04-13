package se.wilmer.factory.component.components.milker;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import se.wilmer.factory.Factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class MilkerTask implements Runnable {
    private static final Class<?>[] MILKING_ENTITIES = new Class[]{
            Cow.class,
            Goat.class,
            MushroomCow.class
    };

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
        if (world.getEntitiesByClasses(MILKING_ENTITIES).isEmpty() || !(block.getState() instanceof Container container)) {
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
