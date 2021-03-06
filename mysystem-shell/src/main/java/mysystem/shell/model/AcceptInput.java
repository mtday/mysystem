package mysystem.shell.model;

import com.google.gson.JsonObject;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;

import javax.annotation.Nullable;

/**
 * An immutable object used to indicate that more input can be accepted from the shell console.
 */
public class AcceptInput implements Model, Comparable<AcceptInput> {
    private final static String SERIALIZATION_MANIFEST = AcceptInput.class.getSimpleName();

    /**
     * Default constructor.
     */
    private AcceptInput() {
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
    public int compareTo(@Nullable final AcceptInput other) {
        return (other == null) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof AcceptInput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    /**
     * Used to create {@link AcceptInput} objects.
     */
    public static class Builder implements ModelBuilder<AcceptInput> {
        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            // No need to actually parse anything from the json object.
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AcceptInput build() {
            return new AcceptInput();
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
