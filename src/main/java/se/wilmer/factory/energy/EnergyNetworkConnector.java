package se.wilmer.factory.energy;

import org.bukkit.scheduler.BukkitScheduler;
import se.wilmer.factory.Factory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EnergyNetworkConnector {
    private final Factory plugin;
    private final EnergyNetworkManager energyNetworkManager;

    public EnergyNetworkConnector(Factory plugin, EnergyNetworkManager energyNetworkManager) {
        this.plugin = plugin;
        this.energyNetworkManager = energyNetworkManager;
    }

    /**
     * Connects two energy components by either creating a new network, merging existing networks,
     * or adding the components to one of the existing networks.
     * If both components have the same network, will no connection be created.
     *
     * @param firstComponent  The first energy component to connect.
     * @param secondComponent The second energy component to connect.
     * @return A CompletableFuture containing true when the connection is successfully established.
     */
    public boolean connectComponents(EnergyComponent firstComponent, EnergyComponent secondComponent) {
        EnergyNetwork firstNetwork = energyNetworkManager.getComponentFromLoadedNetworks(firstComponent).orElse(null);
        EnergyNetwork secondNetwork = energyNetworkManager.getComponentFromLoadedNetworks(secondComponent).orElse(null);

        if (secondNetwork != null && firstNetwork == secondNetwork) {
            return false;
        }

        if (firstNetwork == null && secondNetwork == null) {
            createNewNetwork(firstComponent, secondComponent);
            return true;
        }

        if (firstNetwork != null && secondNetwork != null) {
            mergeNetworks(firstNetwork, secondNetwork, firstComponent.getUUID(), secondComponent.getUUID());
            return true;
        }

        if (firstNetwork != null) {
            addComponentToNetwork(secondComponent, firstComponent, firstNetwork);
        } else {
            addComponentToNetwork(firstComponent, secondComponent, secondNetwork);
        }

        return true;
    }

    /**
     * Adds a new component to an existing energy network and establishes mutual connections with an existing component.
     *
     * @param newComponent      The new energy component to add.
     * @param existingComponent The existing energy component already in the network.
     * @param energyNetwork     The energy network to which the component is being added.
     */
    private void addComponentToNetwork(EnergyComponent newComponent, EnergyComponent existingComponent, EnergyNetwork energyNetwork) {
        energyNetwork.addComponent(newComponent);

        UUID existingComponentUUID = existingComponent.getUUID();
        UUID newComponentUUID = newComponent.getUUID();

        energyNetwork.getComponentsConnections(existingComponentUUID).ifPresent(c -> c.add(newComponentUUID));
        energyNetwork.getComponentsConnections(newComponentUUID).ifPresent(c -> c.add(existingComponentUUID));

        energyNetwork.requestEnergyNetworkUpdate();
        energyNetworkManager.getSerializer().deserializeNetwork(energyNetwork);
    }

    /**
     * Merges two energy networks into one, combining their components and connections.
     *
     * @param firstNetwork        The first energy network to merge.
     * @param secondNetwork       The second energy network to merge.
     * @param firstComponentUUID  The UUID of the first component linking the networks.
     * @param secondComponentUUID The UUID of the second component linking the networks.
     */
    private void mergeNetworks(EnergyNetwork firstNetwork, EnergyNetwork secondNetwork, UUID firstComponentUUID, UUID secondComponentUUID) {
        firstNetwork.getComponentsConnections().putAll(secondNetwork.getComponentsConnections());

        secondNetwork.getComponents().forEach(firstNetwork::addComponent);

        firstNetwork.getComponentsConnections(firstComponentUUID).ifPresent(connections -> connections.add(secondComponentUUID));
        firstNetwork.getComponentsConnections(secondComponentUUID).ifPresent(connections -> connections.add(firstComponentUUID));

        energyNetworkManager.getNetworks().remove(secondNetwork);
        firstNetwork.requestEnergyNetworkUpdate();

        EnergyNetworkSerializer serializer = energyNetworkManager.getSerializer();
        serializer.deserializeNetwork(firstNetwork);
        serializer.deleteNetworkFile(secondNetwork.getNetworkID());
    }


    /**
     * Creates a new energy network containing two components and establishes mutual connections between them.
     *
     * @param firstComponent  The first energy component to include in the network.
     * @param secondComponent The second energy component to include in the network.
     */
    private void createNewNetwork(EnergyComponent firstComponent, EnergyComponent secondComponent) {
        UUID networkUUID = UUID.randomUUID();

        UUID firstComponentUUID = firstComponent.getUUID();
        UUID secondComponentUUID = secondComponent.getUUID();

        HashMap<UUID, List<UUID>> componentsConnections = new HashMap<>();
        componentsConnections.put(firstComponentUUID, new ArrayList<>(Collections.singletonList(secondComponentUUID)));
        componentsConnections.put(secondComponentUUID, new ArrayList<>(Collections.singletonList(firstComponentUUID)));

        EnergyNetwork energyNetwork = new EnergyNetwork(plugin, networkUUID, componentsConnections);
        energyNetwork.addComponent(firstComponent);
        energyNetwork.addComponent(secondComponent);

        energyNetwork.requestEnergyNetworkUpdate();

        energyNetworkManager.getNetworks().add(energyNetwork);
        energyNetworkManager.getSerializer().deserializeNetwork(energyNetwork);
    }
}
