package mysystem.shell.model;

import com.google.gson.JsonObject;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;

import javax.annotation.Nullable;

/**
 * An immutable object used to request registration information from a shell command.
 */
public class RegistrationRequest implements Model, Comparable<RegistrationRequest> {
    private final static String SERIALIZATION_MANIFEST = RegistrationRequest.class.getSimpleName();

    /**
     * Default constructor.
     */
    private RegistrationRequest() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("manifest", getSerializationManifest());
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final RegistrationRequest other) {
        return (other == null) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof RegistrationRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    /**
     * Used to create {@link RegistrationRequest} objects.
     */
    public static class Builder implements ModelBuilder<RegistrationRequest> {
        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            // No need to do anything with the json object.
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public RegistrationRequest build() {
            return new RegistrationRequest();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSerializationManifest() {
            return SERIALIZATION_MANIFEST;
        }
    }
}
