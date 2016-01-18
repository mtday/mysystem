package mysystem.shell.model;

import com.google.common.base.Preconditions;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Objects;

/**
 * The immutable parsed command to be executed.
 */
public class Command implements Comparable<Command>, Serializable {
    private final static long serialVersionUID = 1L;

    private final Registration registration;
    private final UserInput userInput;
    private final CommandLine commandLine;

    /**
     * @param registration the {@link Registration} associated with the command being invoked
     * @param userInput the {@link UserInput} entered in the shell to be executed
     * @param commandLine the parsed {@link CommandLine} parameters for this command
     */
    public Command(final Registration registration, final UserInput userInput, final CommandLine commandLine) {
        this.registration = registration;
        this.userInput = userInput;
        this.commandLine = commandLine;
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
    public UserInput getUserInput() {
        return this.userInput;
    }

    /**
     * @return the parsed {@link CommandLine} parameters for this command
     */
    public CommandLine getCommandLine() {
        return this.commandLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

    /**
     * Used to build {@link Command} instances.
     */
    public static class Builder {
        private final Registration registration;
        private final UserInput userInput;
        private final CommandLine commandLine;

        /**
         * @param response the {@link RegistrationResponse} providing the command registration and the user input
         */
        public Builder(final RegistrationResponse response) {
            Objects.requireNonNull(response);
            Preconditions.checkArgument(response.getRegistrations().size() == 1);
            Preconditions.checkArgument(response.getUserInput().isPresent());

            this.registration = response.getRegistrations().iterator().next();
            this.userInput = response.getUserInput().get();
            this.commandLine = null; // TODO
        }

        /**
         * @param command the {@link Command} to duplicate
         */
        public Builder(final Command command) {
            Objects.requireNonNull(command);

            this.registration = command.getRegistration();
            this.userInput = command.getUserInput();
            this.commandLine = command.getCommandLine();
        }

        /**
         * @return the {@link Command} represented by this builder
         */
        public Command build() {
            return new Command(this.registration, this.userInput, this.commandLine);
        }
    }
}
