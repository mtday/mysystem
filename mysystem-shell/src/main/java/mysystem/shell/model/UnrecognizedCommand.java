package mysystem.shell.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable object used to represent an unrecognized command provided by the user.
 */
public class UnrecognizedCommand implements Comparable<UnrecognizedCommand>, Serializable {
    private final static long serialVersionUID = 1L;

    private final TokenizedUserInput userInput;

    /**
     * @param userInput the {@link TokenizedUserInput} representing the missing command
     */
    private UnrecognizedCommand(final TokenizedUserInput userInput) {
        this.userInput = userInput;
    }

    /**
     * @return the {@link TokenizedUserInput} representing the missing command
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
    public int compareTo(final UnrecognizedCommand other) {
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
        return (other instanceof UnrecognizedCommand) && compareTo((UnrecognizedCommand) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getUserInput().hashCode();
    }

    /**
     * Used to create {@link UnrecognizedCommand} objects.
     */
    public static class Builder {
        private final TokenizedUserInput userInput;

        /**
         * @param userInput the user input to include in the request
         */
        public Builder(final TokenizedUserInput userInput) {
            this.userInput = Objects.requireNonNull(userInput);
        }

        /**
         * @return a new {@link UnrecognizedCommand} instance based on this builder
         */
        public UnrecognizedCommand build() {
            return new UnrecognizedCommand(this.userInput);
        }
    }
}
