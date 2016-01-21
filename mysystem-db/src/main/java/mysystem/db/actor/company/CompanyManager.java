package mysystem.db.actor.company;

import akka.actor.*;
import mysystem.db.model.company.CompanyGet;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * This actor is responsible for managing all of the actors that have to do with {@link mysystem.model.Company} objects
 * in the configured data source.
 */
public class CompanyManager extends UntypedActor {
    private final ActorRef companyGet;

    /**
     * @param actorContext the {@link ActorContext} that will host the actor
     * @param dataSource the {@link DataSource} used to manage database connections
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorContext actorContext, final DataSource dataSource) {
        final Props props = Props.create(CompanyManager.class, dataSource);
        return Objects.requireNonNull(actorContext).actorOf(props, CompanyManager.class.getSimpleName());
    }

    /**
     * @param dataSource the {@link DataSource} used to manage database connections
     */
    public CompanyManager(final DataSource dataSource) {
        this.companyGet = GetActor.create(context(), dataSource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof CompanyGet) {
            this.companyGet.tell(message, sender());
        } else {
            unhandled(message);
        }
    }
}
