package se.wilmer.factory.tools.multimeter;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentManager;
import se.wilmer.factory.energy.EnergyConsumer;
import se.wilmer.factory.energy.EnergySupplier;
import se.wilmer.factory.tools.Tool;

import java.util.Optional;
import java.util.UUID;

public class Multimeter extends Tool {
    private final Factory plugin;

    public Multimeter(Factory plugin) {
        super(plugin, "multimeter");

        this.plugin = plugin;
    }

    @Override
    public void load() {
        plugin.getServer().getPluginManager().registerEvents(new MultimeterListener(this), plugin);
    }

    public Optional<Component> getEnergyInformation(Block block) {
        ComponentManager componentManager = plugin.getComponentManager();

        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        UUID uuid = customBlockData.get(componentManager.getUUIDKey(), DataType.UUID);
        if (uuid == null) {
            return Optional.empty();
        }

        Optional<ComponentEntity<?>> optionalComponentEntity = plugin.getComponentManager().getComponentEntity(uuid);
        if (optionalComponentEntity.isEmpty()) {
            return Optional.empty();
        }
        ComponentEntity<?> componentEntity = optionalComponentEntity.get();

        if (componentEntity instanceof EnergyConsumer energyConsumer) {
            return Optional.of(Component.text("Consumed Energy: " + energyConsumer.getCurrentEnergyLimit() + " / " + energyConsumer.getMaxEnergyConsumption()));
        } else if (componentEntity instanceof EnergySupplier energySupplier) {
            return Optional.of(Component.text("Supplied Energy: " + energySupplier.getSuppliedEnergy()));
        }

        return Optional.empty();
    }
}
