package mysystem.shell.actor;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.shell.model.InvalidInput;
import mysystem.shell.model.TokenizedUserInput;
import mysystem.shell.model.UserInput;

import java.text.ParseException;
import java.util.Objects;

/**
 * Responsible for tokenizing user input from the shell console.
 */
public class InputTokenizer extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorRef registrationFinder;
    private final ActorSelection consoleManager;

    /**
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        final Props props = Props.create(InputTokenizer.class);
        return Objects.requireNonNull(actorSystem).actorOf(props, InputTokenizer.class.getSimpleName());
    }

    /**
     * Default constructor.
     */
    public InputTokenizer() {
        this.registrationFinder = RegistrationFinder.create(context().system());
        this.consoleManager = context().system().actorSelection("/user/" + ConsoleManager.class.getSimpleName());
    }

    /**
     * @return a reference to the registration finder actor
     */
    protected ActorRef getRegistrationFinder() {
        return this.registrationFinder;
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
        if (message instanceof UserInput) {
            final UserInput userInput = (UserInput) message;
            try {
                getRegistrationFinder().tell(new TokenizedUserInput.Builder(userInput).build(), self());
            } catch (final ParseException parseException) {
                getConsoleManager().tell(new InvalidInput.Builder(userInput, parseException).build(), self());
            }
        } else {
            unhandled(message);
        }
    }
}
