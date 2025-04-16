package se.wilmer.factory.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.util.Optional;
import java.util.UUID;

public class ComponentInfo {
    private final ComponentEntity<?> componentEntity;
    private final boolean useEnergy;
    private final String title;
    private Long maxEnergy = null;
    private Long currentEnergy = null;
    private World world;

    public ComponentInfo(ComponentInfoSerializer serializer, ComponentEntity<?> componentEntity) {
        this.title = serializer.getTitle();
        this.useEnergy = serializer.isUseEnergy();
        this.componentEntity = componentEntity;
    }

    public void spawn(Location location) {
        world = location.getWorld();

        ComponentData data = componentEntity.getData();
        if (data.getInformationEntityUUID().isEmpty()) {
            data.setInformationEntityUUID(world.spawn(componentEntity.getOffsetLocation().clone().add(0, 0.2, 0), TextDisplay.class, textDisplay -> {
                textDisplay.setBillboard(Display.Billboard.CENTER);
            }).getUniqueId());

            updateText();
        }
    }

    public void despawn(World world) {
        ComponentData data = componentEntity.getData();
        data.getInformationEntityUUID().ifPresent(uuid -> {
            Entity entity = world.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
            data.setInformationEntityUUID(null);
        });
    }

    public void updateEnergy(long currentEnergy, long maxEnergy) {
        this.currentEnergy = currentEnergy;
        this.maxEnergy = maxEnergy;

        updateText();
    }

    public void updateLocation() {
        componentEntity.getData().getInformationEntityUUID().ifPresent(uuid -> {
            if (world == null) {
                return;
            }
            Entity entity = world.getEntity(uuid);
            if (entity != null) {
                entity.teleport(componentEntity.getOffsetLocation());
            }
        });
    }

    private void updateText() {
        Component component = Component.text("");

        if (useEnergy && currentEnergy != null && maxEnergy != null) {
            component = component.append(Component.text("\u26A1").color(NamedTextColor.YELLOW))
                    .appendSpace()
                    .append(Component.text(currentEnergy).color(NamedTextColor.WHITE))
                    .append(Component.text("/").color(NamedTextColor.GRAY))
                    .append(Component.text(maxEnergy).color(NamedTextColor.WHITE));
        }
        if (title != null) {
            component = component.appendNewline().append(Component.text(title).color(NamedTextColor.GOLD));
        }

        Optional<UUID> entityUUID = componentEntity.getData().getInformationEntityUUID();
        if (entityUUID.isPresent() && world.getEntity(entityUUID.get()) instanceof TextDisplay textDisplay) {
            textDisplay.text(component);
        }
    }
}
