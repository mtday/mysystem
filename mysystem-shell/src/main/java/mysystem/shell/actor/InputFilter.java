package mysystem.shell.actor;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import mysystem.shell.model.AcceptInput;
import mysystem.shell.model.UserInput;

import java.util.Objects;

/**
 * Responsible for filtering user input so it is not processed, things like comments and blank strings.
 */
public class InputFilter extends UntypedActor {
    private final ActorSelection inputTokenizer;

    /**
     * @param refFactory the {@link ActorRefFactory} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorRefFactory refFactory) {
        final Props props = Props.create(InputFilter.class);
        return Objects.requireNonNull(refFactory).actorOf(props, InputFilter.class.getSimpleName());
    }

    /**
     * @param refFactory the {@link ActorRefFactory} hosting the actor
     * @return an {@link ActorSelection} referencing this actor
     */
    public static ActorSelection getActorSelection(final ActorRefFactory refFactory) {
        return Objects.requireNonNull(refFactory).actorSelection("/user/" + InputFilter.class.getSimpleName());
    }

    /**
     * Default constructor.
     */
    public InputFilter() {
        this.inputTokenizer = InputTokenizer.getActorSelection(context().system());
    }

    /**
     * @return a reference to the input tokenizer actor
     */
    protected ActorSelection getInputTokenizer() {
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
