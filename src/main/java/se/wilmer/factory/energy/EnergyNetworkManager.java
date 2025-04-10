package se.wilmer.factory.energy;

import se.wilmer.factory.Factory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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

    public CompletableFuture<Void> loadComponent(EnergyComponent component) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        UUID uuid = component.getUUID();

        Optional<EnergyNetwork> optionalEnergyNetwork = networks.stream()
                .filter(network -> network.getComponentsConnections().containsKey(uuid))
                .findAny();

        if (optionalEnergyNetwork.isPresent()) {
            addComponentToNetwork(component, optionalEnergyNetwork.get());
            return CompletableFuture.completedFuture(null);
        }

        getComponentFromUnloadedNetworks(uuid).thenAccept(optionalNetwork -> optionalNetwork.ifPresent(energyNetwork -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            Optional<EnergyNetwork> existingEnergyNetwork = networks.stream()
                    .filter(network -> network.getNetworkID().equals(energyNetwork.getNetworkID()))
                    .findAny();

            if (existingEnergyNetwork.isPresent()) {
                addComponentToNetwork(component, existingEnergyNetwork.get());
                future.complete(null);
                return;
            }

            addComponentToNetwork(component, energyNetwork);
            networks.add(energyNetwork);

            future.complete(null);
        })));

        return future;
    }

    private void addComponentToNetwork(EnergyComponent component, EnergyNetwork energyNetwork) {
        if (energyNetwork.getComponents().stream().noneMatch(c -> c.getUUID().equals(component.getUUID()))) {
            energyNetwork.addComponent(component);
        }
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

    public CompletableFuture<Optional<EnergyNetwork>> getComponentFromAllNetworks(EnergyComponent energyComponent) {
        Optional<EnergyNetwork> energyNetwork = getComponentFromLoadedNetworks(energyComponent);

        if (energyNetwork.isPresent()) {
            return CompletableFuture.completedFuture(energyNetwork);
        }

        return getComponentFromUnloadedNetworks(energyComponent.getUUID());
    }

    public Optional<EnergyNetwork> getComponentFromLoadedNetworks(EnergyComponent energyComponent) {
        UUID uuid = energyComponent.getUUID();

        return networks.stream()
                .filter(network -> network.getComponents().stream().anyMatch(component -> component.getUUID().equals(uuid)))
                .findAny();
    }

    private CompletableFuture<Optional<EnergyNetwork>> getComponentFromUnloadedNetworks(UUID uuid) {
        List<UUID> removeNetworks = networks.stream()
                .map(EnergyNetwork::getNetworkID)
                .toList();

        return CompletableFuture.supplyAsync(() -> {
            List<UUID> allNetworkUUIDs = serializer.getNetworkUUIDs().join();

            allNetworkUUIDs.removeAll(removeNetworks);

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
