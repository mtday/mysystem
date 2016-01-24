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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The immutable parsed command to be executed.
 */
public class Command implements Model, Comparable<Command> {
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
            // During the Builder process, this command was guaranteed to have a valid command line, so we can
            // safely suppress this exception here.
            return Optional.empty();
        }
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
    public int compareTo(final Command other) {
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
         * @throws ParseException if there is a problem with the user input matching the command options
         */
        public Builder(final RegistrationResponse response) throws ParseException {
            Objects.requireNonNull(response);
            Preconditions.checkArgument(response.getRegistrations().size() == 1);
            Preconditions.checkArgument(response.getUserInput().isPresent());

            this.registration = Optional.of(response.getRegistrations().iterator().next());
            this.userInput = Optional.of(response.getUserInput().get());
            this.commandPath = Optional.of(new CommandPath.Builder(this.userInput.get()).build());

            // Used to verify that the command line parameters are valid before the command is created.
            // This guarantees that the command, once built, will be able to create the command line without issue.
            parseCommandLine(this.registration.get(), this.userInput.get());
        }

        /**
         * @param command the {@link Command} to duplicate
         */
        public Builder(final Command command) {
            Objects.requireNonNull(command);

            this.commandPath = Optional.of(command.getCommandPath());
            this.registration = Optional.of(command.getRegistration());
            this.userInput = Optional.of(command.getUserInput());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final JsonObject json) {
            Objects.requireNonNull(json);

            if (json.has("commandPath")) {
                this.commandPath =
                        Optional.of(new CommandPath.Builder().fromJson(json.getAsJsonObject("commandPath")).build());
            }
            if (json.has("registration")) {
                this.registration =
                        Optional.of(new Registration.Builder().fromJson(json.getAsJsonObject("registration")).build());
            }
            if (json.has("userInput")) {
                this.userInput = Optional.of(
                        new TokenizedUserInput.Builder().fromJson(json.getAsJsonObject("userInput")).build());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Command build() {
            if (!this.commandPath.isPresent()) {
                throw new IllegalStateException("A command path is required to build a command");
            }
            if (!this.registration.isPresent()) {
                throw new IllegalStateException("A command registration is required to build a command");
            }
            if (!this.userInput.isPresent()) {
                throw new IllegalStateException("A tokenized user input is required to build a command");
            }

            return new Command(this.commandPath.get(), this.registration.get(), this.userInput.get());
        }
    }
}
