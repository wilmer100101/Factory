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

        long remainingEnergy = distributeRemainingEnergy(totalSuppliedEnergy, getRemainingEnergyConsumers(totalSuppliedEnergy));
    }

    private long distributeRemainingEnergy(long totalSuppliedEnergy, Map<EnergyConsumer, Long> remainingEnergyConsumers) {
        while (totalSuppliedEnergy > 0 && !remainingEnergyConsumers.isEmpty()) {
            final long energyPerConsumer = totalSuppliedEnergy / remainingEnergyConsumers.size();

            List<EnergyConsumer> energyConsumersToRemove = new ArrayList<>();
            for (Map.Entry<EnergyConsumer, Long> entry : remainingEnergyConsumers.entrySet()) {
                EnergyConsumer consumer = entry.getKey();
                long currentEnergy = entry.getValue();
                long maxEnergy = consumer.getMaxEnergyConsumption();

                long energy = Math.min(energyPerConsumer + currentEnergy, maxEnergy);
                if (energy < maxEnergy) {
                    energyConsumersToRemove.add(consumer);
                }

                totalSuppliedEnergy -= energy;
            }

            energyConsumersToRemove.forEach(remainingEnergyConsumers::remove);
        }

        return totalSuppliedEnergy;
    }

    private Map<EnergyConsumer, Long> getRemainingEnergyConsumers(long totalSuppliedEnergy) {
        Map<EnergyConsumer, Long> remainingEnergyConsumers = new HashMap<>();
        List<EnergyConsumer> clonedConsumers = new ArrayList<>(energyNetwork.getConsumers());
        final long energyPerConsumer = totalSuppliedEnergy / clonedConsumers.size();
        for (EnergyConsumer clonedConsumer : clonedConsumers) {
            final long maxEnergy = clonedConsumer.getMaxEnergyConsumption();

            if (maxEnergy <= energyPerConsumer) {
                clonedConsumer.setCurrentEnergyLimit(maxEnergy);
                continue;
            }

            remainingEnergyConsumers.put(clonedConsumer, energyPerConsumer);
        }
        return remainingEnergyConsumers;
    }
}
