package mysystem.shell.model;

import com.google.gson.JsonObject;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;

/**
 * An immutable object used to indicate that the shell process should be terminated.
 */
public class Terminate implements Model, Comparable<Terminate> {
    private final static String SERIALIZATION_MANIFEST = Terminate.class.getSimpleName();

    /**
     * Default constructor.
     */
    private Terminate() {
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
    public int compareTo(final Terminate other) {
        return (other == null) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof Terminate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    /**
     * Used to create {@link Terminate} objects.
     */
    public static class Builder implements ModelBuilder<Terminate> {
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
        public Terminate build() {
            return new Terminate();
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
