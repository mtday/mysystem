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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * An immutable class representing a possible option available to a command.
 */
public class Option implements Model, Comparable<Option> {
    private final static String SERIALIZATION_MANIFEST = Option.class.getSimpleName();

    private final String description;
    private final String shortOption;
    private final Optional<String> longOption;
    private final Optional<String> argName;
    private final int arguments;
    private final boolean required;
    private final boolean optionalArg;

    /**
     * @param description the description of the option
     * @param shortOption the name of the option in short form
     * @param longOption the name of the option in long form, possibly empty
     * @param argName the name of the argument for the option
     * @param arguments the number of arguments expected with this option
     * @param required whether this option is required
     * @param optionalArg whether this option supports an optional argument
     */
    private Option(
            final String description, final String shortOption, final Optional<String> longOption,
            final Optional<String> argName, final int arguments, final boolean required, final boolean optionalArg) {
        this.description = description;
        this.shortOption = shortOption;
        this.longOption = longOption;
        this.argName = argName;
        this.arguments = arguments;
        this.required = required;
        this.optionalArg = optionalArg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
    }

    /**
     * @return the description of the option
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the name of the option in short form
     */
    public String getShortOption() {
        return this.shortOption;
    }

    /**
     * @return the name of the option in long form
     */
    public Optional<String> getLongOption() {
        return this.longOption;
    }

    /**
     * @return the name of the argument for the option
     */
    public Optional<String> getArgName() {
        return this.argName;
    }

    /**
     * @return the number of arguments expected in this option
     */
    public int getArguments() {
        return this.arguments;
    }

    /**
     * @return whether this option is required
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * @return whether this option supports an optional argument
     */
    public boolean hasOptionalArg() {
        return this.optionalArg;
    }

