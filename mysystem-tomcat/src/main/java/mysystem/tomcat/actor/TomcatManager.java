package mysystem.tomcat.actor;

import org.apache.catalina.LifecycleException;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.common.model.SystemRole;
import mysystem.common.util.cluster.ClusterUtils;
import mysystem.tomcat.model.TomcatStart;
import mysystem.tomcat.model.TomcatStop;
import mysystem.tomcat.server.TomcatServer;

import java.util.Objects;
import java.util.Optional;

/**
 * This actor is responsible for managing the Tomcat web server.
 */
public class TomcatManager extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final TomcatServer tomcatServer;

    /**
     * @param refFactory the {@link ActorRefFactory} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorRefFactory refFactory) {
        final Props props = Props.create(TomcatManager.class);
        return Objects.requireNonNull(refFactory).actorOf(props, TomcatManager.class.getSimpleName());
    }

    /**
     * @param actorSystem the {@link ActorSystem} hosting the actor used to find the selection
     * @param cluster the {@link Cluster} from which the selection should be retrieved
     * @return an {@link ActorSelection} referencing this actor
     */
    public static Optional<ActorSelection> getActorSelection(final ActorSystem actorSystem, final Cluster cluster) {
        final Optional<Address> address =
                new ClusterUtils().getRandomNode(Objects.requireNonNull(cluster), SystemRole.SYSTEM);
        if (!address.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(Objects.requireNonNull(actorSystem)
                .actorSelection(String.format("%s/user/%s", address.get(), TomcatManager.class.getSimpleName())));
    }

    public TomcatManager() {
        this.tomcatServer = new TomcatServer(context().system(), context().system().settings().config());
    }

    protected TomcatServer getTomcatServer() {
        return this.tomcatServer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() {
        handleStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() {
        handleStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        log.info("Received: {}", message);
        if (message instanceof TomcatStart) {
            handleStart();
        } else if (message instanceof TomcatStop) {
            handleStop();
        } else {
            unhandled(message);
        }
    }

    protected void handleStart() {
        try {
            getTomcatServer().start();
            sender().tell(new Status.Success("Tomcat started"), self());
        } catch (final LifecycleException failed) {
            log.error("Failed to start tomcat", failed);
            sender().tell(new Status.Failure(failed), self());
        }
    }

    protected void handleStop() {
        try {
            getTomcatServer().stop();
            sender().tell(new Status.Success("Tomcat stopped"), self());
        } catch (final LifecycleException failed) {
            log.error("Failed to stop tomcat", failed);
            sender().tell(new Status.Failure(failed), self());
        }
    }
}
