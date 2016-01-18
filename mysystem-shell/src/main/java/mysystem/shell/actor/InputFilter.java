package mysystem.shell.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.shell.model.AcceptInput;
import mysystem.shell.model.UserInput;

import java.util.Objects;

/**
 * Responsible for filtering user input so it is not processed, things like comments and blank strings.
 */
public class InputFilter extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorRef inputTokenizer;

    /**
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        final Props props = Props.create(InputFilter.class);
        return Objects.requireNonNull(actorSystem).actorOf(props, InputFilter.class.getSimpleName());
    }

    /**
     * Default constructor.
     */
    public InputFilter() {
        this.inputTokenizer = InputTokenizer.create(context().system());
        log.error("Input Tokenizer: {}", this.inputTokenizer);
    }

    /**
     * @return a reference to the input tokenizer actor
     */
    protected ActorRef getInputTokenizer() {
        return this.inputTokenizer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof UserInput) {
            final UserInput userInput = (UserInput) message;
            // Filter the user input if appropriate.
            if (userInput.isComment() || userInput.isEmpty()) {
                // We can ignore this user input. Go back and accept more input.
                sender().tell(new AcceptInput.Builder().build(), self());
            } else {
                // Forward the user input to the tokenizer.
                getInputTokenizer().tell(userInput, self());
            }
        } else {
            unhandled(message);
        }
    }
}
