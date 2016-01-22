package mysystem.shell.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import mysystem.shell.command.ExitCommand;
import mysystem.shell.command.HelpCommand;
import mysystem.shell.config.ShellConfig;
import mysystem.shell.model.CommandConfig;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationLookup;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Perform testing of the {@link RegistrationManager} class.
 */
public class RegistrationManagerTest {
    private static ActorSystem system = null;
    private static TestActorRef<RegistrationManager> actorRef = null;
    private static RegistrationManager actor = null;

    /**
     * Initialize the test actor system.
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("test-actor-system", ConfigFactory.load("test-config"));
        actorRef = TestActorRef.create(system, Props.create(RegistrationManager.class), "actor");
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

    @Test
    public void testReceiveWithRegistrationResponseAndRequest() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef regmgr = RegistrationManager.create(system);

            try {
                final CommandPath path = new CommandPath.Builder("exit").build();
                final Registration reg = new Registration.Builder(getRef(), path).build();
                final RegistrationResponse response = new RegistrationResponse.Builder(reg).build();

                regmgr.tell(response, getRef());

                final RegistrationRequest request = new RegistrationRequest.Builder().build();
                regmgr.tell(request, getRef());

                final Set<Registration> registrations =
                        expectMsgClass(duration("500 ms"), RegistrationResponse.class).getRegistrations();

                assertEquals(1, registrations.size());
                assertTrue(registrations.contains(reg));
            } finally {
                regmgr.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithRegistrationLookup() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef regmgr = RegistrationManager.create(system);

            try {
                final CommandPath exitPath = new CommandPath.Builder("exit").build();
                final Registration exit = new Registration.Builder(getRef(), exitPath).build();
                final CommandPath quitPath = new CommandPath.Builder("quit").build();
                final Registration quit = new Registration.Builder(getRef(), quitPath).build();
                final CommandPath helpPath = new CommandPath.Builder("help").build();
                final Registration help = new Registration.Builder(getRef(), helpPath).build();

                final RegistrationResponse response = new RegistrationResponse.Builder(exit, quit, help).build();

                regmgr.tell(response, getRef());

                regmgr.tell(new RegistrationLookup.Builder(exitPath).build(), getRef());
                final Set<Registration> exitRegs =
                        expectMsgClass(duration("500 ms"), RegistrationResponse.class).getRegistrations();
                assertEquals(1, exitRegs.size());
                assertTrue(exitRegs.contains(exit));

                final CommandPath whatever = new CommandPath.Builder("whatever").build();
                regmgr.tell(new RegistrationLookup.Builder(whatever).build(), getRef());

                final Set<Registration> whateverRegs =
                        expectMsgClass(duration("500 ms"), RegistrationResponse.class).getRegistrations();
                assertTrue(whateverRegs.isEmpty());
            } finally {
                regmgr.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveUnhandled() {
        new JavaTestKit(system) {{
            final ActorRef regmgr = RegistrationManager.create(system);

            try {
                regmgr.tell("unhandled", getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                regmgr.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testCreateActorsAndRequestCommandRegistrations() {
        final Map<String, String> map = new HashMap<>();
        final Config configWithoutCommands = ConfigFactory.parseMap(map);

        map.put(ShellConfig.SHELL_COMMANDS.getKey() + ".a.class", ExitCommand.class.getName());
        map.put(ShellConfig.SHELL_COMMANDS.getKey() + ".b.class", HelpCommand.class.getName());
        final Config configWithCommands = ConfigFactory.parseMap(map);

        actor.createActorsAndRequestCommandRegistrations(configWithoutCommands);
        actor.createActorsAndRequestCommandRegistrations(configWithCommands);
    }

    @Test
    public void testCreateCommandActors() {
        final Map<String, String> map = new HashMap<>();
        map.put(ShellConfig.SHELL_COMMANDS.getKey() + ".exit.class", ExitCommand.class.getName());
        map.put(ShellConfig.SHELL_COMMANDS.getKey() + ".help.class", HelpCommand.class.getName());

        final Config config = ConfigFactory.parseMap(map);
        final List<ActorRef> actors = actor.createCommandActors(config);
        assertEquals(2, actors.size());

        final ActorRef ref1 = actors.get(0);
        assertEquals("akka://test-actor-system/user/exit", ref1.path().toString());

        final ActorRef ref2 = actors.get(1);
        assertEquals("akka://test-actor-system/user/help", ref2.path().toString());
    }

    @Test
    public void testActor() {
        final Map<String, String> map = new HashMap<>();
        map.put("class", ExitCommand.class.getName());
        final Config config = ConfigFactory.parseMap(map);
        final CommandConfig cmd = new CommandConfig.Builder("cmd", config).build();

        final ActorRef actorRef = actor.actor(system, cmd);
        assertNotNull(actorRef);

        assertEquals("akka://test-actor-system/user/cmd", actorRef.path().toString());
    }

    @Test
    public void testGetActorSelection() {
        final ActorSelection expected = system.actorSelection("/user/" + RegistrationManager.class.getSimpleName());
        assertEquals(expected, RegistrationManager.getActorSelection(system));
    }

    @Test
    public void testGetCommandConfigsEmpty() {
        final Config config = ConfigFactory.empty();
        final List<CommandConfig> configs = actor.getCommandConfigs(config);
        assertTrue(configs.isEmpty());
    }

    @Test
    public void testGetCommandConfigs() {
        final Map<String, String> map = new HashMap<>();
        map.put(ShellConfig.SHELL_COMMANDS.getKey() + ".invalid", "whatever");
        map.put(ShellConfig.SHELL_COMMANDS.getKey() + ".exit.class", ExitCommand.class.getName());
        map.put(ShellConfig.SHELL_COMMANDS.getKey() + ".help.class", HelpCommand.class.getName());

        final Config config = ConfigFactory.parseMap(map);
        final List<CommandConfig> configs = actor.getCommandConfigs(config);
        assertEquals(2, configs.size());

        final CommandConfig exit = configs.get(0);
        assertEquals("exit", exit.getCommandName());
        assertEquals(ExitCommand.class, exit.getCommandClass());

        final CommandConfig help = configs.get(1);
        assertEquals("help", help.getCommandName());
        assertEquals(HelpCommand.class, help.getCommandClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCommandConfigUnrecognizedClass() {
        final Map<String, String> map = new HashMap<>();
        map.put(ShellConfig.SHELL_COMMANDS.getKey() + ".cmd.class", "doesNotExist");

        actor.getCommandConfigs(ConfigFactory.parseMap(map));
    }
}
