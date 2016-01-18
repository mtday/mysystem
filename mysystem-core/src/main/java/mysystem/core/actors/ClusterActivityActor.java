package mysystem.core.actors;

import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 *
 */
public class ClusterActivityActor extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Cluster cluster = Cluster.get(getContext().system());

    /**
     * Default constructor.
     */
    public ClusterActivityActor() {
        log.info("Default constructor");
    }

    @Override
    public void preStart() {
        log.info("preStart");
        this.cluster.subscribe(self(), ClusterEvent.initialStateAsEvents(), ClusterEvent.MemberEvent.class,
                ClusterEvent.UnreachableMember.class);
    }

    @Override
    public void postStop() {
        log.info("postStop");
        this.cluster.unsubscribe(self());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        log.info("Received: {}", message);
    }
}
