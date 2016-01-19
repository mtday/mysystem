package mysystem.shell.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.ConfigFactory;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import mysystem.shell.actor.ConsoleManager;
import mysystem.shell.actor.RegistrationManager;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationLookup;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;
import mysystem.shell.model.TokenizedUserInput;

import java.util.Iterator;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Perform testing of the {@link HelpCommand} class.
 */
public class HelpCommandTest {
    private static ActorSystem system = null;
    private static TestActorRef<HelpCommand> actorRef = null;
    private static HelpCommand actor = null;

    /**
     * Initialize the test actor system.
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("test-actor-system", ConfigFactory.load("test-config"));
        actorRef = TestActorRef.create(system, Props.create(HelpCommand.class), "actor");
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

    // simple actor which just forwards messages
    static class Forwarder extends UntypedActor {
        final ActorRef target;

        @SuppressWarnings("unused")
        public Forwarder(ActorRef target) {
            this.target = target;
        }

        public void onReceive(Object msg) {
            target.forward(msg, getContext());
        }
    }

    @Test
    public void testReceiveWithRegistrationRequest() {
        new JavaTestKit(system) {{
            final ActorRef helpCommand = system.actorOf(Props.create(HelpCommand.class), HelpCommand.class.getName());

            helpCommand.tell(new RegistrationRequest.Builder().build(), getRef());

            final RegistrationResponse response = expectMsgClass(RegistrationResponse.class);
            final Set<Registration> registrations = response.getRegistrations();
            assertEquals(1, registrations.size());

            final Registration registration = registrations.iterator().next();
            assertTrue(registration.getDescription().isPresent());
            assertEquals("display usage information for available shell commands", registration.getDescription().get());
            assertEquals(new CommandPath.Builder("help").build(), registration.getPath());
            assertFalse(registration.getOptions().isPresent());

            expectNoMsg();

            helpCommand.tell(PoisonPill.getInstance(), getRef());
        }};
    }

    @Test
    public void testReceiveWithRegistrationResponse() {
        final Option h = new Option("h", "help", true, "description");
        final Option s = new Option("s", null);
        final Option r = new Option("r", "required", false, "required");
        r.setRequired(true);

        final Options options = new Options();
        options.addOption(h);
        options.addOption(s);
        options.addOption(r);

        final CommandPath path = new CommandPath.Builder("command").build();
        final Registration reg = new Registration.Builder(actorRef, path, options, "description").build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).build();

        new JavaTestKit(system) {{
            final ActorRef consoleManager =
                    system.actorOf(Props.create(Forwarder.class, getRef()), ConsoleManager.class.getSimpleName());
            final ActorRef helpCommand = system.actorOf(Props.create(HelpCommand.class), HelpCommand.class.getName());

            helpCommand.tell(response, getRef());

            ConsoleOutput output = expectMsgClass(ConsoleOutput.class);
            assertEquals("Optional[  command  description]", output.getOutput().toString());
            assertTrue(output.hasMore());
            assertFalse(output.isTerminate());

            output = expectMsgClass(ConsoleOutput.class);
            assertEquals("Optional[    -h  --help  description]", output.getOutput().toString());
            assertTrue(output.hasMore());
            assertFalse(output.isTerminate());

            output = expectMsgClass(ConsoleOutput.class);
            assertEquals("Optional[    -s]", output.getOutput().toString());
            assertTrue(output.hasMore());
            assertFalse(output.isTerminate());

            output = expectMsgClass(ConsoleOutput.class);
            assertEquals("Optional[    -r  --required  (required)  required]", output.getOutput().toString());
            assertTrue(output.hasMore());
            assertFalse(output.isTerminate());

            output = expectMsgClass(ConsoleOutput.class);
            assertEquals("Optional.empty", output.getOutput().toString());
            assertFalse(output.hasMore());
            assertFalse(output.isTerminate());

            expectNoMsg();

            helpCommand.tell(PoisonPill.getInstance(), getRef());
            consoleManager.tell(PoisonPill.getInstance(), getRef());
        }};
    }

    @Test
    public void testReceiveWithRegistrationResponseMultipleRegistrations() {
        final Option h = new Option("h", "help", true, "description");
        final Option s = new Option("s", null);
        final Option r = new Option("r", "required", false, "required");
        r.setRequired(true);

        final Options options = new Options();
        options.addOption(h);
        options.addOption(s);
        options.addOption(r);

        final CommandPath pathA = new CommandPath.Builder("command1").build();
        final CommandPath pathB = new CommandPath.Builder("command2").build();

        final Registration withoutOptions = new Registration.Builder(actorRef, pathA).build();
        final Registration withOptions = new Registration.Builder(actorRef, pathB, options).build();

        final RegistrationResponse response = new RegistrationResponse.Builder(withOptions, withoutOptions).build();

        new JavaTestKit(system) {{
            final ActorRef consoleManager =
                    system.actorOf(Props.create(Forwarder.class, getRef()), ConsoleManager.class.getSimpleName());
            final ActorRef helpCommand = system.actorOf(Props.create(HelpCommand.class), HelpCommand.class.getName());

            helpCommand.tell(response, getRef());

            ConsoleOutput output = expectMsgClass(ConsoleOutput.class);
            assertEquals("Optional[  command1]", output.getOutput().toString());
            assertTrue(output.hasMore());
            assertFalse(output.isTerminate());

            output = expectMsgClass(ConsoleOutput.class);
            assertEquals("Optional[  command2]", output.getOutput().toString());
            assertTrue(output.hasMore());
            assertFalse(output.isTerminate());

            output = expectMsgClass(ConsoleOutput.class);
            assertEquals("Optional.empty", output.getOutput().toString());
            assertFalse(output.hasMore());
            assertFalse(output.isTerminate());

            expectNoMsg();

            helpCommand.tell(PoisonPill.getInstance(), getRef());
            consoleManager.tell(PoisonPill.getInstance(), getRef());
        }};
    }

    @Test
    public void testReceiveWithCommand() throws Exception {
        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("help").build();
        final CommandPath path = new CommandPath.Builder("help").build();
        final Registration reg = new Registration.Builder(actorRef, path).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
        final Command command = new Command.Builder(response).build();

        new JavaTestKit(system) {{
            final ActorRef registrationManager =
                    system.actorOf(Props.create(Forwarder.class, getRef()), RegistrationManager.class.getSimpleName());
            final ActorRef helpCommand = system.actorOf(Props.create(HelpCommand.class), HelpCommand.class.getName());

            helpCommand.tell(command, getRef());

            assertNotNull(expectMsgClass(RegistrationRequest.class));

            expectNoMsg();

            helpCommand.tell(PoisonPill.getInstance(), getRef());
            registrationManager.tell(PoisonPill.getInstance(), getRef());
        }};
    }

    @Test
    public void testReceiveWithCommandAndSubCommand() throws Exception {
        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("help command").build();
        final CommandPath path = new CommandPath.Builder("help", "command").build();
        final Registration reg = new Registration.Builder(actorRef, path).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
        final Command command = new Command.Builder(response).build();

        new JavaTestKit(system) {{
            final ActorRef registrationManager =
                    system.actorOf(Props.create(Forwarder.class, getRef()), RegistrationManager.class.getSimpleName());
            final ActorRef helpCommand = system.actorOf(Props.create(HelpCommand.class), HelpCommand.class.getName());

            helpCommand.tell(command, getRef());

            final RegistrationLookup lookup = expectMsgClass(RegistrationLookup.class);
            assertEquals("[command]", lookup.getPaths().toString());
            assertEquals("Optional.empty", lookup.getUserInput().toString());

            expectNoMsg();

            helpCommand.tell(PoisonPill.getInstance(), getRef());
            registrationManager.tell(PoisonPill.getInstance(), getRef());
        }};
    }

    @Test
    public void testReceiveWithUnhandled() {
        final Option h = new Option("h", "help", true, "description");
        final Option s = new Option("s", null);
        final Option r = new Option("r", "required", false, "required");
        r.setRequired(true);

        final Options options = new Options();
        options.addOption(h);
        options.addOption(s);
        options.addOption(r);

        final CommandPath pathA = new CommandPath.Builder("command1").build();
        final CommandPath pathB = new CommandPath.Builder("command2").build();

        final Registration withoutOptions = new Registration.Builder(actorRef, pathA).build();
        final Registration withOptions = new Registration.Builder(actorRef, pathB, options).build();

        final RegistrationResponse response = new RegistrationResponse.Builder(withOptions, withoutOptions).build();

        new JavaTestKit(system) {{
            final ActorRef helpCommand = system.actorOf(Props.create(HelpCommand.class), HelpCommand.class.getName());

            helpCommand.tell("unhandled", getRef());

            expectNoMsg();

            helpCommand.tell(PoisonPill.getInstance(), getRef());
        }};
    }

    @Test
    public void testGetOutput() {
        final Option h = new Option("h", "help", true, "description");
        final Option s = new Option("s", null);
        final Option r = new Option("r", "required", false, "required");
        r.setRequired(true);

        final Options options = new Options();
        options.addOption(h);
        options.addOption(s);
        options.addOption(r);

        final CommandPath commandPath = new CommandPath.Builder("help").build();

        final Registration withoutOptions = new Registration.Builder(actorRef, commandPath).build();
        Iterator<ConsoleOutput> iter = actor.getOutput(withoutOptions, false, OptionalInt.of(6)).iterator();
        assertTrue(iter.hasNext());

        ConsoleOutput output = iter.next();
        assertEquals("Optional[  help]", output.getOutput().toString());
        assertTrue(output.hasMore());
        assertFalse(iter.hasNext());

        final Registration withOptions = new Registration.Builder(actorRef, commandPath, options).build();
        iter = actor.getOutput(withOptions, true, OptionalInt.of(6)).iterator();
        assertTrue(iter.hasNext());

        output = iter.next();
        assertEquals("Optional[  help]", output.getOutput().toString());
        assertTrue(output.hasMore());

        output = iter.next();
        assertEquals("Optional[    -h  --help  description]", output.getOutput().toString());
        assertTrue(output.hasMore());

        output = iter.next();
        assertEquals("Optional[    -s]", output.getOutput().toString());
        assertTrue(output.hasMore());

        output = iter.next();
        assertEquals("Optional[    -r  --required  (required)  required]", output.getOutput().toString());
        assertTrue(output.hasMore());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testGetDescription() {
        final CommandPath commandPath = new CommandPath.Builder("help").build();
        final Registration without = new Registration.Builder(actorRef, commandPath).build();
        final Registration with = new Registration.Builder(actorRef, commandPath).setDescription("desc").build();

        assertEquals("  help", actor.getDescription(without, OptionalInt.of(6)));
        assertEquals("  help    desc", actor.getDescription(with, OptionalInt.of(6)));
    }

    @Test
    public void testGetOptions() {
        final Option h = new Option("h", "help", true, "description");
        final Option s = new Option("s", null);
        final Option r = new Option("r", "required", false, "required");
        r.setRequired(true);

        final Options options = new Options();
        options.addOption(h);
        options.addOption(s);
        options.addOption(r);

        final CommandPath commandPath = new CommandPath.Builder("help").build();

        final Registration withoutOptions = new Registration.Builder(actorRef, commandPath).build();
        assertTrue(actor.getOptions(withoutOptions).isEmpty());

        final Registration withOptions = new Registration.Builder(actorRef, commandPath, options).build();
        final Iterator<ConsoleOutput> iter = actor.getOptions(withOptions).iterator();
        assertTrue(iter.hasNext());

        ConsoleOutput output = iter.next();
        assertEquals("Optional[    -h  --help  description]", output.getOutput().toString());
        assertTrue(output.hasMore());

        output = iter.next();
        assertEquals("Optional[    -s]", output.getOutput().toString());
        assertTrue(output.hasMore());

        output = iter.next();
        assertEquals("Optional[    -r  --required  (required)  required]", output.getOutput().toString());
        assertTrue(output.hasMore());

        assertFalse(iter.hasNext());
    }
}
