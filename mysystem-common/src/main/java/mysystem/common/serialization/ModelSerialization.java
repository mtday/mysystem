package mysystem.common.serialization;

import com.google.common.base.Preconditions;
import com.google.gson.JsonParser;

import akka.serialization.SerializerWithStringManifest;
import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * Used to perform JSON-based serialization.
 */
public class ModelSerialization extends SerializerWithStringManifest {
    private final ManifestMapping manifestMapping = new ManifestMapping();

    /**
     * {@inheritDoc}
     */
    @Override
    public int identifier() {
        return 48151623;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String manifest(final Object object) {
        Objects.requireNonNull(object);
        Preconditions.checkArgument(object instanceof Model, "Can only serialize Model objects");
        final Model model = (Model) object;
        final Optional<String> manifest = this.manifestMapping.getManifest(model.getClass());
        if (manifest.isPresent()) {
            return manifest.get();
        }
        throw new RuntimeException("Object type is not recognized: " + model.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromBinary(final byte[] bytes, final String manifest) {
        final Optional<ModelBuilder<?>> builder = this.manifestMapping.getBuilder(Objects.requireNonNull(manifest));
        if (builder.isPresent()) {
            final String json = new String(bytes, StandardCharsets.UTF_8);
            builder.get().fromJson(this.manifestMapping, new JsonParser().parse(json).getAsJsonObject());
            return builder.get().build();
        }
        throw new RuntimeException("Serialization manifest type is not recognized: " + manifest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] toBinary(final Object object) {
        Objects.requireNonNull(object);
        Preconditions.checkArgument(object instanceof Model, "Can only serialize Model objects");
        return ((Model) object).toJson().toString().getBytes(StandardCharsets.UTF_8);
    }
}
