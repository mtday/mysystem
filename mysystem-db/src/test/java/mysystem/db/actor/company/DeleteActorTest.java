package mysystem.db.actor.company;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.ConfigFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Status;
import akka.pattern.CircuitBreaker;
import akka.testkit.JavaTestKit;
import mysystem.db.TestDatabase;
import mysystem.db.model.DataType;
import mysystem.db.model.DeleteById;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

/**
 * Perform testing on the {@link DeleteActor} class.
 */
public class DeleteActorTest {
    private static TestDatabase testdb = new TestDatabase(DeleteActorTest.class.getSimpleName());

    /**
     * Initialize the test actor system.
     */
    @BeforeClass
    public static void setup() throws IOException, SQLException {
        testdb.load("hsqldb/tables.sql");
        testdb.load("hsqldb/testdata.sql");
    }

    private static CircuitBreaker getCircuitBreaker(final ActorSystem system) {
        final int maxFailures = 1;
        final FiniteDuration callTimeout = Duration.create(10, TimeUnit.SECONDS);
        final FiniteDuration resetTimeout = Duration.create(60, TimeUnit.SECONDS);
        return new CircuitBreaker(system.dispatcher(), system.scheduler(), maxFailures, callTimeout, resetTimeout);
    }

    @Test
    public void testReceiveDeleteById() {
        final ActorSystem system = ActorSystem.create("test-del-by-id", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final ActorRef delActor = DeleteActor.create(system, testdb.getDataSource(), getCircuitBreaker(system));

            try {
                delActor.tell(new DeleteById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Success success = expectMsgClass(duration("500 ms"), Status.Success.class);
                assertEquals("Success(Delete completed successfully)", success.toString());
            } finally {
                delActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveDeleteByIdConnectionException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-del-by-id-conn-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceGetConnectionException();
            final ActorRef delActor = DeleteActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                delActor.tell(new DeleteById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: dataSource.getConnection failed)", failure.toString());
            } finally {
                delActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveDeleteByIdConnectionCloseException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-del-by-id-conn-cl-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceConnectionCloseException();
            final ActorRef delActor = DeleteActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                delActor.tell(new DeleteById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: connection.close failed)", failure.toString());
            } finally {
                delActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveDeleteByIdPreparedStatementException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-del-by-id-ps-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourcePrepareStatementException();
            final ActorRef delActor = DeleteActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                delActor.tell(new DeleteById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: connection.prepareStatement failed)", failure.toString());
            } finally {
                delActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveDeleteByIdPreparedStatementCloseException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-del-by-id-ps-close-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourcePreparedStatementCloseException();
            final ActorRef delActor = DeleteActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                delActor.tell(new DeleteById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: preparedStatement.close failed)", failure.toString());
            } finally {
                delActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveDeleteByIdResultSetException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-del-by-id-rs-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceResultSetException();
            final ActorRef delActor = DeleteActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                delActor.tell(new DeleteById.Builder(DataType.COMPANY, 1).build(), getRef());

                // Delete has no result set, so this is successful.
                final Status.Success success = expectMsgClass(duration("500 ms"), Status.Success.class);
                assertEquals("Success(Delete completed successfully)", success.toString());
            } finally {
                delActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveDeleteByIdResultSetCloseException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-del-by-id-rs-close-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceResultSetCloseException();
            final ActorRef delActor = DeleteActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                delActor.tell(new DeleteById.Builder(DataType.COMPANY, 1).build(), getRef());

                // Delete has no result set, so this is successful.
                final Status.Success success = expectMsgClass(duration("500 ms"), Status.Success.class);
                assertEquals("Success(Delete completed successfully)", success.toString());
            } finally {
                delActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveWithUnhandled() {
        final ActorSystem system = ActorSystem.create("test-unhandled", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final ActorRef delActor = DeleteActor.create(system, testdb.getDataSource(), getCircuitBreaker(system));

            try {
                delActor.tell("unhandled", getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                delActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }
}
