package mysystem.db.actor.company;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.ConfigFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Status;
import akka.pattern.CircuitBreaker;
import akka.testkit.JavaTestKit;
import mysystem.common.model.Company;
import mysystem.db.TestDatabase;
import mysystem.db.model.Add;
import mysystem.db.model.DataType;
import mysystem.db.model.ModelCollection;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

/**
 * Perform testing on the {@link AddActor} class.
 */
public class AddActorTest {
    private static TestDatabase testdb = new TestDatabase(AddActorTest.class.getSimpleName());

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
    public void testReceiveAdd() {
        final ActorSystem system = ActorSystem.create("test-add", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final ActorRef addActor = AddActor.create(system, testdb.getDataSource(), getCircuitBreaker(system));

            try {
                final Company company = new Company.Builder().setName("New Company").build();
                addActor.tell(new Add.Builder<Company>(DataType.COMPANY).add(company).build(), getRef());

                final ModelCollection<?> response = expectMsgClass(duration("500 ms"), ModelCollection.class);
                assertNotNull(response);
                assertEquals(1, response.getModels().size());

                final Company created = (Company) response.getModels().iterator().next();
                assertTrue(created.getId().isPresent());
                assertEquals(company.getName(), created.getName());
                assertEquals(company.isActive(), created.isActive());

            } finally {
                addActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveAddConnectionException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-add-conn-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceGetConnectionException();
            final ActorRef addActor = AddActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                final Company company = new Company.Builder().setName("New Company").build();
                addActor.tell(new Add.Builder<Company>(DataType.COMPANY).add(company).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: dataSource.getConnection failed)", failure.toString());
            } finally {
                addActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveAddConnectionClosedException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-add-conn-close-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceConnectionCloseException();
            final ActorRef addActor = AddActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                final Company company = new Company.Builder().setName("New Company").build();
                addActor.tell(new Add.Builder<Company>(DataType.COMPANY).add(company).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: connection.close failed)", failure.toString());
            } finally {
                addActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveAddPreparedStatementException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-add-ps-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourcePrepareStatementException();
            final ActorRef addActor = AddActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                final Company company = new Company.Builder().setName("New Company").build();
                addActor.tell(new Add.Builder<Company>(DataType.COMPANY).add(company).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: connection.prepareStatement failed)", failure.toString());
            } finally {
                addActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveAddPreparedStatementCloseException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-add-ps-close-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourcePreparedStatementCloseException();
            final ActorRef addActor = AddActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                final Company company = new Company.Builder().setName("New Company").build();
                addActor.tell(new Add.Builder<Company>(DataType.COMPANY).add(company).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: preparedStatement.close failed)", failure.toString());
            } finally {
                addActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveAddResultSetException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-add-rs-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceResultSetException();
            final ActorRef addActor = AddActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                final Company company = new Company.Builder().setName("New Company").build();
                addActor.tell(new Add.Builder<Company>(DataType.COMPANY).add(company).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: resultSet.next failed)", failure.toString());
            } finally {
                addActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveAddResultSetCloseException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-add-rs-close-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceResultSetCloseException();
            final ActorRef addActor = AddActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                final Company company = new Company.Builder().setName("New Company").build();
                addActor.tell(new Add.Builder<Company>(DataType.COMPANY).add(company).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: resultSet.close failed)", failure.toString());
            } finally {
                addActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveWithUnhandled() {
        final ActorSystem system = ActorSystem.create("test-unhandled", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final ActorRef addActor = AddActor.create(system, testdb.getDataSource(), getCircuitBreaker(system));

            try {
                addActor.tell("unhandled", getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                addActor.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }
}
