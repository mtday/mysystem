package mysystem.shell.actor;

import org.apache.commons.cli.ParseException;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import mysystem.shell.model.Command;
import mysystem.shell.model.InvalidInput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationLookup;
import mysystem.shell.model.RegistrationResponse;
import mysystem.shell.model.TokenizedUserInput;
import mysystem.shell.model.UnrecognizedCommand;
import mysystem.shell.model.UserInput;

import java.util.Objects;
import java.util.Set;

/**
 * Responsible for finding the corresponding {@link Registration} for a user-entered command.
 */
public class RegistrationFinder extends UntypedActor {
    private final ActorSelection registrationManager;
    private final ActorSelection consoleManager;
    private final ActorSelection inputTokenizer;
    private final ActorSelection commandExecutor;

    /**
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        final Props props = Props.create(RegistrationFinder.class);
        return Objects.requireNonNull(actorSystem).actorOf(props, RegistrationFinder.class.getSimpleName());
    }

    /**
     * @param actorSystem the {@link ActorSystem} hosting the actor
     * @return an {@link ActorSelection} referencing this actor
     */
    public static ActorSelection getActorSelection(final ActorSystem actorSystem) {
        return Objects.requireNonNull(actorSystem).actorSelection("/user/" + RegistrationFinder.class.getSimpleName());
    }

    /**
     * Default constructor.
     */
    public RegistrationFinder() {
        this.registrationManager = RegistrationManager.getActorSelection(context().system());
        this.consoleManager = ConsoleManager.getActorSelection(context().system());
        this.inputTokenizer = InputTokenizer.getActorSelection(context().system());
        this.commandExecutor = CommandExecutor.getActorSelection(context().system());
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
     * @return a reference to the input tokenizer actor
     */
    protected ActorSelection getInputTokenizer() {
        return this.inputTokenizer;
    }

    /**
     * @return a reference to the command executor actor
     */
    protected ActorSelection getCommandExecutor() {
        return this.commandExecutor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof TokenizedUserInput) {
            // Lookup the available commands matching the user input.
            getRegistrationManager().tell(new RegistrationLookup.Builder((TokenizedUserInput) message).build(), self());
        } else if (message instanceof RegistrationResponse) {
            final RegistrationResponse resp = (RegistrationResponse) message;
            final Set<Registration> registrations = resp.getRegistrations();
            if (registrations.isEmpty()) {
                // No registrations found so the command is not recognized.
                getConsoleManager().tell(new UnrecognizedCommand.Builder(resp.getUserInput().get()).build(), self());
            } else if (registrations.size() > 1) {
                // Multiple registrations match, turn into a help command.
                final String input = resp.getUserInput().get().getUserInput().getInput();
                getInputTokenizer().tell(new UserInput.Builder("help " + input).build(), self());
            } else {
                try {
                    final Command command = new Command.Builder(resp).build();
                    // Test to make sure the command line parameters are valid.
                    command.validateCommandLine();

                    getCommandExecutor().tell(command, self());
                } catch (final ParseException error) {
                    // This will happen if the command line parameters are invalid.
                    getConsoleManager()
                            .tell(new InvalidInput.Builder(resp.getUserInput().get(), error).build(), self());
                }
            }
        } else {
            unhandled(message);
        }
    }
}
