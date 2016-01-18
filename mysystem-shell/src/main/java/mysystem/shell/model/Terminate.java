package mysystem.shell.model;

import java.io.Serializable;

/**
 * An immutable object used to indicate that the shell process should be terminated.
 */
public class Terminate implements Comparable<Terminate>, Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    private Terminate() {
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
    public static class Builder {
        /**
         * @return a new {@link Terminate} instance based on this builder
         */
        public Terminate build() {
            return new Terminate();
        }
    }
}
