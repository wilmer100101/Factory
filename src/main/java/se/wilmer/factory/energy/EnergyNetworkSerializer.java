package se.wilmer.factory.energy;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.Types;
import se.wilmer.factory.Factory;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class EnergyNetworkSerializer {
    private static final TypeToken<Map<UUID, List<UUID>>> COMPONENTS_CONNECTIONS_MAP = Types.makeMap(new TypeToken<>() {
    }, Types.makeList(UUID.class));

    private final Factory plugin;
    private final Path networkDataPath;
    private final Path networkResourcePath;
    private final Map<UUID, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    public EnergyNetworkSerializer(Factory plugin) {
        this.plugin = plugin;

        networkDataPath = plugin.getDataFolder().toPath().resolve("energy_networks");
        networkResourcePath = Path.of("energy_networks");
    }

    public CompletableFuture<List<UUID>> getNetworkUUIDs() {
        return CompletableFuture.supplyAsync(() -> {
            List<UUID> uuids = new ArrayList<>();
            try (Stream<Path> stream = Files.list(networkDataPath)) {
                stream.forEach(path -> {
                    String fileName = path.getFileName().toString();
                    String uuid = fileName.replace(".json", "");
                    uuids.add(UUID.fromString(uuid));
                });
            } catch (IOException ignored) {
            }

            return uuids;
        });
    }

    public CompletableFuture<Void> deleteNetworkFile(UUID networkUUID) {
        return CompletableFuture.supplyAsync(() -> {
            ReentrantReadWriteLock lock = locks.computeIfAbsent(networkUUID, uuid -> new ReentrantReadWriteLock());
            Lock writeLock = lock.writeLock();
            writeLock.lock();

            try {
                Path path = networkDataPath.resolve(networkUUID.toString());
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    plugin.getComponentLogger().error("Could not delete network file {}", networkUUID, e);
                }
            } finally {
                writeLock.unlock();
                locks.remove(networkUUID);
            }
            return null;
        });
    }

    public CompletableFuture<Void> deserializeNetwork(EnergyNetwork network) {
        UUID networkUUID = network.getNetworkID();
        Path path = networkDataPath.resolve(networkUUID.toString());
        Path resourcePath = networkResourcePath.resolve(networkUUID.toString());

        return CompletableFuture.supplyAsync(() -> {
            ReentrantReadWriteLock lock = locks.computeIfAbsent(networkUUID, uuid -> new ReentrantReadWriteLock());
            Lock writeLock = lock.writeLock();
            writeLock.lock();

            try {
                GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
                        .path(path)
                        .build();

                ConfigurationNode node = loader.load();
                node.set(COMPONENTS_CONNECTIONS_MAP.getType(), network.getComponentsConnections());

                loader.save(node);
            } catch (IOException | IllegalArgumentException e) {
                plugin.getComponentLogger().error("Failed to deserializeNetwork energy network: {}", networkUUID, e);
            } finally {
                writeLock.unlock();
                locks.remove(networkUUID);
            }

            return null;
        });
    }

    public CompletableFuture<Optional<EnergyNetwork>> serializeNetwork(UUID networkUUID) {
        Path path = networkDataPath.resolve(networkUUID.toString());

        return CompletableFuture.supplyAsync(() -> {
            ReentrantReadWriteLock lock = locks.computeIfAbsent(networkUUID, uuid -> new ReentrantReadWriteLock());
            Lock readLock = lock.readLock();
            readLock.lock();

            try {
                ConfigurationNode node;
                GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
                        .path(path)
                        .build();

                try {
                    node = loader.load();
                } catch (ConfigurateException e) {
                    plugin.getComponentLogger().error("Failed to serializeNetwork energy network: {}", networkUUID, e);
                    return Optional.empty();
                }

                try {
                    Object object = node.get(COMPONENTS_CONNECTIONS_MAP.getType());
                    if (!(object instanceof Map<?, ?> map)) {
                        plugin.getComponentLogger().error("Failed to serializeNetwork energy network map: {}", networkUUID);
                        return Optional.empty();
                    }

                    return Optional.of(new EnergyNetwork(plugin, networkUUID, getComponentsConnections(map)));
                } catch (SerializationException e) {
                    plugin.getComponentLogger().error("Failed to serializeNetwork energy network: {}", networkUUID, e);
                    return Optional.empty();
                }
            } finally {
                readLock.unlock();
                locks.remove(networkUUID);
            }
        });
    }

    private ConcurrentHashMap<UUID, List<UUID>> getComponentsConnections(Map<?, ?> map) {
        ConcurrentHashMap<UUID, List<UUID>> componentsConnections = new ConcurrentHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof UUID componentUUID)) {
                continue;
            }
            if (!(entry.getValue() instanceof List<?> connectedComponents)) {
                continue;
            }
            List<UUID> connectedComponentsList = new ArrayList<>();
            for (Object connectedComponent : connectedComponents) {
                if (!(connectedComponent instanceof UUID connectedComponentUUID)) {
                    continue;
                }
                connectedComponentsList.add(connectedComponentUUID);
            }

            componentsConnections.put(componentUUID, connectedComponentsList);
        }
        return componentsConnections;
    }
}
