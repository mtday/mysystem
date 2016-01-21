package mysystem.db.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

import java.util.Objects;

/**
 * This actor is responsible for retrieving {@link Company} objects from the configured data source.
 */
public class CompanyGet extends UntypedActor {
    /**
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        final Props props = Props.create(CompanyGet.class);
        return Objects.requireNonNull(actorSystem).actorOf(props, CompanyGet.class.getSimpleName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof String) {

        } else {
            unhandled(message);
        }
    }
}
