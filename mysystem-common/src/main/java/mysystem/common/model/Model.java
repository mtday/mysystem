package mysystem.common.model;

import com.google.gson.JsonObject;

/**
 * This interface defines the required functionality for model objects in this system.
 */
public interface Model {
    /**
     * @return the serialization manifest used to uniquely distinguish this model object
     */
    String getSerializationManifest();

    /**
     * @return a JSON representation of the model object used for serialization
     */
    JsonObject toJson();
}
