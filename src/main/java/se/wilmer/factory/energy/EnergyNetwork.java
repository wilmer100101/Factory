package se.wilmer.factory.energy;

import org.jetbrains.annotations.NotNull;
import se.wilmer.factory.Factory;

import java.util.*;

public class EnergyNetwork {
    private static final int MAX_ITERATION_ATTEMPTS = 4000;

    private final Factory plugin;
    private final UUID networkID;
    private final EnergyNetworkDistributor distributor;
    private final Map<UUID, List<UUID>> componentsConnections;
    private final ArrayList<EnergyComponent> components = new ArrayList<>();
    private boolean isNetworkUpdateScheduled = false;

    public EnergyNetwork(Factory plugin, UUID networkID, HashMap<UUID, List<UUID>> componentsConnections) {
        this.plugin = plugin;
        this.networkID = networkID;
        this.componentsConnections = componentsConnections;

        distributor = new EnergyNetworkDistributor(this);

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::runUpdateTask, 1L, 1L);
    }

    /**
     * Schedules an energy update of the network.
     * <p>
     * This method ensures that only one network update is scheduled at each tick. If an update is already scheduled,
     * subsequent calls to this method will be ignored.
     */
    public void requestEnergyNetworkUpdate() {
        isNetworkUpdateScheduled = true;
    }

    private void runUpdateTask() {
        if (!isNetworkUpdateScheduled) {
            return;
        }

        distributor.updateNetwork();
        isNetworkUpdateScheduled = false;
    }

    public void addComponent(EnergyComponent component) {
        components.add(component);
    }

    public void removeComponent(EnergyComponent component) {
        components.remove(component);
    }

    public List<EnergyComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public List<EnergySupplier> getSuppliers() {
        return components.stream()
                .filter(EnergySupplier.class::isInstance)
                .map(EnergySupplier.class::cast)
                .toList();
    }

    public List<EnergyConsumer> getConsumers() {
        return components.stream()
                .filter(EnergyConsumer.class::isInstance)
                .map(EnergyConsumer.class::cast)
                .toList();
    }

    public List<EnergyStorage> getStorages() {
        return components.stream()
                .filter(EnergyStorage.class::isInstance)
                .map(EnergyStorage.class::cast)
                .toList();
    }

    public Map<UUID, List<UUID>> getComponentsConnections() {
        return componentsConnections;
    }

    public Optional<List<UUID>> getComponentsConnections(UUID uuid) {
        if (components.stream().noneMatch(component -> component.getUUID().equals(uuid))) {
            return Optional.empty();
        }

        return Optional.of(componentsConnections.computeIfAbsent(uuid, k -> new ArrayList<>()));
    }

    public UUID getNetworkID() {
        return networkID;
    }
}
