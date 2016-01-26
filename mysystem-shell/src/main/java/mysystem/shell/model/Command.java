package mysystem.shell.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * The immutable parsed command to be executed.
 */
public class Command implements Model, Comparable<Command> {
    private final static String SERIALIZATION_MANIFEST = Command.class.getSimpleName();

    private final CommandPath commandPath;
    private final Registration registration;
    private final TokenizedUserInput userInput;

    /**
     * @param commandPath the {@link CommandPath} representing the user-specified command
     * @param registration the {@link Registration} associated with the command being invoked
     * @param userInput the {@link TokenizedUserInput} entered in the shell to be executed
     */
    public Command(
            final CommandPath commandPath, final Registration registration, final TokenizedUserInput userInput) {
        this.commandPath = commandPath;
        this.registration = registration;
        this.userInput = userInput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
    }

    /**
     * @return the {@link CommandPath} representing the user-specified command
     */
    public CommandPath getCommandPath() {
        return this.commandPath;
    }

    /**
     * @return the {@link Registration} associated with the command being invoked
     */
    public Registration getRegistration() {
        return this.registration;
    }

    /**
     * @return the {@link UserInput} entered in the shell to be executed
     */
    public TokenizedUserInput getUserInput() {
        return this.userInput;
    }

    /**
     * @return the parsed {@link CommandLine} parameters for this command based on the user input and registration
     * options
     */
    public Optional<CommandLine> getCommandLine() {
        try {
            return parseCommandLine(getRegistration(), getUserInput());
        } catch (final ParseException parseException) {
            // This exception is suppressed. The validateCommandLine method is used to determine whether the command
            // line is valid or not.
            return Optional.empty();
        }
    }

    /**
     * @throws ParseException if the command line parameters are invalid for some reason
     */
    public void validateCommandLine() throws ParseException {
        parseCommandLine(getRegistration(), getUserInput());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.add("commandPath", getCommandPath().toJson());
        json.add("registration", getRegistration().toJson());
        json.add("userInput", getUserInput().toJson());
        json.addProperty("manifest", getSerializationManifest());
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("commandPath", getCommandPath());
        str.append("registration", getRegistration());
        str.append("userInput", getUserInput());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final Command other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getCommandPath(), other.getCommandPath());
        cmp.append(getRegistration(), other.getRegistration());
        cmp.append(getUserInput(), other.getUserInput());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof Command) && compareTo((Command) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getRegistration().hashCode();
    }

    private static Optional<CommandLine> parseCommandLine(
            final Registration registration, final TokenizedUserInput userInput) throws ParseException {
        if (registration.getOptions().isPresent()) {
            final List<String> tokens = userInput.getTokens();
            final String[] array = tokens.toArray(new String[tokens.size()]);
            return Optional.of(new DefaultParser().parse(registration.getOptions().get().asOptions(), array));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Used to build {@link Command} instances.
     */
    public static class Builder implements ModelBuilder<Command> {
        private Optional<CommandPath> commandPath = Optional.empty();
        private Optional<Registration> registration = Optional.empty();
        private Optional<TokenizedUserInput> userInput = Optional.empty();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param response the {@link RegistrationResponse} providing the command registration and the user input
         */
        public Builder(final RegistrationResponse response) {
            Objects.requireNonNull(response);
            Preconditions.checkArgument(response.getRegistrations().size() == 1);
            Preconditions.checkArgument(response.getUserInput().isPresent());

            this.registration = Optional.of(response.getRegistrations().iterator().next());
            this.userInput = Optional.of(response.getUserInput().get());
            this.commandPath = Optional.of(new CommandPath.Builder(this.userInput.get()).build());
        }

        /**
         * @param command the {@link Command} to duplicate
         */
        public Builder(final Command command) {
            Objects.requireNonNull(command);
            setCommandPath(command.getCommandPath());
            setRegistration(command.getRegistration());
            setUserInput(command.getUserInput());
        }

        /**
         * @param registration the command registration describing the expected parameters
         * @return {@code this} for fluent-style usage
         */
        public Builder setRegistration(final Registration registration) {
            this.registration = Optional.of(Objects.requireNonNull(registration));
            return this;
        }

        /**
         * @param userInput the tokenized input provided by the user
         * @return {@code this} for fluent-style usage
         */
        public Builder setUserInput(final TokenizedUserInput userInput) {
            this.userInput = Optional.of(Objects.requireNonNull(userInput));
            return this;
        }

        /**
         * @param commandPath the path to the command as entered by the user
         * @return {@code this} for fluent-style usage
         */
        public Builder setCommandPath(final CommandPath commandPath) {
            this.commandPath = Optional.of(Objects.requireNonNull(commandPath));
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("registration")) {
                setRegistration(
                        new Registration.Builder().fromJson(mapping, json.getAsJsonObject("registration")).build());
            }
            if (json.has("userInput")) {
                setUserInput(
                        new TokenizedUserInput.Builder().fromJson(mapping, json.getAsJsonObject("userInput")).build());
            }
            if (json.has("commandPath")) {
                setCommandPath(
                        new CommandPath.Builder().fromJson(mapping, json.getAsJsonObject("commandPath")).build());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Command build() {
            if (!this.registration.isPresent()) {
                throw new IllegalStateException("A command registration is required to build a command");
            }
            if (!this.userInput.isPresent()) {
                throw new IllegalStateException("A tokenized user input is required to build a command");
            }
            if (!this.commandPath.isPresent()) {
                throw new IllegalStateException("A command path is required to build a command");
            }

            return new Command(this.commandPath.get(), this.registration.get(), this.userInput.get());
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
