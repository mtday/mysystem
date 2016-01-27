package mysystem.tomcat.model;

import com.google.gson.JsonObject;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;

import javax.annotation.Nullable;

/**
 * An immutable object used to tell the {@link mysystem.tomcat.actor.TomcatManager} to start.
 */
public class TomcatStart implements Model, Comparable<TomcatStart> {
    private final static String SERIALIZATION_MANIFEST = TomcatStart.class.getSimpleName();

    /**
     * Default constructor.
     */
    private TomcatStart() {
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
    public int compareTo(@Nullable final TomcatStart other) {
        return (other == null) ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof TomcatStart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    /**
     * Used to create {@link TomcatStart} objects.
     */
    public static class Builder implements ModelBuilder<TomcatStart> {
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
        public TomcatStart build() {
            return new TomcatStart();
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
