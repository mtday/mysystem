package mysystem.shell.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable object used to lookup registration information for user input.
 */
public class RegistrationLookup implements Comparable<RegistrationLookup>, Serializable {
    private final static long serialVersionUID = 1L;

    private final TokenizedUserInput userInput;

    /**
     * @param userInput the {@link TokenizedUserInput} for which a command registration should be found
     */
    private RegistrationLookup(final TokenizedUserInput userInput) {
        this.userInput = userInput;
    }

    /**
     * @return the {@link UserInput} for which a command registration should be found
     */
    public TokenizedUserInput getUserInput() {
        return this.userInput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("userInput", getUserInput());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final RegistrationLookup other) {
        if (other == null) {
            return 1;
        }

        return getUserInput().compareTo(other.getUserInput());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof RegistrationLookup) && compareTo((RegistrationLookup) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getUserInput().hashCode();
    }

    /**
     * Used to create {@link RegistrationLookup} objects.
     */
    public static class Builder {
        private final TokenizedUserInput userInput;

        /**
         * @param userInput the tokenized user input to include in the request
         */
        public Builder(final TokenizedUserInput userInput) {
            this.userInput = Objects.requireNonNull(userInput);
        }

        /**
         * @return a new {@link RegistrationLookup} instance based on this builder
         */
        public RegistrationLookup build() {
            return new RegistrationLookup(this.userInput);
        }
    }
}
