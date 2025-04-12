package se.wilmer.factory.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private long distributeEnergy(float totalSuppliedEnergy) {
        Map<EnergyConsumer, Float> remainingConsumers = new HashMap<>();

        for (EnergyConsumer consumer : energyNetwork.getConsumers()) {
            remainingConsumers.put(consumer, 0f);
            consumer.setCurrentEnergyLimit(0);
        }

        while (totalSuppliedEnergy > 1 && !remainingConsumers.isEmpty()) {

            final float energyPerConsumer = totalSuppliedEnergy / remainingConsumers.size();

            List<EnergyConsumer> consumersToRemove = new ArrayList<>();
            for (Map.Entry<EnergyConsumer, Float> entry : remainingConsumers.entrySet()) {
                EnergyConsumer consumer = entry.getKey();
                float currentEnergy = entry.getValue();
                long maxEnergy = consumer.getMaxEnergyConsumption();

                float energy = Math.min(energyPerConsumer + currentEnergy, maxEnergy);
                if (energy >= maxEnergy) {
                    consumersToRemove.add(consumer);
                }

                consumer.setCurrentEnergyLimit((long) energy);
                entry.setValue(energy);

                totalSuppliedEnergy -= (energy - currentEnergy);
            }

            consumersToRemove.forEach(remainingConsumers::remove);
        }

        return (long) totalSuppliedEnergy;
    }

}
