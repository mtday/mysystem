package mysystem.common.model;

import com.google.gson.JsonObject;

/**
 * This interface is a marker for model builder objects in this system.
 */
public interface ModelBuilder<M extends Model> {
    /**
     * @param json the {@link JsonObject} from which the model object will be built
     * @return the builder itself for fluent-style usage
     */
    ModelBuilder<M> fromJson(JsonObject json);

    /**
     * @return the model object represented by the current state of the builder
     */
    M build();
}
