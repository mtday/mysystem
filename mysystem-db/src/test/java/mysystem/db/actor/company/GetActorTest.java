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
import mysystem.db.model.DataType;
import mysystem.db.model.GetAll;
import mysystem.db.model.GetById;
import mysystem.db.model.ModelCollection;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

/**
 * Perform testing on the {@link GetActor} class.
 */
public class GetActorTest {
    private static TestDatabase testDatabase = new TestDatabase(GetActorTest.class.getSimpleName());

    /**
     * Initialize the test actor system.
     */
    @BeforeClass
    public static void setup() throws IOException, SQLException {
        testDatabase.load("hsqldb/tables.sql");
        testDatabase.load("hsqldb/testdata.sql");
    }

    private static CircuitBreaker getCircuitBreaker(final ActorSystem system) {
        final int maxFailures = 1;
        final FiniteDuration callTimeout = Duration.create(10, TimeUnit.SECONDS);
        final FiniteDuration resetTimeout = Duration.create(60, TimeUnit.SECONDS);
        return new CircuitBreaker(system.dispatcher(), system.scheduler(), maxFailures, callTimeout, resetTimeout);
    }

    @Test
    public void testReceiveGetById() {
        final ActorSystem system = ActorSystem.create("test-get-by-id", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final ActorRef getActor = GetActor.create(system, testDatabase.getDataSource(), getCircuitBreaker(system));

            try {
                getActor.tell(new GetById.Builder(DataType.COMPANY, 1).build(), getRef());

                final ModelCollection response = expectMsgClass(duration("500 ms"), ModelCollection.class);
                assertNotNull(response);
                assertEquals(1, response.getModels().size());

                final Company a = new Company.Builder().setId(1).setName("Test Company").setActive(true).build();
                assertTrue(response.getModels().contains(a));
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetByIdWithActive() {
        final ActorSystem system = ActorSystem.create("test-get-by-id-with-active", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final ActorRef getActor = GetActor.create(system, testDatabase.getDataSource(), getCircuitBreaker(system));

            try {
                getActor.tell(new GetById.Builder(DataType.COMPANY, 1).setActive(true).build(), getRef());

                final ModelCollection response = expectMsgClass(duration("500 ms"), ModelCollection.class);
                assertNotNull(response);
                assertEquals(1, response.getModels().size());

                final Company a = new Company.Builder().setId(1).setName("Test Company").setActive(true).build();
                assertTrue(response.getModels().contains(a));
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetByIdConnectionException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-by-id-conn-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceGetConnectionException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetByIdConnectionCloseException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-by-id-conn-cl-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceConnectionCloseException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetByIdPreparedStatementException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-by-id-ps-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourcePrepareStatementException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetByIdPreparedStatementCloseException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-by-id-ps-close-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourcePreparedStatementCloseException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetByIdResultSetException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-by-id-rs-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceResultSetException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetByIdResultSetCloseException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-by-id-rs-close-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceResultSetCloseException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetById.Builder(DataType.COMPANY, 1).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetAll() {
        final ActorSystem system = ActorSystem.create("test-get-all", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final ActorRef getActor = GetActor.create(system, testDatabase.getDataSource(), getCircuitBreaker(system));

            try {
                getActor.tell(new GetAll.Builder(DataType.COMPANY).build(), getRef());

                final ModelCollection response = expectMsgClass(duration("500 ms"), ModelCollection.class);
                assertNotNull(response);
                assertEquals(2, response.getModels().size());

                final Company a = new Company.Builder().setId(1).setName("Test Company").setActive(true).build();
                final Company b = new Company.Builder().setId(2).setName("Another Company").setActive(false).build();
                assertTrue(response.getModels().contains(a));
                assertTrue(response.getModels().contains(b));

            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetAllWithActive() {
        final ActorSystem system = ActorSystem.create("test-get-all-with-active", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final ActorRef getActor = GetActor.create(system, testDatabase.getDataSource(), getCircuitBreaker(system));

            try {
                getActor.tell(new GetAll.Builder(DataType.COMPANY).setActive(true).build(), getRef());

                final ModelCollection response = expectMsgClass(duration("500 ms"), ModelCollection.class);
                assertNotNull(response);
                assertEquals(1, response.getModels().size());

                final Company a = new Company.Builder().setId(1).setName("Test Company").setActive(true).build();
                assertTrue(response.getModels().contains(a));
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetAllConnectionException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-all-conn-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceGetConnectionException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetAll.Builder(DataType.COMPANY).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetAllConnectionClosedException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-all-conn-close-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceConnectionCloseException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetAll.Builder(DataType.COMPANY).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetAllPreparedStatementException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-all-ps-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourcePrepareStatementException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetAll.Builder(DataType.COMPANY).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetAllPreparedStatementCloseException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-all-ps-close-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourcePreparedStatementCloseException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetAll.Builder(DataType.COMPANY).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetAllResultSetException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-all-rs-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceResultSetException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetAll.Builder(DataType.COMPANY).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveGetAllResultSetCloseException() throws SQLException {
        final ActorSystem system = ActorSystem.create("test-get-all-rs-close-exc", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final DataSource dataSource = TestDatabase.getMockDataSourceResultSetCloseException();
            final ActorRef getActor = GetActor.create(system, dataSource, getCircuitBreaker(system));

            try {
                getActor.tell(new GetAll.Builder(DataType.COMPANY).build(), getRef());

                final Status.Failure failure = expectMsgClass(duration("500 ms"), Status.Failure.class);
                assertEquals("Failure(java.sql.SQLException: sql-exception)", failure.toString());
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveWithUnhandled() {
        final ActorSystem system = ActorSystem.create("test-unhandled", ConfigFactory.load("test-config"));
        new JavaTestKit(system) {{
            final ActorRef getActor = GetActor.create(system, testDatabase.getDataSource(), getCircuitBreaker(system));

            try {
                getActor.tell("unhandled", getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                getActor.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }
}
