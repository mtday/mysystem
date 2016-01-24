package mysystem.shell.model;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable representation of unprocessed user input received from the shell interface.
 */
public class UserInput implements Model, Comparable<UserInput> {
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
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("input", getInput());
        return json;
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
    public static class Builder implements ModelBuilder<UserInput> {
        private Optional<String> input = Optional.empty();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param other the {@link UserInput} to duplicate
         */
        public Builder(final UserInput other) {
            setInput(Objects.requireNonNull(other).getInput());
        }

        /**
         * @param input the unprocessed user input provided by the user from the shell interface
         */
        public Builder(final String input) {
            setInput(StringUtils.trimToEmpty(Objects.requireNonNull(input)));
        }

        /**
         * @param input the unprocessed user input provided by the user from the shell interface
         */
        public Builder setInput(final String input) {
            this.input = Optional.of(Objects.requireNonNull(input));
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final JsonObject json) {
            if (Objects.requireNonNull(json).has("input")) {
                setInput(json.getAsJsonPrimitive("input").getAsString());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UserInput build() {
            if (!this.input.isPresent()) {
                throw new IllegalStateException("Input is required");
            }

            return new UserInput(this.input.get());
        }
    }
}
