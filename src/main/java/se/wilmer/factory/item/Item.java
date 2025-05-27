package se.wilmer.factory.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public record Item(ItemStack itemStack, NamespacedKey namespacedKey) {
}