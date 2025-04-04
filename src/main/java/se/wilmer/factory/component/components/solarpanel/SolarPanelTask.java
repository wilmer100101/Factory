package se.wilmer.factory.component.components.solarpanel;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.DaylightDetector;

import java.util.Optional;


public class SolarPanelTask implements Runnable {
    private final SolarPanelEntity solarPanelEntity;

    public SolarPanelTask(SolarPanelEntity solarPanelEntity) {
        this.solarPanelEntity = solarPanelEntity;
    }

    @Override
    public void run() {
    }

}
