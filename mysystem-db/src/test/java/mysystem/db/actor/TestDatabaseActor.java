package mysystem.db.actor;

import akka.actor.UntypedActor;
import akka.pattern.CircuitBreaker;

import javax.sql.DataSource;

/**
 * Used during testing to forward messages to another actor.
 */
public class TestDatabaseActor extends UntypedActor {
    /**
     * @param dataSource the data source used to retrieve connections to the database
     * @param circuitBreaker the circuit breaker used to prevent overloading
     */
    public TestDatabaseActor(final DataSource dataSource, final CircuitBreaker circuitBreaker) {
    }

    /**
     * @param msg the message to process
     */
    @Override
    public void onReceive(final Object msg) {
        sender().tell(msg, self());
    }
}
