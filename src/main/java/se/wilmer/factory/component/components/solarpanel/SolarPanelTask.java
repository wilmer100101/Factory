package se.wilmer.factory.component.components.solarpanel;

import org.bukkit.Location;
import org.bukkit.block.Block;

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
