package mysystem.db.actor.company;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.pattern.CircuitBreaker;
import akka.pattern.Patterns;
import mysystem.common.model.Company;
import mysystem.db.model.DeleteById;
import scala.concurrent.Future;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

/**
 * This actor is responsible for deleting {@link Company} objects from the configured data source.
 */
public class DeleteActor extends UntypedActor {
    private final DataSource dataSource;
    private final CircuitBreaker circuitBreaker;

    /**
     * @param actorRefFactory the {@link ActorRefFactory} that will be used to create actor references
     * @param dataSource the {@link DataSource} used to manage database connections
     * @param circuitBreaker the {@link CircuitBreaker} used to manage push-back when the database gets overloaded
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(
            final ActorRefFactory actorRefFactory, final DataSource dataSource, final CircuitBreaker circuitBreaker) {
        final Props props = Props.create(DeleteActor.class, dataSource, circuitBreaker);
        return Objects.requireNonNull(actorRefFactory).actorOf(props, DeleteActor.class.getSimpleName());
    }

    /**
     * @param dataSource the {@link DataSource} used to manage database connections
     * @param circuitBreaker the {@link CircuitBreaker} used to manage push-back when the database gets overloaded
     */
    public DeleteActor(final DataSource dataSource, final CircuitBreaker circuitBreaker) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.circuitBreaker = Objects.requireNonNull(circuitBreaker);
    }

    protected DataSource getDataSource() {
        return this.dataSource;
    }

    protected CircuitBreaker getCircuitBreaker() {
        return this.circuitBreaker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof DeleteById) {
            final Callable<Future<Status.Success>> callable = handleDeleteById((DeleteById) message);
            final Future<Status.Success> future = getCircuitBreaker().callWithCircuitBreaker(callable);
            Patterns.pipe(future, context().dispatcher()).to(sender());
        } else {
            unhandled(message);
        }
    }

    protected Callable<Future<Status.Success>> handleDeleteById(final DeleteById deleteById) {
        final String sql = "DELETE FROM companies WHERE id = ?";

        return () -> Futures.future(() -> {
            try (final Connection conn = getDataSource().getConnection();
                 final PreparedStatement ps = conn.prepareStatement(sql)) {
                for (final Integer id : deleteById.getIds()) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }

            return new Status.Success("Delete completed successfully");
        }, context().dispatcher());
    }
}
