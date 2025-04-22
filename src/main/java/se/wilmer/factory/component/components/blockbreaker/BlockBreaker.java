package se.wilmer.factory.component.components.blockbreaker;

import com.jeff_media.customblockdata.CustomBlockData;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import se.wilmer.factory.Factory;
import se.wilmer.factory.component.Component;
import se.wilmer.factory.component.ComponentEntity;
import se.wilmer.factory.component.ComponentInfoSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BlockBreaker extends Component {
    private final long maxEnergyConsumption;
    private final Map<Material, Long> materialBreakDurationTicks;

    public BlockBreaker(Factory plugin, String id, ComponentInfoSerializer componentInfoSerializer, long maxEnergyConsumption, Map<Material, Long> materialBreakDurationTicks) {
        super(plugin, id, componentInfoSerializer);

        this.maxEnergyConsumption = maxEnergyConsumption;
        this.materialBreakDurationTicks = materialBreakDurationTicks;
    }

    @Override
    public ComponentEntity<BlockBreaker> createEntity(Block block) {
        return new BlockBreakerEntity(plugin, this, new BlockBreakerData(plugin, block), block);
    }

    public long getMaxEnergyConsumption() {
        return maxEnergyConsumption;
    }

    /**
     * Retrieves the breaking duration in ticks for a given material.
     *
     * @param material The material to get the duration for.
     * @return Duration in ticks, or empty if the material was not found.
     */
    public Optional<Long> getMaterialBreakingDuration(Material material) {
        return Optional.ofNullable(materialBreakDurationTicks.get(material));
    }

}
