package mysystem.shell.actor;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import mysystem.shell.model.InvalidInput;
import mysystem.shell.model.TokenizedUserInput;
import mysystem.shell.model.UserInput;

import java.text.ParseException;
import java.util.Objects;

/**
 * Responsible for tokenizing user input from the shell console.
 */
public class InputTokenizer extends UntypedActor {
    private final ActorSelection registrationFinder;
    private final ActorSelection consoleManager;

    /**
     * @param refFactory the {@link ActorRefFactory} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorRefFactory refFactory) {
        final Props props = Props.create(InputTokenizer.class);
        return Objects.requireNonNull(refFactory).actorOf(props, InputTokenizer.class.getSimpleName());
    }

    /**
     * @param refFactory the {@link ActorRefFactory} hosting the actor
     * @return an {@link ActorSelection} referencing this actor
     */
    public static ActorSelection getActorSelection(final ActorRefFactory refFactory) {
        return Objects.requireNonNull(refFactory).actorSelection("/user/" + InputTokenizer.class.getSimpleName());
    }

    /**
     * Default constructor.
     */
    public InputTokenizer() {
        this.registrationFinder = RegistrationFinder.getActorSelection(context().system());
        this.consoleManager = ConsoleManager.getActorSelection(context().system());
    }

    /**
     * @return a reference to the registration finder actor
     */
    protected ActorSelection getRegistrationFinder() {
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
                // Tokenize the user input and send to the registration finder.
                getRegistrationFinder().tell(new TokenizedUserInput.Builder(userInput).build(), self());
            } catch (final ParseException parseException) {
                getConsoleManager().tell(new InvalidInput.Builder(userInput, parseException).build(), self());
            }
        } else {
            unhandled(message);
        }
    }
}
