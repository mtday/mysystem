package mysystem.db.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.hsqldb.jdbc.JDBCDriver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.cluster.UniqueAddress;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import mysystem.common.model.SystemRole;
import mysystem.db.TestDatabase;
import mysystem.db.actor.company.GetActor;
import mysystem.db.config.DatabaseConfig;
import mysystem.db.model.DataType;
import mysystem.db.model.DatabaseManagerConfig;
import mysystem.db.model.GetById;
import mysystem.db.model.ModelCollection;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.immutable.HashSet;
import scala.collection.immutable.TreeSet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        map.put(
                DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.data-type",
                ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.reset-timeout", ConfigValueFactory.fromAnyRef("60 s"));
        map.put(
                DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.actors.get-by-id.actor-class",
                ConfigValueFactory.fromAnyRef(GetActor.class.getName()));
        map.put(
                DatabaseConfig.DATABASE_ACTORS.getKey() + ".name.actors.get-by-id.message-class",
                ConfigValueFactory.fromAnyRef(GetById.class.getName()));
        map.put(DatabaseConfig.DATABASE_ACTORS.getKey() + ".invalid", ConfigValueFactory.fromAnyRef("a"));
        return ConfigFactory.parseMap(map).withFallback(ConfigFactory.load("test-config"));
    }

    private static Config getDatabaseConfig() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(
                DatabaseConfig.DATABASE_DRIVER_CLASS.getKey(),
                ConfigValueFactory.fromAnyRef(JDBCDriver.class.getName()));
        map.put(DatabaseConfig.DATABASE_JDBC_URL.getKey(), ConfigValueFactory.fromAnyRef("jdbc:hsqldb:mem:testdb"));
        map.put(DatabaseConfig.DATABASE_USERNAME.getKey(), ConfigValueFactory.fromAnyRef("SA"));
        map.put(DatabaseConfig.DATABASE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef(""));
        return ConfigFactory.parseMap(map);
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
        final ActorSystem system = ActorSystem.create("test-get-by-id", getConfig());
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
        final Config config = ConfigFactory.parseMap(new HashMap<>());
        final ActorSystem system = ActorSystem.create("test-unhandled", config);
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
        final Config config = ConfigFactory.parseMap(new HashMap<>());
        final ActorSystem system = ActorSystem.create("test-bad-data-type", config);
        new JavaTestKit(system) {{
            final ActorRef dbmgr = system.actorOf(Props.create(DatabaseManager.class, testdb.getDataSource()));

            try {
                dbmgr.tell(new GetById.Builder(DataType.COMPANY, 1).build(), getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                dbmgr.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testCreate() {
        final ActorSystem system = ActorSystem.create("test-create", getDatabaseConfig());
        new JavaTestKit(system) {{
            final ActorRef dbmgr = DatabaseManager.create(system);
            dbmgr.tell(PoisonPill.getInstance(), getRef());
            system.terminate();
        }};
    }

    private Member getMember(final String host, final int port, final SystemRole role) {
        final Address address = new Address("akka.tcp", "mysystem", host, port);
        final UniqueAddress uniqueAddress = new UniqueAddress(address, 1);
        final Set<String> roles = Sets.newHashSet("0.0.0-SNAPSHOT", role.name());
        scala.collection.immutable.Set<String> immutableRoles = JavaConversions.asScalaSet(roles).toSet();
        return new Member(uniqueAddress, 1, MemberStatus.up(), immutableRoles);
    }

    private Cluster mockClusterState(final List<Member> members) {
        TreeSet<Member> memberSet = new TreeSet<>(Member.ordering());
        for (final Member member : members) {
            memberSet = memberSet.insert(member);
        }

        final HashSet<Member> unreachable = new HashSet<>();
        final HashSet<Address> seenBy = new HashSet<>();
        final Option<Address> leader = members.isEmpty() ? Option.empty() : Option.apply(members.get(0).address());

        final ClusterEvent.CurrentClusterState state =
                new ClusterEvent.CurrentClusterState(memberSet, unreachable, seenBy, leader, null);

        final Cluster cluster = Mockito.mock(Cluster.class);
        Mockito.when(cluster.state()).thenReturn(state);
        return cluster;
    }

    @Test
    public void testGetActorSelectionNoMembers() {
        final Cluster cluster = mockClusterState(Collections.emptyList());
        final ActorSystem system = ActorSystem.create("test-create", getConfig());
        final Optional<ActorSelection> response = DatabaseManager.getActorSelection(system, cluster);
        assertFalse(response.isPresent());
    }

    @Test
    public void testGetActorSelectionNoMatchingRoles() {
        final Member m1 = getMember("127.0.0.1", 2551, SystemRole.SHELL);
        final Member m2 = getMember("127.0.0.2", 2551, SystemRole.SHELL);
        final Cluster cluster = mockClusterState(Arrays.asList(m1, m2));
        final ActorSystem system = ActorSystem.create("test-create", getConfig());
        final Optional<ActorSelection> response = DatabaseManager.getActorSelection(system, cluster);
        assertFalse(response.isPresent());
    }

    @Test
    public void testGetActorSelectionMatchingRole() {
        final Member m1 = getMember("127.0.0.1", 2551, SystemRole.SHELL);
        final Member m2 = getMember("127.0.0.2", 2551, SystemRole.SYSTEM);
        final Cluster cluster = mockClusterState(Arrays.asList(m1, m2));
        final ActorSystem system = ActorSystem.create("test-create", getConfig());
        final Optional<ActorSelection> response = DatabaseManager.getActorSelection(system, cluster);
        assertTrue(response.isPresent());
        assertEquals(
                "ActorSelection[Anchor(akka://test-create/deadLetters), Path(/user/DatabaseManager)]",
                response.get().toString());
    }
}
