package se.wilmer.factory.energy;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class EnergyNetworkDistributor {
    private final EnergyNetwork energyNetwork;

    public EnergyNetworkDistributor(EnergyNetwork energyNetwork) {
        this.energyNetwork = energyNetwork;
    }

    public void updateNetwork() {
        long totalSuppliedEnergy = 0;

        for (EnergySupplier supplier : energyNetwork.getSuppliers()) {
            totalSuppliedEnergy += supplier.getSuppliedEnergy();
        }

        long remainingEnergy = distributeEnergy(totalSuppliedEnergy);
    }

    private long distributeEnergy(long totalSuppliedEnergy) {
        Map<EnergyConsumer, Long> remainingConsumers = new HashMap<>();

        for (EnergyConsumer consumer : energyNetwork.getConsumers()) {
            remainingConsumers.put(consumer, 0L);
            consumer.setCurrentEnergyLimit(0);
        }

        while (totalSuppliedEnergy >= 1 && !remainingConsumers.isEmpty()) {

            final long energyPerConsumer = totalSuppliedEnergy / remainingConsumers.size();

            List<EnergyConsumer> consumersToRemove = new ArrayList<>();
            for (Map.Entry<EnergyConsumer, Long> entry : remainingConsumers.entrySet()) {
                EnergyConsumer consumer = entry.getKey();
                long currentEnergy = entry.getValue();
                long maxEnergy = consumer.getMaxEnergyConsumption();

                long energy = Math.min(energyPerConsumer + currentEnergy, maxEnergy);
                if (energy <= maxEnergy) {
                    consumersToRemove.add(consumer);
                }

                consumer.setCurrentEnergyLimit(energy);
                totalSuppliedEnergy -= energy;
            }

            consumersToRemove.forEach(remainingConsumers::remove);
        }

        return totalSuppliedEnergy;
    }

}
