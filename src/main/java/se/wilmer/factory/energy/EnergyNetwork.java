package se.wilmer.factory.energy;

import org.jetbrains.annotations.Nullable;
import se.wilmer.factory.Factory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EnergyNetwork {
    private static final int MAX_ITERATION_ATTEMPTS = 4000;

    private final Factory plugin;
    private final UUID networkID;
    private final ConcurrentHashMap<UUID, List<UUID>> componentsConnections;
    private final List<EnergyConsumer> consumers = new ArrayList<>();
    private final List<EnergySupplier> suppliers = new ArrayList<>();
    private final List<EnergyComponent> components = new ArrayList<>();
    private boolean isNetworkUpdateScheduled = false;

    public EnergyNetwork(Factory plugin, UUID networkID, ConcurrentHashMap<UUID, List<UUID>> componentsConnections) {
        this.plugin = plugin;
        this.networkID = networkID;
        this.componentsConnections = componentsConnections;
    }

    public void addComponent(EnergyComponent component) {
        if (component instanceof EnergyConsumer energyConsumer) {
            consumers.add(energyConsumer);
        } else if (component instanceof EnergySupplier energySupplier) {
            suppliers.add(energySupplier);
        } else {
            components.add(component);
        }
    }

    public void removeComponent(EnergyComponent component) {
        if (component instanceof EnergyConsumer energyConsumer) {
            consumers.remove(energyConsumer);
        } else if (component instanceof EnergySupplier energySupplier) {
            suppliers.remove(energySupplier);
        } else {
            components.remove(component);
        }
    }

    public List<EnergyComponent> getComponents() {
        List<EnergyComponent> allComponents = new ArrayList<>();
        allComponents.addAll(consumers);
        allComponents.addAll(suppliers);
        allComponents.addAll(components);
        return Collections.unmodifiableList(allComponents);
    }

    public Map<UUID, List<UUID>> getComponentsConnections() {
        return componentsConnections;
    }

    public Optional<List<UUID>> getComponentsConnections(UUID uuid) {
        if (componentsConnections.containsKey(uuid)) {
            return Optional.of(componentsConnections.get(uuid));
        }
        if (components.stream().filter(component -> component.getUUID().equals(uuid)).findAny().isEmpty()) {
            return Optional.empty();
        }
        List<UUID> connections = new ArrayList<>();
        componentsConnections.put(uuid, connections);
        return Optional.of(connections);
    }

    public UUID getNetworkID() {
        return networkID;
    }

    /**
     * Schedules an update of the energy network.
     * <p>
     * This method ensures that only one network update is scheduled at each tick. If an update is already scheduled,
     * subsequent calls to this method will be ignored. The actual update is performed on the next tick.
     *
     * @see #updateNetwork()
     */
    public void requestEnergyNetworkUpdate() {
        if (isNetworkUpdateScheduled) {
            return;
        }
        isNetworkUpdateScheduled = true;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updateNetwork();
            isNetworkUpdateScheduled = false;
        }, 1L);
    }

    private void updateNetwork() {
        long totalSuppliedEnergy = 0;
        for (EnergySupplier supplier : suppliers) {
            totalSuppliedEnergy += supplier.getSuppliedEnergy();
        }

        distributeRemainingEnergy(totalSuppliedEnergy, getRemainingEnergyConsumers(totalSuppliedEnergy));
    }

    private void distributeRemainingEnergy(long totalSuppliedEnergy, Map<EnergyConsumer, Long> remainingEnergyConsumers) {
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
    }

    private Map<EnergyConsumer, Long> getRemainingEnergyConsumers(long totalSuppliedEnergy) {
        Map<EnergyConsumer, Long> remainingEnergyConsumers = new HashMap<>();
        List<EnergyConsumer> clonedConsumers = new ArrayList<>(consumers);
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
