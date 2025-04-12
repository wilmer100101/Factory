package se.wilmer.factory.tools;

import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.tools.multimeter.Multimeter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ToolRegistry {
    private final Factory plugin;
    private final List<Tool> tools = new ArrayList<>();

    public ToolRegistry(Factory plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Multimeter multimeter = new Multimeter(plugin);
        multimeter.load();
        tools.add(multimeter);
    }

    public List<Tool> getTools() {
        return Collections.unmodifiableList(tools);
    }
}

