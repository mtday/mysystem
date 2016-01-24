package mysystem.shell.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Option;
import mysystem.shell.model.Options;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;
import mysystem.shell.model.TokenizedUserInput;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Perform testing of the {@link ConfigCommand} class.
 */
public class ConfigCommandTest {
    private static ActorSystem system = null;
    private static TestActorRef<ConfigCommand> actorRef = null;
    private static ConfigCommand actor = null;

    /**
     * Initialize the test actor system.
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("test-actor-system", ConfigFactory.load("test-config"));
        actorRef = TestActorRef.create(system, Props.create(ConfigCommand.class), "actor");
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
    public void testReceiveWithRegistrationRequest() {
        new JavaTestKit(system) {{
            final ActorRef configCommand =
                    system.actorOf(Props.create(ConfigCommand.class), ConfigCommand.class.getSimpleName());

            try {
                configCommand.tell(new RegistrationRequest.Builder().build(), getRef());

                final RegistrationResponse response = expectMsgClass(duration("500 ms"), RegistrationResponse.class);
                final Set<Registration> registrations = response.getRegistrations();
                assertEquals(1, registrations.size());

                final Registration registration = registrations.iterator().next();
                assertTrue(registration.getDescription().isPresent());
                assertEquals("provides information about the system configuration",
                        registration.getDescription().get());
                assertEquals(new CommandPath.Builder("config").build(), registration.getPath());
                assertTrue(registration.getOptions().isPresent());

                expectNoMsg(duration("100 ms"));
            } finally {
                configCommand.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithCommand() throws Exception {
        new JavaTestKit(system) {{
            final TokenizedUserInput userInput = new TokenizedUserInput.Builder("config").build();
            final CommandPath path = new CommandPath.Builder("config").build();
            final Registration reg = new Registration.Builder().setActorPath(getRef()).setPath(path).build();
            final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
            final Command command = new Command.Builder(response).build();

            final ActorRef configCommand =
                    system.actorOf(Props.create(ConfigCommand.class), ConfigCommand.class.getSimpleName());

            try {
                configCommand.tell(command, getRef());

                final ConsoleOutput output = expectMsgClass(duration("500 ms"), ConsoleOutput.class);
                assertNotNull(output);
                assertTrue(output.getOutput().isPresent());
                assertEquals("  akka.actor.creation-timeout => 20s", output.getOutput().get());
                assertTrue(output.hasMore());
                assertFalse(output.isTerminate());

                // The rest of the configuration information is ignored.
            } finally {
                configCommand.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithUnhandled() {
        new JavaTestKit(system) {{
            final ActorRef configCommand =
                    system.actorOf(Props.create(ConfigCommand.class), ConfigCommand.class.getSimpleName());

            try {
                configCommand.tell("unhandled", getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                configCommand.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testGetFilterPatternWithRegex() throws Exception {
        final Option option = new Option.Builder().setDescription("filter").setShortOption("f").setLongOption("filter")
                .setArguments(1).build();
        final Options options = new Options.Builder(option).build();

        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("config -f 'abc.*'").build();
        final CommandPath path = new CommandPath.Builder("config").build();
        final Registration reg =
                new Registration.Builder().setActorPath(actorRef).setPath(path).setOptions(options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
        final Command command = new Command.Builder(response).build();

        final Optional<Pattern> pattern = actor.getFilterPattern(command);
        assertTrue(pattern.isPresent());
        assertEquals("abc.*", pattern.get().toString());
    }

    @Test
    public void testGetFilterPatternWithNonRegex() throws Exception {
        final Option option = new Option.Builder().setDescription("filter").setShortOption("f").setLongOption("filter")
                .setArguments(1).build();
        final Options options = new Options.Builder(option).build();

        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("config -f 'abc'").build();
        final CommandPath path = new CommandPath.Builder("config").build();
        final Registration reg =
                new Registration.Builder().setActorPath(actorRef).setPath(path).setOptions(options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
        final Command command = new Command.Builder(response).build();

        final Optional<Pattern> pattern = actor.getFilterPattern(command);
        assertTrue(pattern.isPresent());
        assertEquals(".*abc.*", pattern.get().toString());
    }

    @Test
    public void testGetFilterPatternWithEmptyRegex() throws Exception {
        final Option option = new Option.Builder().setDescription("filter").setShortOption("f").setLongOption("filter")
                .setArguments(1).build();
        final Options options = new Options.Builder(option).build();

        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("config -f ''").build();
        final CommandPath path = new CommandPath.Builder("config").build();
        final Registration reg =
                new Registration.Builder().setActorPath(actorRef).setPath(path).setOptions(options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
        final Command command = new Command.Builder(response).build();

        final Optional<Pattern> pattern = actor.getFilterPattern(command);
        assertFalse(pattern.isPresent());
    }

    @Test
    public void testGetFilterPatternWithNoOption() throws Exception {
        final Option option = new Option.Builder().setDescription("filter").setShortOption("f").setLongOption("filter")
                .setArguments(1).build();
        final Options options = new Options.Builder(option).build();

        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("config").build();
        final CommandPath path = new CommandPath.Builder("config").build();
        final Registration reg =
                new Registration.Builder().setActorPath(actorRef).setPath(path).setOptions(options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
        final Command command = new Command.Builder(response).build();

        final Optional<Pattern> pattern = actor.getFilterPattern(command);
        assertFalse(pattern.isPresent());
    }

    @Test
    public void testAccept() {
        final ConfigValue value = ConfigValueFactory.fromAnyRef("my configuration value");
        Map.Entry<String, ConfigValue> entry = new AbstractMap.SimpleEntry<>("akka.whatever.key", value);

        final Optional<Pattern> empty = Optional.empty();
        final Optional<Pattern> keyRegex = Optional.of(Pattern.compile("^akka.*"));
        final Optional<Pattern> valueMatch = Optional.of(Pattern.compile("my configuration value"));
        final Optional<Pattern> noMatch = Optional.of(Pattern.compile("non-existent"));

        assertTrue(actor.accept(entry, empty));
        assertTrue(actor.accept(entry, keyRegex));
        assertTrue(actor.accept(entry, valueMatch));
        assertFalse(actor.accept(entry, noMatch));
    }
}
