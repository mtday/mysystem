package mysystem.shell.model;

import com.google.gson.JsonObject;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;

/**
 * An immutable object used to indicate that the shell process should be terminated.
 */
public class Terminate implements Model, Comparable<Terminate> {
    /**
     * Default constructor.
     */
    private Terminate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        return new JsonObject();
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
        public Builder fromJson(final JsonObject json) {
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
    }
}
