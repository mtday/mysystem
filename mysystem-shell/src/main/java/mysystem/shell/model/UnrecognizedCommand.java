package mysystem.shell.model;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable object used to represent an unrecognized command provided by the user.
 */
public class UnrecognizedCommand implements Model, Comparable<UnrecognizedCommand> {
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
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.add("userInput", getUserInput().toJson());
        return json;
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
    public static class Builder implements ModelBuilder<UnrecognizedCommand> {
        private Optional<TokenizedUserInput> userInput = Optional.empty();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param userInput the user input to include in the request
         */
        public Builder(final TokenizedUserInput userInput) {
            setUserInput(Objects.requireNonNull(userInput));
        }

        /**
         * @param userInput the user input to include in the request
         */
        public Builder setUserInput(final TokenizedUserInput userInput) {
            this.userInput = Optional.of(Objects.requireNonNull(userInput));
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final JsonObject json) {
            if (Objects.requireNonNull(json).has("userInput")) {
                setUserInput(new TokenizedUserInput.Builder().fromJson(json.getAsJsonObject("userInput")).build());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UnrecognizedCommand build() {
            if (!this.userInput.isPresent()) {
                throw new IllegalStateException("Tokenized user input is required to build an unrecognized command");
            }

            return new UnrecognizedCommand(this.userInput.get());
        }
    }
}
