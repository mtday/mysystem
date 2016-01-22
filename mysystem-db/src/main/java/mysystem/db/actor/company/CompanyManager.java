package mysystem.db.actor.company;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.CircuitBreaker;
import mysystem.db.model.Add;
import mysystem.db.model.GetAll;
import mysystem.db.model.GetById;

import javax.sql.DataSource;

/**
 * This actor is responsible for managing all of the actors that have to do with {@link mysystem.common.model.Company}
 * objects in the configured data source.
 */
public class CompanyManager extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorRef companyGet;
    private final ActorRef companyAdd;

    /**
     * @param dataSource the {@link DataSource} used to manage database connections
     * @param circuitBreaker the {@link CircuitBreaker} used to manage push-back when the database gets overloaded
     */
    public CompanyManager(final DataSource dataSource, final CircuitBreaker circuitBreaker) {
        this.companyGet = GetActor.create(context(), dataSource, circuitBreaker);
        this.companyAdd = AddActor.create(context(), dataSource, circuitBreaker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        log.info("Received: {}", message);
        if (message instanceof GetById || message instanceof GetAll) {
            this.companyGet.forward(message, context());
        } else if (message instanceof Add) {
            this.companyAdd.forward(message, context());
        } else {
            unhandled(message);
        }
    }
}
