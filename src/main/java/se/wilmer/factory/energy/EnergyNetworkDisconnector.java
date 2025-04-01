package se.wilmer.factory.energy;

import se.wilmer.factory.Factory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class EnergyNetworkDisconnector {

    private final Factory plugin;
    private final EnergyNetworkManager energyNetworkManager;

    public EnergyNetworkDisconnector(Factory plugin, EnergyNetworkManager energyNetworkManager) {
        this.plugin = plugin;
        this.energyNetworkManager = energyNetworkManager;
    }

    /**
     * Disconnect the two components from each other, and creates two new networks if needed.
     *
     * @implNote We are only getting the first components, network, as they should have the same if they are connected.
     *
     * @param firstComponent The first component
     * @param secondComponent The second component
     * @return true, if we were able to disconnect to components from each other, otherwise false.
     */
    public CompletableFuture<Boolean> disconnectComponents(EnergyComponent firstComponent, EnergyComponent secondComponent) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<EnergyNetwork> optionalEnergyNetwork = energyNetworkManager.getComponentNetwork(firstComponent).join();
            if (optionalEnergyNetwork.isEmpty()) {
                return false;
            }
            EnergyNetwork energyNetwork = optionalEnergyNetwork.get();

            if (!disconnectFromNetwork(energyNetwork, firstComponent, secondComponent)) {
                return false;
            }

            UUID firstComponentUUID = firstComponent.getUUID();
            UUID secondComponentUUID = secondComponent.getUUID();

            Map<UUID, List<UUID>> componentsConnections = energyNetwork.getComponentsConnections();
            List<UUID> firstNetworkComponents = getComponentNetwork(componentsConnections, firstComponentUUID, new ArrayList<>(Collections.singletonList(firstComponentUUID)));
            List<UUID> secondNetworkComponents = getComponentNetwork(componentsConnections, secondComponentUUID, new ArrayList<>(Collections.singletonList(secondComponentUUID)));

            //TODO: Varför skapar den inte nya networks?
            System.out.println("FirstNetworkComponents");

            if (firstNetworkComponents.equals(secondNetworkComponents)) {
                return true;
            }

            recreateNetworks(energyNetwork, firstNetworkComponents, secondNetworkComponents, firstComponent, secondComponent);
            return true;
        });
    }

    /**
     * Removes connections between two components in the energy network.
     *
     * @param network The energy network.
     * @param firstComponent The first component.
     * @param secondComponent The second component.
     * @return True if connections were successfully removed; otherwise false.
     */
    private boolean disconnectFromNetwork(EnergyNetwork network, EnergyComponent firstComponent, EnergyComponent secondComponent) {
        UUID firstComponentUUID = firstComponent.getUUID();
        UUID secondComponentUUID = secondComponent.getUUID();
        Optional<List<UUID>> firstConnections = network.getComponentsConnections(firstComponentUUID);
        Optional<List<UUID>> secondConnections = network.getComponentsConnections(secondComponentUUID);

        if (firstConnections.isEmpty() || secondConnections.isEmpty()) {
            return false;
        }

        firstConnections.get().remove(secondComponentUUID);
        secondConnections.get().remove(firstComponentUUID);
        return true;
    }

    /**
     * Recreates energy networks after components are disconnected.
     *
     * @param originalNetwork The original energy network being split.
     * @param firstNetworkComponents Components for the first new network.
     * @param secondNetworkComponents Components for the second new network.
     * @param firstComponent The first energy component.
     * @param secondComponent The second energy component.
     */
    private void recreateNetworks(EnergyNetwork originalNetwork, List<UUID> firstNetworkComponents, List<UUID> secondNetworkComponents, EnergyComponent firstComponent, EnergyComponent secondComponent) {
        Map<UUID, List<UUID>> clonedComponentsConnections = new HashMap<>(originalNetwork.getComponentsConnections());
        EnergyNetworkSerializer serializer = energyNetworkManager.getSerializer();
        serializer.deleteNetworkFile(originalNetwork.getNetworkID()).join();
        energyNetworkManager.getNetworks().remove(originalNetwork);

        final boolean shouldRemoveFirstComponent = firstNetworkComponents.size() <= 1;
        final boolean shouldRemoveSecondComponent = secondNetworkComponents.size() <= 1;

        if (shouldRemoveFirstComponent && shouldRemoveSecondComponent) {
            return;
        } else if (shouldRemoveFirstComponent) {
            clonedComponentsConnections.remove(firstComponent.getUUID());
            EnergyNetwork newEnergyNetwork = createNewNetwork(clonedComponentsConnections, secondNetworkComponents);

            List<EnergyComponent> energyComponents = newEnergyNetwork.getComponents();
            energyComponents.remove(firstComponent);
            newEnergyNetwork.getComponents().addAll(energyComponents);
            return;
        } else if (shouldRemoveSecondComponent) {
            clonedComponentsConnections.remove(secondComponent.getUUID());
            EnergyNetwork newEnergyNetwork = createNewNetwork(clonedComponentsConnections, firstNetworkComponents);

            List<EnergyComponent> energyComponents = newEnergyNetwork.getComponents();
            energyComponents.remove(secondComponent);
            newEnergyNetwork.getComponents().addAll(energyComponents);
            return;
        }

        createNewNetwork(clonedComponentsConnections, firstNetworkComponents);
        createNewNetwork(clonedComponentsConnections, secondNetworkComponents);
    }

    /**
     * Creates a new energy network using a subset of components from an existing network.
     * <p>
     * This method takes an existing mapping of components and connections and builds a new energy network
     * using only the specified components. The new network is assigned a unique identifier and is added
     * to the collection of managed networks. Additionally, the new network's data is serialized for storage.
     *
     * @param oldComponentsConnections The old components connections from an existing network.
     * @param usedComponents The components that should be included in the new network.
     * @return A new {@link EnergyNetwork}.
     */
    private EnergyNetwork createNewNetwork(Map<UUID, List<UUID>> oldComponentsConnections, List<UUID> usedComponents) {
        ConcurrentHashMap<UUID, List<UUID>> newComponentsConnections = new ConcurrentHashMap<>();
        for (UUID uuid : usedComponents) {
            newComponentsConnections.put(uuid, oldComponentsConnections.get(uuid));
        }

        UUID newNetworkUUID = UUID.randomUUID();
        EnergyNetwork energyNetwork = new EnergyNetwork(plugin, newNetworkUUID, newComponentsConnections);
        energyNetworkManager.getSerializer().serializeNetwork(newNetworkUUID);
        energyNetworkManager.getNetworks().add(energyNetwork);

        return energyNetwork;
    }

    /**
     * Recursively builds a network of connected components starting from a specified component.
     *
     * @param componentsConnections The map of componentsConnections from the original network.
     * @param componentUUID The UUID of the component to start building the network from.
     * @param connectedComponents A list that accumulates the UUIDs of all components in the connected network.
     * @return A list of UUIDs representing all components that are part of the same connected network as the starting component.
     */
    private List<UUID> getComponentNetwork(Map<UUID, List<UUID>> componentsConnections, UUID componentUUID, List<UUID> connectedComponents) {
        componentsConnections.get(componentUUID).forEach(connectedComponent -> {
            if (connectedComponents.contains(connectedComponent)) {
                return;
            }
            connectedComponents.add(connectedComponent);
            getComponentNetwork(componentsConnections, connectedComponent, connectedComponents);
        });

        return connectedComponents;
    }

}
