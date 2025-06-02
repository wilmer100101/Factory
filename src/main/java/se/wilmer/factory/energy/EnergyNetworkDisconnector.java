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
     * @param firstComponent  The first component
     * @param secondComponent The second component
     * @return true, if we were able to disconnect to components from each other, otherwise false.
     * @implNote We are only getting the first components, network, as they should have the same if they are connected.
     */
    public boolean disconnectComponents(EnergyComponent firstComponent, EnergyComponent secondComponent) {
        Optional<EnergyNetwork> optionalEnergyNetwork = energyNetworkManager.getComponentFromLoadedNetworks(firstComponent);
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

        if (firstNetworkComponents.equals(secondNetworkComponents)) {
            return true;
        }

        recreateNetworks(energyNetwork, firstNetworkComponents, secondNetworkComponents, firstComponent, secondComponent);

        energyNetworkManager.getSerializer().deleteNetworkFile(energyNetwork.getNetworkID());
        energyNetworkManager.getNetworks().remove(energyNetwork);
        return true;
    }

    /**
     * Removes connections between two components in the energy network.
     *
     * @param network         The energy network.
     * @param firstComponent  The first component.
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
     * @param originalNetwork         The original energy network being split.
     * @param firstNetworkComponents  Components for the first new network.
     * @param secondNetworkComponents Components for the second new network.
     * @param firstComponent          The first energy component.
     * @param secondComponent         The second energy component.
     */
    private void recreateNetworks(EnergyNetwork originalNetwork, List<UUID> firstNetworkComponents, List<UUID> secondNetworkComponents, EnergyComponent firstComponent, EnergyComponent secondComponent) {
        Map<UUID, List<UUID>> clonedComponentsConnections = new HashMap<>(originalNetwork.getComponentsConnections());
        EnergyNetworkSerializer serializer = energyNetworkManager.getSerializer();

        final boolean shouldRemoveFirstComponent = firstNetworkComponents.size() <= 1;
        final boolean shouldRemoveSecondComponent = secondNetworkComponents.size() <= 1;

        if (shouldRemoveFirstComponent && shouldRemoveSecondComponent) {
            removeSavedEnergy(firstComponent);
            removeSavedEnergy(secondComponent);
            return;
        } else if (shouldRemoveFirstComponent) {
            UUID uuid = firstComponent.getUUID();
            clonedComponentsConnections.remove(uuid);
            secondNetworkComponents.remove(uuid);
            EnergyNetwork newEnergyNetwork = createNewNetwork(clonedComponentsConnections, secondNetworkComponents);

            removeSavedEnergy(firstComponent);
            serializer.deserializeNetwork(newEnergyNetwork);
            return;
        } else if (shouldRemoveSecondComponent) {
            UUID uuid = secondComponent.getUUID();
            clonedComponentsConnections.remove(uuid);
            firstNetworkComponents.remove(uuid);
            EnergyNetwork newEnergyNetwork = createNewNetwork(clonedComponentsConnections, firstNetworkComponents);

            removeSavedEnergy(secondComponent);
            serializer.deserializeNetwork(newEnergyNetwork);
            return;
        }

        serializer.deserializeNetwork(createNewNetwork(clonedComponentsConnections, firstNetworkComponents));
        serializer.deserializeNetwork(createNewNetwork(clonedComponentsConnections, secondNetworkComponents));
    }

    /**
     * Creates a new energy network using a subset of components from an existing network.
     * <p>
     * This method takes an existing mapping of components and connections and builds a new energy network
     * using only the specified components. The new network is assigned a unique identifier and is added
     * to the collection of managed networks. The usedComponents is also added to the new network.
     *
     * @param oldComponentsConnections The old components connections from an existing network.
     * @param usedComponents           The components that should be included in the new network.
     * @return A new {@link EnergyNetwork}.
     */
    private EnergyNetwork createNewNetwork(Map<UUID, List<UUID>> oldComponentsConnections, List<UUID> usedComponents) {
        HashMap<UUID, List<UUID>> newComponentsConnections = new HashMap<>();
        for (UUID uuid : usedComponents) {
            newComponentsConnections.put(uuid, oldComponentsConnections.get(uuid));
        }

        UUID newNetworkUUID = UUID.randomUUID();
        EnergyNetwork energyNetwork = new EnergyNetwork(plugin, newNetworkUUID, newComponentsConnections);
        usedComponents.forEach(usedComponent -> energyNetworkManager.getEnergyComponent(usedComponent).ifPresent(energyNetwork::addComponent));

        energyNetworkManager.getNetworks().add(energyNetwork);
        energyNetwork.requestEnergyNetworkUpdate();

        return energyNetwork;
    }

    /**
     * Recursively builds a network of connected components starting from a specified component.
     *
     * @param componentsConnections The map of componentsConnections from the original network.
     * @param componentUUID         The UUID of the component to start building the network from.
     * @param connectedComponents   A list that accumulates the UUIDs of all components in the connected network.
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

    /**
     * If the component has saved energy that does no longer belongs to the component, will that energy be removed.
     *
     * @param energyComponent The component to remove the energy from.
     */
    private void removeSavedEnergy(EnergyComponent energyComponent) {
        if (energyComponent instanceof EnergyConsumer energyConsumer) {
            energyConsumer.setCurrentEnergyLimit(0);
        }
    }
}
