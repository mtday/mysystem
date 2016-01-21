package mysystem.shell.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.util.OptionalComparator;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable object used to represent invalid input provided by the user.
 */
public class InvalidInput implements Comparable<InvalidInput>, Serializable {
    private final static long serialVersionUID = 1L;

    private final UserInput userInput;
    private final String error;
    private final Optional<Integer> location;

    /**
     * @param userInput the {@link UserInput} representing the missing command
     * @param error     the error message to display back to the user
     * @param location  the location in the input where the error occurred
     */
    private InvalidInput(final UserInput userInput, final String error, final Optional<Integer> location) {
        this.userInput = userInput;
        this.error = error;
        this.location = location;
    }

    /**
     * @return the {@link UserInput} representing the missing command
     */
    public UserInput getUserInput() {
        return this.userInput;
    }

    /**
     * @return the error message describing the input problem
     */
    public String getError() {
        return this.error;
    }

    /**
     * @return the location in the user input where the error occurred.
     */
    public Optional<Integer> getLocation() {
        return this.location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("userInput", getUserInput());
        str.append("error", getError());
        str.append("location", getLocation());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final InvalidInput other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getUserInput(), other.getUserInput());
        cmp.append(getError(), other.getError());
        cmp.append(getLocation(), other.getLocation(), new OptionalComparator());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof InvalidInput) && compareTo((InvalidInput) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getUserInput());
        hash.append(getError());
        hash.append(getLocation());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link InvalidInput} objects.
     */
    public static class Builder {
        private final UserInput userInput;
        private final String error;
        private final Optional<Integer> location;

        /**
         * @param userInput      the user input to include in the request
         * @param parseException the parse exception caused by the invalid input
         */
        public Builder(final UserInput userInput, final ParseException parseException) {
            this.userInput = Objects.requireNonNull(userInput);
            this.error = Objects.requireNonNull(parseException).getMessage();
            if (parseException.getErrorOffset() >= 0) {
                this.location = Optional.of(parseException.getErrorOffset());
            } else {
                this.location = Optional.empty();
            }
        }

        /**
         * @param userInput      the user input to include in the request
         * @param parseException the parse exception caused by the invalid input
         */
        public Builder(final TokenizedUserInput userInput, final org.apache.commons.cli.ParseException parseException) {
            this.userInput = Objects.requireNonNull(userInput).getUserInput();
            this.error = Objects.requireNonNull(parseException).getMessage();
            this.location = Optional.empty();
        }

        /**
         * @param invalidInput the {@link InvalidInput} to duplicate
         */
        public Builder(final InvalidInput invalidInput) {
            this.userInput = Objects.requireNonNull(invalidInput).getUserInput();
            this.error = invalidInput.getError();
            this.location = invalidInput.getLocation();
        }

        /**
         * @return a new {@link InvalidInput} instance based on this builder
         */
        public InvalidInput build() {
            return new InvalidInput(this.userInput, this.error, this.location);
        }
    }
}
