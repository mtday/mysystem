package mysystem.shell.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable representation of unprocessed user input received from the shell interface.
 */
public class UserInput implements Comparable<UserInput>, Serializable {
    private final static long serialVersionUID = 1L;
    private final String input;

    /**
     * @param input the unprocessed user-provided input from the shell interface
     */
    private UserInput(final String input) {
        this.input = input;
    }

    /**
     * @return the unprocessed user-provided input from the shell interface
     */
    public String getInput() {
        return this.input;
    }

    /**
     * @return whether the user input is empty
     */
    public boolean isEmpty() {
        return getInput().isEmpty();
    }

    /**
     * @return whether the user input is a comment
     */
    public boolean isComment() {
        return getInput().startsWith("#");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getInput();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final UserInput other) {
        if (other == null) {
            return 1;
        }

        return getInput().compareTo(other.getInput());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof UserInput) && compareTo((UserInput) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getInput().hashCode();
    }

    /**
     * Used to create {@link UserInput} instances.
     */
    public static class Builder {
        private final String input;

        /**
         * Copy constructor.
         *
         * @param other the {@link UserInput} to duplicate
         */
        public Builder(final UserInput other) {
            this.input = Objects.requireNonNull(other).getInput();
        }

        /**
         * @param input the unprocessed user input provided by the user from the shell interface
         */
        public Builder(final String input) {
            this.input = StringUtils.trimToEmpty(Objects.requireNonNull(input));
        }

        /**
         * @return the {@link UserInput} defined in this builder
         */
        public UserInput build() {
            return new UserInput(this.input);
        }
    }
}
