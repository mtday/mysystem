package mysystem.shell.command;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.shell.actor.ConsoleManager;
import mysystem.shell.actor.RegistrationManager;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationLookup;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;

import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;

/**
 * This actor implements the {@code help} command in the shell.
 */
public class HelpCommand extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorSelection registrationManager;
    private final ActorSelection consoleManager;

    /**
     * Default constructor.
     */
    public HelpCommand() {
        this.registrationManager = RegistrationManager.getActorSelection(context().system());
        this.consoleManager = ConsoleManager.getActorSelection(context().system());
    }

    /**
     * @return a reference to the registration manager actor
     */
    protected ActorSelection getRegistrationManager() {
        return this.registrationManager;
    }

    /**
     * @return a reference to the console manager actor
     */
    protected ActorSelection getConsoleManager() {
        return this.consoleManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof RegistrationRequest) {
            handleRegistrationRequest();
        } else if (message instanceof Command) {
            handleCommand((Command) message);
        } else if (message instanceof RegistrationResponse) {
            handleRegistrations((RegistrationResponse) message);
        } else {
            unhandled(message);
        }
    }

    private void handleRegistrationRequest() {
        final String description = "display usage information for available shell commands";
        final CommandPath help = new CommandPath.Builder("help").build();
        final Registration reg = new Registration.Builder(self(), help).setDescription(description).build();
        sender().tell(new RegistrationResponse.Builder().add(reg).build(), self());
    }

    protected void handleCommand(final Command command) {
        final CommandPath path = command.getCommandPath();
        if (path.isPrefix(new CommandPath.Builder("help").build()) && path.getSize() > 1) {
            // Strip off the "help" at the front and lookup the registrations for which help should be retrieved.
            getRegistrationManager().tell(new RegistrationLookup.Builder(path.getChild().get()).build(), self());
        } else {
            // Request all of the available registrations.
            getRegistrationManager().tell(new RegistrationRequest.Builder().build(), self());
        }
    }

    protected void handleRegistrations(final RegistrationResponse response) {
        // Only show options for help with a single command
        final boolean includeOptions = response.getRegistrations().size() == 1;

        // Determine the longest command path length to better format the output
        final OptionalInt longestPath =
                response.getRegistrations().stream().mapToInt(r -> r.getPath().toString().length()).max();

        final List<ConsoleOutput> output = new LinkedList<>();
        response.getRegistrations().forEach(r -> output.addAll(getOutput(r, includeOptions, longestPath)));
        output.forEach(o -> getConsoleManager().tell(o, self()));
        getConsoleManager().tell(new ConsoleOutput.Builder().build(), self());
    }

    protected List<ConsoleOutput> getOutput(
            final Registration registration, final boolean includeOptions, final OptionalInt longestPath) {
        final List<ConsoleOutput> output = new LinkedList<>();
        output.add(new ConsoleOutput.Builder(getDescription(registration, longestPath)).setHasMore(true).build());
        if (includeOptions) {
            output.addAll(getOptions(registration));
        }
        return output;
    }

    protected String getDescription(final Registration registration, final OptionalInt longestPath) {
        if (registration.getDescription().isPresent()) {
            final String path = StringUtils.rightPad(registration.getPath().toString(), longestPath.getAsInt());
            return String.format("  %s  %s", path, registration.getDescription().get());
        }
        return String.format("  %s", registration.getPath().toString());
    }

    protected List<ConsoleOutput> getOptions(final Registration registration) {
        final List<ConsoleOutput> output = new LinkedList<>();
        if (registration.getOptions().isPresent()) {
            for (final Option option : registration.getOptions().get().getOptions()) {
                final StringBuilder str = new StringBuilder("    ");
                str.append("-");
                str.append(option.getOpt());
                if (option.getLongOpt() != null) {
                    str.append("  --");
                    str.append(option.getLongOpt());
                }
                if (option.isRequired()) {
                    str.append("  (required)");
                }
                if (option.getDescription() != null) {
                    str.append("  ");
                    str.append(option.getDescription());
                }
                output.add(new ConsoleOutput.Builder(str.toString()).setHasMore(true).build());
            }
        }
        return output;
    }
}
