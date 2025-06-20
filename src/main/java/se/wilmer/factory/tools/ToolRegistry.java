package se.wilmer.factory.tools;

import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.tools.connector.Connector;
import se.wilmer.factory.tools.multimeter.Multimeter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


//FIXME: Make it possible to get a specific tool.
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

        Connector connector = new Connector(plugin);
        connector.load();
        tools.add(connector);
    }

    public List<Tool> getTools() {
        return Collections.unmodifiableList(tools);
    }
}

