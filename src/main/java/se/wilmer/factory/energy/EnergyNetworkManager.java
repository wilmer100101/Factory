package se.wilmer.factory.energy;

import se.wilmer.factory.Factory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class EnergyNetworkManager {
    private final Factory plugin;
    private final EnergyNetworkSerializer serializer;
    private final EnergyNetworkDisconnector disconnector;
    private final EnergyNetworkConnector connector;
    private final ArrayList<EnergyNetwork> networks = new ArrayList<>();

    public EnergyNetworkManager(Factory plugin) {
        this.plugin = plugin;

        serializer = new EnergyNetworkSerializer(plugin);
        disconnector = new EnergyNetworkDisconnector(plugin, this);
        connector = new EnergyNetworkConnector(plugin, this);
    }

    public void loadComponent(EnergyComponent component) {
        getComponentFromAllNetworks(component).thenAccept(optionalNetwork -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (optionalNetwork.isEmpty()) {
                return;
            }
            EnergyNetwork energyNetwork = optionalNetwork.get();

            if (!networks.contains(energyNetwork)) {
                networks.add(energyNetwork);
            }

            energyNetwork.addComponent(component);
        }));
    }

    public void unloadComponent(EnergyComponent component) {
        getComponentFromLoadedNetworks(component)
                .ifPresent(energyNetwork -> {
                    energyNetwork.removeComponent(component);

                    if (energyNetwork.getComponents().isEmpty()) {
                        networks.remove(energyNetwork);
                    }
                });
    }

    public Optional<EnergyNetwork> getComponentFromLoadedNetworks(EnergyComponent energyComponent) {
        UUID uuid = energyComponent.getUUID();

        return networks.stream()
                .filter(network -> network.getComponents().stream().anyMatch(component -> component.getUUID().equals(uuid)))
                .findAny();
    }

    public CompletableFuture<Optional<EnergyNetwork>> getComponentFromAllNetworks(EnergyComponent energyComponent) {
        UUID uuid = energyComponent.getUUID();

        Optional<EnergyNetwork> energyNetwork = getComponentFromLoadedNetworks(energyComponent);

        if (energyNetwork.isPresent()) {
            return CompletableFuture.completedFuture(energyNetwork);
        }

        List<UUID> networkUUIDs = networks.stream()
                .map(EnergyNetwork::getNetworkID)
                .toList();

        return CompletableFuture.supplyAsync(() -> {
            List<UUID> allNetworkUUIDs = serializer.getNetworkUUIDs().join();
            allNetworkUUIDs.removeAll(networkUUIDs);

            for (UUID networkUUID : allNetworkUUIDs) {
                Optional<EnergyNetwork> optionalNetwork = serializer.serializeNetwork(networkUUID).join();
                if (optionalNetwork.isEmpty()) {
                    continue;
                }
                if (!optionalNetwork.get().getComponentsConnections().containsKey(uuid)) {
                    continue;
                }

                return optionalNetwork;
            }

            return Optional.empty();
        });
    }

    public Optional<EnergyComponent> getEnergyComponent(UUID uuid) {
        return networks.stream()
                .flatMap(network -> network.getComponents().stream())
                .filter(component -> component.getUUID().equals(uuid))
                .findAny();
    }

    public EnergyNetworkSerializer getSerializer() {
        return serializer;
    }

    public EnergyNetworkConnector getConnector() {
        return connector;
    }

    public EnergyNetworkDisconnector getDisconnector() {
        return disconnector;
    }

    public List<EnergyNetwork> getNetworks() {
        return networks;
    }
}
