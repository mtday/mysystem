package mysystem.shell.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;
import mysystem.common.util.OptionalComparator;

import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * An immutable object used to represent invalid input provided by the user.
 */
public class InvalidInput implements Model, Comparable<InvalidInput> {
    private final static String SERIALIZATION_MANIFEST = InvalidInput.class.getSimpleName();

    private final UserInput userInput;
    private final String error;
    private final Optional<Integer> location;

    /**
     * @param userInput the {@link UserInput} representing the missing command
     * @param error the error message to display back to the user
     * @param location the location in the input where the error occurred
     */
    private InvalidInput(final UserInput userInput, final String error, final Optional<Integer> location) {
        this.userInput = userInput;
        this.error = error;
        this.location = location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
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
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.add("userInput", getUserInput().toJson());
        json.addProperty("error", getError());
        if (getLocation().isPresent()) {
            json.addProperty("location", getLocation().get());
        }
        json.addProperty("manifest", getSerializationManifest());
        return json;
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
    public int compareTo(@Nullable final InvalidInput other) {
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
    public static class Builder implements ModelBuilder<InvalidInput> {
        private Optional<UserInput> userInput = Optional.empty();
        private Optional<String> error = Optional.empty();
        private Optional<Integer> location = Optional.empty();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param userInput the user input to include in the request
         * @param parseException the parse exception caused by the invalid input
         */
        public Builder(final UserInput userInput, final ParseException parseException) {
            setUserInput(userInput);
            //noinspection ThrowableResultOfMethodCallIgnored
            setError(Objects.requireNonNull(parseException).getMessage());
            if (parseException.getErrorOffset() >= 0) {
                setLocation(parseException.getErrorOffset());
            }
        }

        /**
         * @param userInput the user input to include in the request
         * @param parseException the parse exception caused by the invalid input
         */
        public Builder(final TokenizedUserInput userInput, final org.apache.commons.cli.ParseException parseException) {
            setUserInput(Objects.requireNonNull(userInput).getUserInput());
            //noinspection ThrowableResultOfMethodCallIgnored
            setError(Objects.requireNonNull(parseException).getMessage());
        }

        /**
         * @param invalidInput the {@link InvalidInput} to duplicate
         */
        public Builder(final InvalidInput invalidInput) {
            setUserInput(Objects.requireNonNull(invalidInput).getUserInput());
            setError(invalidInput.getError());
            if (invalidInput.getLocation().isPresent()) {
                setLocation(invalidInput.getLocation().get());
            }
        }

        /**
         * @param userInput the user input to include in the request
         * @return {@code this} for fluent-style usage
         */
        public Builder setUserInput(final UserInput userInput) {
            this.userInput = Optional.of(Objects.requireNonNull(userInput));
            return this;
        }

        /**
         * @param userInput the user input to include in the request
         * @return {@code this} for fluent-style usage
         */
        public Builder setUserInput(final TokenizedUserInput userInput) {
            this.userInput = Optional.of(Objects.requireNonNull(userInput).getUserInput());
            return this;
        }

        /**
         * @param error the error message describing the invalid input
         * @return {@code this} for fluent-style usage
         */
        public Builder setError(final String error) {
            Objects.requireNonNull(error);
            Preconditions.checkArgument(StringUtils.isNotBlank(error), "Error message cannot be blank");
            this.error = Optional.of(error);
            return this;
        }

        /**
         * @param location the location of the error in the input
         * @return {@code this} for fluent-style usage
         */
        public Builder setLocation(final int location) {
            Preconditions.checkArgument(location >= 0, "Error location must be positive");
            this.location = Optional.of(location);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("userInput")) {
                setUserInput(new UserInput.Builder().fromJson(mapping, json.getAsJsonObject("userInput")).build());
            }
            if (json.has("error")) {
                setError(json.getAsJsonPrimitive("error").getAsString());
            }
            if (json.has("location")) {
                setLocation(json.getAsJsonPrimitive("location").getAsInt());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InvalidInput build() {
            if (!this.userInput.isPresent()) {
                throw new IllegalStateException("User input is required when building invalid input");
            }
            if (!this.error.isPresent()) {
                throw new IllegalStateException("Error message is required when building invalid input");
            }

            return new InvalidInput(this.userInput.get(), this.error.get(), this.location);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSerializationManifest() {
            return SERIALIZATION_MANIFEST;
        }
    }
}
