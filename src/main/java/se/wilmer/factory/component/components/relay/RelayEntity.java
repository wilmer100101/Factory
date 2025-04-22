package se.wilmer.factory.component.components.relay;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfo;
import se.wilmer.factory.component.components.fuelburner.FuelBurnerTask;
import se.wilmer.factory.energy.EnergyComponent;
import se.wilmer.factory.energy.EnergyNetwork;
import se.wilmer.factory.energy.EnergySupplier;

public class RelayEntity extends ComponentEntity<Relay> implements EnergyComponent {
    private final RelayData data;

    public RelayEntity(Factory plugin, Relay component, RelayData data, Block block) {
        super(plugin, component, block);

        this.data = data;
    }

    @Override
    public RelayData getData() {
        return data;
    }
}
