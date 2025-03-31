package se.wilmer.factory.component.wire;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.bukkit.persistence.PersistentDataType;


public class WireDataType implements PersistentDataType<byte[], Wire> {

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<Wire> getComplexType() {
        return Wire.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(Wire complex, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES * 2 * 2);
        bb.putLong(complex.firstEntityUUID().getMostSignificantBits());
        bb.putLong(complex.firstEntityUUID().getLeastSignificantBits());
        bb.putLong(complex.secondEntityUUID().getMostSignificantBits());
        bb.putLong(complex.secondEntityUUID().getLeastSignificantBits());
        return bb.array();
    }

    @Override
    public @NotNull Wire fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(primitive);
        UUID firstEntityUUID = new UUID(bb.getLong(), bb.getLong());
        UUID secondEntityUUID = new UUID(bb.getLong(), bb.getLong());
        return new Wire(firstEntityUUID, secondEntityUUID);
    }
}
