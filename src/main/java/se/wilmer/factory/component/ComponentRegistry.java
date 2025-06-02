package se.wilmer.factory.component;

import se.wilmer.factory.Factory;
import se.wilmer.factory.component.components.blockbreaker.BlockBreakerConfig;
import se.wilmer.factory.component.components.blockplacer.BlockPlacerConfig;
import se.wilmer.factory.component.components.fuelburner.FuelBurnerConfig;
import se.wilmer.factory.component.components.milker.MilkerConfig;
import se.wilmer.factory.component.components.relay.RelayConfig;
import se.wilmer.factory.component.components.solarpanel.SolarPanelConfig;
import se.wilmer.factory.component.components.treecutter.TreeCutterConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

//FIXME: Make it possible to get a specific component, or does that even have a use cause?
public class ComponentRegistry {
    private final Factory plugin;
    private final List<Component> components = new ArrayList<>();

    public ComponentRegistry(Factory plugin) {
        this.plugin = plugin;
    }

    public void register() {
        components.addAll(new TreeCutterConfig(plugin, "treecutter").load());
        components.addAll(new BlockBreakerConfig(plugin, "blockbreaker").load());
        components.addAll(new BlockPlacerConfig(plugin, "blockplacer").load());
        components.addAll(new SolarPanelConfig(plugin, "solarpanel").load());
        components.addAll(new MilkerConfig(plugin, "milker").load());
        components.addAll(new FuelBurnerConfig(plugin, "fuelburner").load());
        components.addAll(new RelayConfig(plugin, "relay").load());
    }

    public List<Component> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public Optional<Component> getComponentByID(String id) {
        return components.stream().filter(component -> component.getId().equals(id)).findAny();
    }
}
