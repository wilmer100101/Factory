package se.wilmer.factory.energy;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.util.Types;
import se.wilmer.factory.Factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class EnergyNetworkSerializer {
    private static final TypeToken<Map<UUID, List<UUID>>> COMPONENTS_CONNECTIONS_MAP = Types.makeMap(new TypeToken<>() {}, Types.makeList(UUID.class));

    private final Scheduler scheduler = Scheduler.systemScheduler();
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private final LoadingCache<UUID, ReentrantLock> ioLocks;

    private final Factory plugin;
    private final Path networkDataPath;

    public EnergyNetworkSerializer(Factory plugin) {
        this.plugin = plugin;

        networkDataPath = plugin.getDataFolder().toPath().resolve("energy_networks");

        ioLocks = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .scheduler(scheduler)
                .build(key -> new ReentrantLock(true));
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
        }, executorService);
    }

    public CompletableFuture<Void> deleteNetworkFile(UUID networkUUID) {
        return CompletableFuture.supplyAsync(() -> {
            ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(networkUUID));
            lock.lock();

            try {
                Path path = networkDataPath.resolve(networkUUID.toString());
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    plugin.getComponentLogger().error("Could not delete network file {}", networkUUID, e);
                }
            } finally {
                lock.unlock();
            }
            return null;
        }, executorService);
    }

    public CompletableFuture<Void> deserializeNetwork(EnergyNetwork network) {
        UUID networkUUID = network.getNetworkID();
        Path path = networkDataPath.resolve(networkUUID.toString());

        return CompletableFuture.supplyAsync(() -> {
            ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(networkUUID));
            lock.lock();

            try {
                GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
                        .path(path)
                        .build();

                ConfigurationNode node = loader.load();
                node.set(COMPONENTS_CONNECTIONS_MAP.getType(), network.getComponentsConnections());

                loader.save(node);
            } catch (Exception e) {
                plugin.getComponentLogger().error("Failed to deserializeNetwork energy network: {}", networkUUID, e);
            } finally {
                lock.unlock();
            }

            return null;
        }, executorService);
    }

    public CompletableFuture<Optional<EnergyNetwork>> serializeNetwork(UUID networkUUID) {
        Path path = networkDataPath.resolve(networkUUID.toString());

        return CompletableFuture.supplyAsync(() -> {
            ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(networkUUID));
            lock.lock();

            try {
                GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
                        .path(path)
                        .build();

                ConfigurationNode node = loader.load();

                Map<UUID, List<UUID>> componentsConnections = node.get(COMPONENTS_CONNECTIONS_MAP);
                if (componentsConnections == null) {
                    plugin.getComponentLogger().error("Failed to serializeNetwork energy network, could not get components connection map: {}", networkUUID);
                    return Optional.empty();
                }
                return Optional.of(new EnergyNetwork(plugin, networkUUID, new ConcurrentHashMap<>(componentsConnections)));
            } catch (IOException e) {
                plugin.getComponentLogger().error("Failed to serializeNetwork energy network: {}", networkUUID, e);
                return Optional.empty();
            } finally {
                lock.unlock();
            }
        }, executorService);
    }
}
