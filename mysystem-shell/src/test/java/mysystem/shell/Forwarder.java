package mysystem.shell;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import java.util.Objects;

/**
 * Used during testing to forward messages to another actor.
 */
public class Forwarder extends UntypedActor {
    private final ActorRef target;

    /**
     * @param target the target to which messages will be forwarded
     */
    public Forwarder(final ActorRef target) {
        this.target = Objects.requireNonNull(target);
    }

    /**
     * @param msg the message to forward
     */
    @Override
    public void onReceive(final Object msg) {
        this.target.forward(msg, getContext());
    }
}
