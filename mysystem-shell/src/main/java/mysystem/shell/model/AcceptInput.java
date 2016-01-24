package mysystem.shell.model;

import com.google.gson.JsonObject;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;

/**
 * An immutable object used to indicate that more input can be accepted from the shell console.
 */
public class AcceptInput implements Model, Comparable<AcceptInput> {
    /**
     * Default constructor.
     */
    private AcceptInput() {
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
    public int compareTo(final AcceptInput other) {
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
        public Builder fromJson(final JsonObject json) {
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
    }
}
