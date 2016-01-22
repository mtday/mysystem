package mysystem.shell.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import mysystem.shell.Forwarder;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.InvalidInput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationLookup;
import mysystem.shell.model.RegistrationResponse;
import mysystem.shell.model.TokenizedUserInput;
import mysystem.shell.model.UnrecognizedCommand;
import mysystem.shell.model.UserInput;

/**
 * Perform testing of the {@link RegistrationFinder} class.
 */
public class RegistrationFinderTest {
    private static ActorSystem system = null;
    private static TestActorRef<RegistrationFinder> actorRef = null;
    private static RegistrationFinder actor = null;

    /**
     * Initialize the test actor system.
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("test-actor-system", ConfigFactory.load("test-config"));
        actorRef = TestActorRef.create(system, Props.create(RegistrationFinder.class), "actor");
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
    public void testReceiveWithTokenizedUserInput() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef registrationManager =
                    system.actorOf(Props.create(Forwarder.class, getRef()), RegistrationManager.class.getSimpleName());
            final ActorRef regfinder = RegistrationFinder.create(system);

            try {
                final TokenizedUserInput input = new TokenizedUserInput.Builder("input").build();

                regfinder.tell(input, getRef());

                final RegistrationLookup lookup = expectMsgClass(duration("500 ms"), RegistrationLookup.class);
                assertTrue(lookup.getUserInput().isPresent());
                assertEquals(input, lookup.getUserInput().get());
                assertEquals(1, lookup.getPaths().size());
                assertEquals(new CommandPath.Builder("input").build(), lookup.getPaths().iterator().next());
            } finally {
                regfinder.tell(PoisonPill.getInstance(), getRef());
                registrationManager.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithRegistrationResponseNoRegistrations() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef consoleManager =
                    system.actorOf(Props.create(Forwarder.class, getRef()), ConsoleManager.class.getSimpleName());
            final ActorRef regfinder = RegistrationFinder.create(system);

            try {
                final UserInput userInput = new UserInput.Builder("input").build();
                final TokenizedUserInput tokenized = new TokenizedUserInput.Builder(userInput).build();
                final RegistrationResponse response = new RegistrationResponse.Builder().setUserInput(tokenized).build();

                regfinder.tell(response, getRef());

                final UnrecognizedCommand output = expectMsgClass(duration("500 ms"), UnrecognizedCommand.class);
                assertEquals(tokenized, output.getUserInput());
            } finally {
                regfinder.tell(PoisonPill.getInstance(), getRef());
                consoleManager.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithRegistrationResponseMultipleRegistrations() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef inputTokenizer =
                    system.actorOf(Props.create(Forwarder.class, getRef()), InputTokenizer.class.getSimpleName());
            final ActorRef regfinder = RegistrationFinder.create(system);

            try {
                final CommandPath exitPath = new CommandPath.Builder("exit").build();
                final Registration exit = new Registration.Builder(getRef(), exitPath).build();
                final CommandPath quitPath = new CommandPath.Builder("quit").build();
                final Registration quit = new Registration.Builder(getRef(), quitPath).build();
                final CommandPath helpPath = new CommandPath.Builder("help").build();
                final Registration help = new Registration.Builder(getRef(), helpPath).build();

                final UserInput userInput = new UserInput.Builder("input").build();
                final TokenizedUserInput tokenized = new TokenizedUserInput.Builder(userInput).build();
                final RegistrationResponse response = new RegistrationResponse.Builder(exit, quit, help).setUserInput(tokenized).build();

                regfinder.tell(response, getRef());

                final UserInput output = expectMsgClass(duration("500 ms"), UserInput.class);
                assertEquals("help input", output.getInput());
            } finally {
                regfinder.tell(PoisonPill.getInstance(), getRef());
                inputTokenizer.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithRegistrationResponseSingleRegistration() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef executor =
                    system.actorOf(Props.create(Forwarder.class, getRef()), CommandExecutor.class.getSimpleName());
            final ActorRef regfinder = RegistrationFinder.create(system);

            try {
                final CommandPath exitPath = new CommandPath.Builder("exit").build();
                final Registration exit = new Registration.Builder(getRef(), exitPath).build();

                final UserInput userInput = new UserInput.Builder("exit").build();
                final TokenizedUserInput tokenized = new TokenizedUserInput.Builder(userInput).build();
                final RegistrationResponse response = new RegistrationResponse.Builder(exit).setUserInput(tokenized).build();

                regfinder.tell(response, getRef());

                final Command command = expectMsgClass(duration("500 ms"), Command.class);
                assertEquals(exitPath, command.getCommandPath());
                assertEquals(exit, command.getRegistration());
                assertEquals(tokenized, command.getUserInput());
                assertFalse(command.getCommandLine().isPresent());
            } finally {
                regfinder.tell(PoisonPill.getInstance(), getRef());
                executor.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithRegistrationResponseSingleRegistrationInvalidParams() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef consoleManager =
                    system.actorOf(Props.create(Forwarder.class, getRef()), ConsoleManager.class.getSimpleName());
            final ActorRef regfinder = RegistrationFinder.create(system);

            try {
                final Options options = new Options();
                final Option option = new Option("r", "required", false, "required field");
                option.setRequired(true);
                options.addOption(option);

                final CommandPath exitPath = new CommandPath.Builder("exit").build();
                final Registration exit = new Registration.Builder(getRef(), exitPath, options).build();

                final UserInput userInput = new UserInput.Builder("exit").build();
                final TokenizedUserInput tokenized = new TokenizedUserInput.Builder(userInput).build();
                final RegistrationResponse response = new RegistrationResponse.Builder(exit).setUserInput(tokenized).build();

                regfinder.tell(response, getRef());

                final InvalidInput command = expectMsgClass(duration("500 ms"), InvalidInput.class);
                assertEquals("Missing required option: r", command.getError());
                assertEquals(userInput, command.getUserInput());
                assertFalse(command.getLocation().isPresent());
            } finally {
                regfinder.tell(PoisonPill.getInstance(), getRef());
                consoleManager.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveUnhandled() {
        new JavaTestKit(system) {{
            final ActorRef regfinder = RegistrationFinder.create(system);

            try {
                regfinder.tell("unhandled", getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                regfinder.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }
}
