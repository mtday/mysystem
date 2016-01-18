package mysystem.shell.model;

import java.io.Serializable;

/**
 * An immutable object used to request registration information from a shell command.
 */
public class RegistrationRequest implements Comparable<RegistrationRequest>, Serializable {
    private final static long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    private RegistrationRequest() {
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
    public int compareTo(final RegistrationRequest other) {
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
    public static class Builder {
        /**
         * @return a new {@link RegistrationRequest} instance based on this builder
         */
        public RegistrationRequest build() {
            return new RegistrationRequest();
        }
    }
}
