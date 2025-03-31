package se.wilmer.factory.energy;

import se.wilmer.factory.Factory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EnergyNetworkManager {
    private final Factory plugin;
    private final EnergyNetworkSerializer serializer;
    private final EnergyNetworkDisconnector disconnector;
    private final EnergyNetworkConnector connector;
    private final List<EnergyNetwork> networks = new ArrayList<>();

    public EnergyNetworkManager(Factory plugin) {
        this.plugin = plugin;

        serializer = new EnergyNetworkSerializer(plugin);
        disconnector = new EnergyNetworkDisconnector(plugin, this);
        connector = new EnergyNetworkConnector(plugin, this);
    }

    public void loadComponent(EnergyComponent component) {
        getComponentNetwork(component).thenAccept(optionalNetwork -> {
            if (optionalNetwork.isEmpty()) {
                return;
            }
            EnergyNetwork energyNetwork = optionalNetwork.get();

            if (!networks.contains(energyNetwork)) {
                networks.add(energyNetwork);
            }
            energyNetwork.addComponent(component);
        });
    }

    public void unloadComponent(EnergyComponent component) {
        getComponentNetwork(component)
                .thenAcceptAsync(optionalEnergyNetwork -> optionalEnergyNetwork.ifPresent(energyNetwork -> {
                    energyNetwork.removeComponent(component);

                    if (energyNetwork.getComponents().isEmpty()) {
                        networks.remove(energyNetwork);
                    }
                }));
    }

    public CompletableFuture<Optional<EnergyNetwork>> getComponentNetwork(EnergyComponent energyComponent) {
        UUID uuid = energyComponent.getUUID();

        Optional<EnergyNetwork> energyNetwork = networks.stream()
                .filter(network -> network.getComponents().stream().anyMatch(component -> component.getUUID().equals(uuid)))
                .findAny();

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
                if (optionalNetwork.get().getComponentsConnections(uuid).isEmpty()) {
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
