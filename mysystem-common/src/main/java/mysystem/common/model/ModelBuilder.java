package mysystem.common.model;

import com.google.gson.JsonObject;

import mysystem.common.serialization.ManifestMapping;

/**
 * This interface is a marker for model builder objects in this system.
 */
public interface ModelBuilder<M extends Model> {
    /**
     * @return the serialization manifest used to distinguish the model objects created by this builder
     */
    String getSerializationManifest();

    /**
     * @param mapping the {@link ManifestMapping} used to dynamically determine model classes
     * @param json the {@link JsonObject} from which the model object will be built
     * @return the builder itself for fluent-style usage
     */
    ModelBuilder<M> fromJson(ManifestMapping mapping, JsonObject json);

    /**
     * @return the model object represented by the current state of the builder
     */
    M build();
}
