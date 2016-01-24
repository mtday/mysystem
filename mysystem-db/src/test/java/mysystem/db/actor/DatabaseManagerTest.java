package mysystem.db.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import mysystem.db.TestDatabase;
import mysystem.db.actor.company.GetActor;
import mysystem.db.config.DatabaseConfig;
import mysystem.db.model.DataType;
import mysystem.db.model.DatabaseManagerConfig;
import mysystem.db.model.GetById;
import mysystem.db.model.HasDataType;
import mysystem.db.model.ModelCollection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Perform testing of the {@link DatabaseManager} class.
 */
public class DatabaseManagerTest {
    private static ActorSystem system = null;
    private static DatabaseManager actor = null;
    private static TestDatabase testdb = new TestDatabase(DatabaseManagerTest.class.getSimpleName());

    /**
     * Initialize the test actor system.
     */
    @BeforeClass
    public static void setup() throws IOException, SQLException {
        testdb.load("hsqldb/tables.sql");
        system = ActorSystem.create("test-actor-system", getConfig());

        final Props props = Props.create(DatabaseManager.class, testdb.getDataSource());
        final TestActorRef<DatabaseManager> actorRef = TestActorRef.create(system, props, "actor");
        actor = actorRef.underlyingActor();
    }

    /**
     * Shutdown the test actor system.
     */
    @AfterClass
    public static void teardown() {
        if (system != null) {
            JavaTestKit.shutdownActorSystem(system);
            system = null;
        }
    }

    private static Config getConfig() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.data-type",
                ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.reset-timeout", ConfigValueFactory.fromAnyRef("60 s"));
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.actors.get-by-id.actor-class",
                ConfigValueFactory.fromAnyRef(GetActor.class.getName()));
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.actors.get-by-id.message-class",
                ConfigValueFactory.fromAnyRef(GetById.class.getName()));
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".invalid", ConfigValueFactory.fromAnyRef("a"));
        return ConfigFactory.parseMap(map).withFallback(ConfigFactory.load("test-config"));
    }

    @Test
    public void testGetDatabaseActorConfigsNoActors() {
        final Config config = ConfigFactory.parseMap(new HashMap<String, ConfigValue>());
        final Set<DatabaseManagerConfig> configs = actor.getDatabaseActorConfigs(config);
        assertTrue(configs.isEmpty());
    }

    @Test
    public void testGetDatabaseActorConfigsWithActors() {
        final Set<DatabaseManagerConfig> configs = actor.getDatabaseActorConfigs(getConfig());
        assertEquals(1, configs.size());
        final DatabaseManagerConfig actorConfig = configs.iterator().next();
        assertEquals("name", actorConfig.getActorName());
        assertEquals(DataType.COMPANY, actorConfig.getDataType());
        assertEquals(5, actorConfig.getMaxFailures());
    }

    @Test
    public void testGetActor() {
        final Optional<ActorRef> ref = actor.getActor(DataType.COMPANY);
        assertNotNull(ref);
        assertTrue(ref.isPresent());
    }

    @Test
    public void testReceiveGetById() {
        final ActorSystem system = ActorSystem.create("test-create", getConfig());
        new JavaTestKit(system) {{
            final ActorRef dbmgr = system.actorOf(Props.create(DatabaseManager.class, testdb.getDataSource()));

            try {
                dbmgr.tell(new GetById.Builder(DataType.COMPANY, 1).build(), getRef());

                final ModelCollection response = expectMsgClass(duration("500 ms"), ModelCollection.class);
                assertNotNull(response);
                assertEquals(0, response.getModels().size());
            } finally {
                dbmgr.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveWithUnhandled() {
        final ActorSystem system = ActorSystem.create("test-create", getConfig());
        new JavaTestKit(system) {{
            final ActorRef dbmgr = system.actorOf(Props.create(DatabaseManager.class, testdb.getDataSource()));

            try {
                dbmgr.tell("unhandled", getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                dbmgr.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveWithUnrecognizedDataType() {
        final ActorSystem system = ActorSystem.create("test-create", getConfig());
        new JavaTestKit(system) {{
            final ActorRef dbmgr = system.actorOf(Props.create(DatabaseManager.class, testdb.getDataSource()));

            try {
                final HasDataType msg = () -> DataType.COMPANY;

                dbmgr.tell(msg, getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                dbmgr.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }
}
