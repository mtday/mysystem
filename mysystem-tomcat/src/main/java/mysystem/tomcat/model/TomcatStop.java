package mysystem.tomcat.model;

import com.google.gson.JsonObject;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;

import javax.annotation.Nullable;

/**
 * An immutable object used to tell the {@link mysystem.tomcat.actor.TomcatManager} to stop.
 */
public class TomcatStop implements Model, Comparable<TomcatStop> {
    private final static String SERIALIZATION_MANIFEST = TomcatStop.class.getSimpleName();

    /**
     * Default constructor.
     */
    private TomcatStop() {
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
    public int compareTo(@Nullable final TomcatStop other) {
        return (other == null) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof TomcatStop;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    /**
     * Used to create {@link TomcatStop} objects.
     */
    public static class Builder implements ModelBuilder<TomcatStop> {
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
        public TomcatStop build() {
            return new TomcatStop();
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
