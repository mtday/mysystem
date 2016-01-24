package mysystem.common.serialization;

import mysystem.common.model.Company;
import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manage the mapping of model objects to their string-based manifest values during serialization and deserialization.
 */
public class ManifestMapping {
    private final Map<Class<?>, String> manifestMap = new HashMap<>();
    private final Map<String, ModelBuilderFactory> builderMap = new HashMap<>();

    /**
     * Package private so this class is only available to the {@link ModelSerialization} implementation.
     */
    public ManifestMapping() {
        this.manifestMap.put(Company.class, Company.class.getSimpleName());
        this.builderMap.put(Company.class.getSimpleName(), Company.Builder::new);
    }

    /**
     * @param clazz the class to be serialized for which a suitable manifest value is needed
     * @return the matching manifest, possibly empty if the class is not recognized
     */
    public Optional<String> getManifest(final Class<?> clazz) {
        return Optional.ofNullable(this.manifestMap.get(Objects.requireNonNull(clazz)));
    }

    /**
     * @param manifest the string manifest value of a serialized object for which a builder is needed
     * @return the matching builder used to deserialize the model object, possibly empty if the manifest is not
     * recognized
     */
    public Optional<ModelBuilder<? extends Model>> getBuilder(final String manifest) {
        final Optional<ModelBuilderFactory> factory =
                Optional.ofNullable(this.builderMap.get(Objects.requireNonNull(manifest)));
        if (factory.isPresent()) {
            return Optional.of(factory.get().getBuilder());
        }
        return Optional.empty();
    }

    /**
     * Defines the interface used to create builder objects on the fly during deserialization.
     */
    private interface ModelBuilderFactory {
        ModelBuilder<? extends Model> getBuilder();
    }
}
