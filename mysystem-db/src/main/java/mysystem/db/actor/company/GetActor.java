package mysystem.db.actor.company;

import akka.actor.*;
import mysystem.db.model.company.CompanyGet;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * This actor is responsible for retrieving {@link mysystem.model.Company} objects from the configured data source.
 */
public class GetActor extends UntypedActor {
    private final DataSource dataSource;

    /**
     * @param actorContext the {@link ActorContext} that will host the actor
     * @param dataSource   the {@link DataSource} used to manage database connections
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorContext actorContext, final DataSource dataSource) {
        final Props props = Props.create(GetActor.class, dataSource);
        return Objects.requireNonNull(actorContext).actorOf(props, GetActor.class.getSimpleName());
    }

    /**
     * @param dataSource the {@link DataSource} used to manage database connections
     */
    public GetActor(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof CompanyGet) {
            final CompanyGet get = (CompanyGet) message;

        } else {
            unhandled(message);
        }
    }
}
