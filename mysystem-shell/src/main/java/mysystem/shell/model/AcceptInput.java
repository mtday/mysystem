package mysystem.shell.model;

import java.io.Serializable;

/**
 * An immutable object used to indicate that more input can be accepted from the shell console.
 */
public class AcceptInput implements Comparable<AcceptInput>, Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    private AcceptInput() {
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
    public static class Builder {
        /**
         * @return a new {@link AcceptInput} instance based on this builder
         */
        public AcceptInput build() {
            return new AcceptInput();
        }
    }
}
