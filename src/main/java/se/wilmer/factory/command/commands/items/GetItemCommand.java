package se.wilmer.factory.command.commands.items;


import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.wilmer.factory.Factory;
import se.wilmer.factory.item.Item;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class GetItemCommand {
    private final Factory plugin;

    public GetItemCommand(Factory plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("get")
                .requires(ctx -> ctx.getExecutor() instanceof Player)
                .then(Commands.argument("key", StringArgumentType.word())
                        .requires(sender -> sender.getExecutor() instanceof Player player && player.hasPermission("factory.command.items.getitem"))
                        .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                            plugin.getItemManager().getKeys().stream()
                                    .map(NamespacedKey::getKey).toList().stream()
                                    .filter(entry -> entry.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                    .forEach(builder::suggest);

                            return builder.build();
                        }))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();

                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            String key = StringArgumentType.getString(ctx, "key");
                            Optional<Item> item = plugin.getItemManager().getItem(new NamespacedKey(plugin, key));
                            if (item.isEmpty()) {
                                player.sendMessage(Component.text("That item does not exist.", NamedTextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            player.getInventory().addItem(item.get().itemStack());
                            player.sendMessage(Component.text("You have now been given the selected item.", NamedTextColor.GREEN));

                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}
