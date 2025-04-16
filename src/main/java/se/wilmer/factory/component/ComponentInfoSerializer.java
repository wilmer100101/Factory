package se.wilmer.factory.component;

import org.spongepowered.configurate.ConfigurationNode;

public class ComponentInfoSerializer {
    private final String title;
    private final boolean useEnergy;

    public ComponentInfoSerializer(Builder builder) {
        this.title = builder.title;
        this.useEnergy = builder.useEnergy;
    }

    public String getTitle() {
        return title;
    }

    public boolean isUseEnergy() {
        return useEnergy;
    }

    public static class Builder {
        private final ConfigurationNode node;
        private String title = null;
        private boolean useEnergy = false;

        public Builder(ConfigurationNode node) {
            this.node = node;
        }

        public ComponentInfoSerializer.Builder energy(boolean hasEnergy) {
            this.useEnergy = hasEnergy;
            return this;
        }

        public ComponentInfoSerializer deserialize() {
            title = node.node("title").getString();
            if (title == null || title.isEmpty()) {
                title = null;
            }

            if (!node.node("display-energy").getBoolean()) {
                useEnergy = false;
            }
            return new ComponentInfoSerializer(this);
        }
    }
}
