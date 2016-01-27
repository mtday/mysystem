package mysystem.shell.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.typesafe.config.ConfigFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.ActorRef;
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
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;
import mysystem.shell.model.TokenizedUserInput;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.immutable.HashSet;
import scala.collection.immutable.TreeSet;

import java.util.Set;

/**
 * Perform testing of the {@link ClusterCommand} class.
 */
public class ClusterCommandTest {
    private static Cluster cluster = null;
    private static Cluster clusterWithNoState = null;
    private static ActorSystem system = null;

    /**
     * Initialize the test actor system.
     */
    @BeforeClass
    public static void setup() {
        cluster = Mockito.mock(Cluster.class);
        clusterWithNoState = Mockito.mock(Cluster.class);
        system = ActorSystem.create("test-actor-system", ConfigFactory.load("test-config"));
        mockClusterState();
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

    private static Member getMember(final String host, final int port) {
        final Address address = new Address("akka.tcp", "mysystem", host, port);
        final UniqueAddress uniqueAddress = new UniqueAddress(address, 1);
        final Set<String> roles = Sets.newHashSet("0.0.0-SNAPSHOT", "SHELL");
        scala.collection.immutable.Set<String> immutableRoles = JavaConversions.asScalaSet(roles).toSet();
        return new Member(uniqueAddress, 1, MemberStatus.up(), immutableRoles);
    }

    private static void mockClusterState() {
        final Member member1 = getMember("127.0.0.1", 2551);
        final Member member2 = getMember("127.0.0.1", 2552);

        final TreeSet<Member> members = new TreeSet<>(Member.ordering()).insert(member1).insert(member2);

        final HashSet<Member> unreachable = new HashSet<>();
        final HashSet<Address> seenBy = new HashSet<>();
        final Option<Address> leader = Option.apply(member1.address());

        final ClusterEvent.CurrentClusterState state =
                new ClusterEvent.CurrentClusterState(members, unreachable, seenBy, leader, null);

        Mockito.when(cluster.state()).thenReturn(state);
    }

    @Test
    public void testReceiveWithRegistrationRequest() {
        new JavaTestKit(system) {{
            final ActorRef clusterCommand =
                    system.actorOf(Props.create(ClusterCommand.class, cluster), ClusterCommand.class.getSimpleName());

            try {
                clusterCommand.tell(new RegistrationRequest.Builder().build(), getRef());

                final RegistrationResponse response = expectMsgClass(duration("500 ms"), RegistrationResponse.class);
                final Set<Registration> registrations = response.getRegistrations();
                assertEquals(1, registrations.size());

                final Registration registration = registrations.iterator().next();
                assertTrue(registration.getDescription().isPresent());
                assertEquals("provides information about the system cluster and its members",
                        registration.getDescription().get());
                assertEquals(new CommandPath.Builder("cluster", "list").build(), registration.getPath());
                assertFalse(registration.getOptions().isPresent());

                expectNoMsg(duration("100 ms"));
            } finally {
                clusterCommand.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithCommand() throws Exception {
        new JavaTestKit(system) {{
            final TokenizedUserInput userInput = new TokenizedUserInput.Builder("cluster list").build();
            final CommandPath path = new CommandPath.Builder("cluster", "list").build();
            final Registration reg = new Registration.Builder().setActorPath(getRef()).setPath(path).build();
            final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
            final Command command = new Command.Builder(response).build();

            final ActorRef clusterCommand =
                    system.actorOf(Props.create(ClusterCommand.class, cluster), ClusterCommand.class.getSimpleName());

            try {
                clusterCommand.tell(command, getRef());

                ConsoleOutput output = expectMsgClass(duration("500 ms"), ConsoleOutput.class);
                assertNotNull(output);
                assertTrue(output.getOutput().isPresent());
                assertEquals("  Cluster Members: 2", output.getOutput().get());
                assertTrue(output.hasMore());
                assertFalse(output.isTerminate());

                output = expectMsgClass(duration("500 ms"), ConsoleOutput.class);
                assertNotNull(output);
                assertTrue(output.getOutput().isPresent());
                assertEquals("    akka.tcp://mysystem@127.0.0.1:2551  Up  leader  0.0.0-SNAPSHOT SHELL",
                        output.getOutput().get());
                assertTrue(output.hasMore());
                assertFalse(output.isTerminate());

                output = expectMsgClass(duration("500 ms"), ConsoleOutput.class);
                assertNotNull(output);
                assertTrue(output.getOutput().isPresent());
                assertEquals("    akka.tcp://mysystem@127.0.0.1:2552  Up          0.0.0-SNAPSHOT SHELL",
                        output.getOutput().get());
                assertTrue(output.hasMore());
                assertFalse(output.isTerminate());

                output = expectMsgClass(duration("500 ms"), ConsoleOutput.class);
                assertNotNull(output);
                assertFalse(output.getOutput().isPresent());
                assertFalse(output.hasMore());
                assertFalse(output.isTerminate());
            } finally {
                clusterCommand.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithCommandNoState() throws Exception {
        new JavaTestKit(system) {{
            final TokenizedUserInput userInput = new TokenizedUserInput.Builder("cluster list").build();
            final CommandPath path = new CommandPath.Builder("cluster", "list").build();
            final Registration reg = new Registration.Builder().setActorPath(getRef()).setPath(path).build();
            final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
            final Command command = new Command.Builder(response).build();

            final ActorRef clusterCommand = system.actorOf(Props.create(ClusterCommand.class, clusterWithNoState),
                    ClusterCommand.class.getSimpleName());

            try {
                clusterCommand.tell(command, getRef());

                ConsoleOutput output = expectMsgClass(duration("500 ms"), ConsoleOutput.class);
                assertNotNull(output);
                assertFalse(output.getOutput().isPresent());
                assertFalse(output.hasMore());
                assertFalse(output.isTerminate());
            } finally {
                clusterCommand.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithUnhandled() {
        new JavaTestKit(system) {{
            final ActorRef clusterCommand =
                    system.actorOf(Props.create(ClusterCommand.class, cluster), ClusterCommand.class.getSimpleName());

            try {
                clusterCommand.tell("unhandled", getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                clusterCommand.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }
}
