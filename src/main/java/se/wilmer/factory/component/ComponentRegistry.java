package se.wilmer.factory.component;

import se.wilmer.factory.Factory;
import se.wilmer.factory.component.components.blockbreaker.BlockBreaker;
import se.wilmer.factory.component.components.blockplacer.BlockPlacer;
import se.wilmer.factory.component.components.milker.Milker;
import se.wilmer.factory.component.components.solarpanel.SolarPanel;
import se.wilmer.factory.component.components.treecutter.TreeCutter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentRegistry {
    private final Factory plugin;
    private final List<Component> components = new ArrayList<>();

    public ComponentRegistry(Factory plugin) {
        this.plugin = plugin;
    }

    public void register() {
        components.add(new BlockBreaker(plugin));
        components.add(new BlockPlacer(plugin));
        components.add(new TreeCutter(plugin));
        components.add(new SolarPanel(plugin));
        components.add(new Milker(plugin));
    }

    public List<Component> getComponents() {
        return Collections.unmodifiableList(components);
    }
}