    /**
     * @return the commons-cli option implementation corresponding to this object
     */
    public org.apache.commons.cli.Option asOption() {
        final org.apache.commons.cli.Option option =
                new org.apache.commons.cli.Option(getShortOption(), getDescription());
        if (getLongOption().isPresent()) {
            option.setLongOpt(getLongOption().get());
        }
        if (getArgName().isPresent()) {
            option.setArgName(getArgName().get());
        }
        option.setArgs(getArguments());
        option.setRequired(isRequired());
        option.setOptionalArg(hasOptionalArg());
        return option;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("description", getDescription());
        json.addProperty("shortOption", getShortOption());
        if (getLongOption().isPresent()) {
            json.addProperty("longOption", getLongOption().get());
        }
        if (getArgName().isPresent()) {
            json.addProperty("argName", getArgName().get());
        }
        json.addProperty("arguments", getArguments());
        json.addProperty("required", isRequired());
        json.addProperty("optionalArg", hasOptionalArg());
        json.addProperty("manifest", getSerializationManifest());
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("description", getDescription());
        str.append("shortOption", getShortOption());
        str.append("longOption", getLongOption());
        str.append("argName", getArgName());
        str.append("arguments", getArguments());
        str.append("required", isRequired());
        str.append("optionalArg", hasOptionalArg());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final Option other) {
        if (other == null) {
            return 1;
        }

        final OptionalComparator<String> optionalComparatorString = new OptionalComparator<>();
        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getDescription(), other.getDescription());
        cmp.append(getShortOption(), other.getShortOption());
        cmp.append(getLongOption(), other.getLongOption(), optionalComparatorString);
        cmp.append(getArgName(), other.getArgName(), optionalComparatorString);
        cmp.append(getArguments(), other.getArguments());
        cmp.append(isRequired(), other.isRequired());
        cmp.append(hasOptionalArg(), other.hasOptionalArg());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof Option) && compareTo((Option) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getDescription());
        hash.append(getShortOption());
        hash.append(getLongOption());
        hash.append(getArgName());
        hash.append(getArguments());
        hash.append(isRequired());
        hash.append(hasOptionalArg());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link Option} objects.
     */
    public static class Builder implements ModelBuilder<Option> {
        private Optional<String> description = Optional.empty();
        private Optional<String> shortOption = Optional.empty();
        private Optional<String> longOption = Optional.empty();
        private Optional<String> argName = Optional.empty();
        private int arguments = 0;
        private boolean required = false;
        private boolean optionalArg = false;

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param other the {@link Option} to duplicate
         */
        public Builder(final Option other) {
            Objects.requireNonNull(other);
            setDescription(other.getDescription());
            setShortOption(other.getShortOption());
            if (other.getLongOption().isPresent()) {
                setLongOption(other.getLongOption().get());
            }
            if (other.getArgName().isPresent()) {
                setArgName(other.getArgName().get());
            }
            setArguments(other.getArguments());
            setRequired(other.isRequired());
            setOptionalArg(other.hasOptionalArg());
        }

        /**
         * @param description the description of the option
         * @return {@code this} for fluent-style usage
         */
        public Builder setDescription(final String description) {
            Preconditions.checkArgument(StringUtils.isNotBlank(Objects.requireNonNull(description)));
            this.description = Optional.of(description);
            return this;
        }

        /**
         * @param shortOption the name of the option in short form
         * @return {@code this} for fluent-style usage
         */
        public Builder setShortOption(final String shortOption) {
            Preconditions.checkArgument(StringUtils.isNotBlank(Objects.requireNonNull(shortOption)));
            this.shortOption = Optional.of(shortOption);
            return this;
        }

        /**
         * @param longOption the name of the option in long form
         * @return {@code this} for fluent-style usage
         */
        public Builder setLongOption(final String longOption) {
            Preconditions.checkArgument(StringUtils.isNotBlank(Objects.requireNonNull(longOption)));
            this.longOption = Optional.of(longOption);
            return this;
        }

        /**
         * @param argName the name of the argument for the option
         * @return {@code this} for fluent-style usage
         */
        public Builder setArgName(final String argName) {
            Preconditions.checkArgument(StringUtils.isNotBlank(Objects.requireNonNull(argName)));
            this.argName = Optional.of(argName);
            return this;
        }

        /**
         * @param arguments the number of arguments expected for this option
         * @return {@code this} for fluent-style usage
         */
        public Builder setArguments(final int arguments) {
            Preconditions.checkArgument(arguments >= 0, "Number of arguments cannot be negative");
            this.arguments = arguments;
            return this;
        }

        /**
         * @param required whether this option is required
         * @return {@code this} for fluent-style usage
         */
        public Builder setRequired(final boolean required) {
            this.required = required;
            return this;
        }

        /**
         * @param optionalArg whether this option has an optional argument
         * @return {@code this} for fluent-style usage
         */
        public Builder setOptionalArg(final boolean optionalArg) {
            this.optionalArg = optionalArg;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("description")) {
                setDescription(json.getAsJsonPrimitive("description").getAsString());
            }
            if (json.has("shortOption")) {
                setShortOption(json.getAsJsonPrimitive("shortOption").getAsString());
            }
            if (json.has("longOption")) {
                setLongOption(json.getAsJsonPrimitive("longOption").getAsString());
            }
            if (json.has("argName")) {
                setArgName(json.getAsJsonPrimitive("argName").getAsString());
            }
            if (json.has("arguments")) {
                setArguments(json.getAsJsonPrimitive("arguments").getAsInt());
            }
            if (json.has("required")) {
                setRequired(json.getAsJsonPrimitive("required").getAsBoolean());
            }
            if (json.has("optionalArg")) {
                setOptionalArg(json.getAsJsonPrimitive("optionalArg").getAsBoolean());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Option build() {
            if (!this.description.isPresent()) {
                throw new IllegalStateException("A description is required");
            }
            if (!this.shortOption.isPresent()) {
                throw new IllegalStateException("A short option is required");
            }

            return new Option(this.description.get(), this.shortOption.get(), this.longOption, this.argName,
                    this.arguments, this.required, this.optionalArg);
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
