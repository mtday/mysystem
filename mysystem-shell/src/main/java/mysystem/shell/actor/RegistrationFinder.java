package mysystem.shell.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.shell.model.*;

import java.util.Objects;
import java.util.Set;

/**
 * Responsible for finding the corresponding {@link Registration} for a user-entered command.
 */
public class RegistrationFinder extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorSelection registrationManager;
    private final ActorSelection consoleManager;
    private final ActorSelection inputTokenizer;
    private final ActorRef commandExecutor;

    /**
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        final Props props = Props.create(RegistrationFinder.class);
        return Objects.requireNonNull(actorSystem).actorOf(props, RegistrationFinder.class.getSimpleName());
    }

    /**
     * Default constructor.
     */
    public RegistrationFinder() {
        this.registrationManager = context().system().actorSelection("/user/" + RegistrationManager.class.getSimpleName());
        this.consoleManager = context().system().actorSelection("/user/" + ConsoleManager.class.getSimpleName());
        this.inputTokenizer = context().system().actorSelection("/user/" + InputTokenizer.class.getSimpleName());
        this.commandExecutor = CommandExecutor.create(context().system());
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
    protected ActorRef getCommandExecutor() {
        return this.commandExecutor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        log.error("Received: {}", message);
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
                getInputTokenizer().tell(new UserInput.Builder("help " + resp.getUserInput().get()).build(), self());
            } else {
                getCommandExecutor().tell(new Command.Builder(resp).build(), self());
            }
        } else {
            unhandled(message);
        }
    }
}
