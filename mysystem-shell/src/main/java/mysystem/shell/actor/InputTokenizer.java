package mysystem.shell.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.shell.model.UserInput;

import java.util.Objects;

/**
 * Responsible for tokenizing user input from the shell console.
 */
public class InputTokenizer extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorRef registrationFinder;

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
    }

    /**
     * @return a reference to the registration finder actor
     */
    protected ActorRef getRegistrationFinder() {
        return this.registrationFinder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof UserInput) {
            // TODO: Tokenize the user input.

            getRegistrationFinder().tell(message, self());
        } else {
            unhandled(message);
        }
    }
}
