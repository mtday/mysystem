package mysystem.shell.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.ConfigFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;
import mysystem.shell.model.TokenizedUserInput;

import java.util.Iterator;
import java.util.Set;

/**
 * Perform testing of the {@link ExitCommand} class.
 */
public class ExitCommandTest {
    private static ActorSystem system = null;

    /**
     * Initialize the test actor system.
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("test-actor-system", ConfigFactory.load("test-config"));
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
            final ActorRef exitCommand =
                    system.actorOf(Props.create(ExitCommand.class), ExitCommand.class.getSimpleName());

            exitCommand.tell(new RegistrationRequest.Builder().build(), getRef());

            final RegistrationResponse response = expectMsgClass(RegistrationResponse.class);
            final Set<Registration> registrations = response.getRegistrations();
            assertEquals(2, registrations.size());

            final Iterator<Registration> iter = registrations.iterator();
            final Registration exit = iter.next();
            assertTrue(exit.getDescription().isPresent());
            assertEquals("exit the shell", exit.getDescription().get());
            assertEquals(new CommandPath.Builder("exit").build(), exit.getPath());
            assertFalse(exit.getOptions().isPresent());

            final Registration quit = iter.next();
            assertTrue(quit.getDescription().isPresent());
            assertEquals("exit the shell", quit.getDescription().get());
            assertEquals(new CommandPath.Builder("quit").build(), quit.getPath());
            assertFalse(quit.getOptions().isPresent());

            expectNoMsg();

            exitCommand.tell(PoisonPill.getInstance(), getRef());
        }};
    }

    @Test
    public void testReceiveWithCommand() throws Exception {
        new JavaTestKit(system) {{
            final TokenizedUserInput userInput = new TokenizedUserInput.Builder("exit").build();
            final CommandPath path = new CommandPath.Builder("exit").build();
            final Registration reg = new Registration.Builder(getRef(), path).build();
            final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
            final Command command = new Command.Builder(response).build();

            final ActorRef exitCommand =
                    system.actorOf(Props.create(ExitCommand.class), ExitCommand.class.getSimpleName());

            exitCommand.tell(command, getRef());

            final ConsoleOutput output = expectMsgClass(ConsoleOutput.class);
            assertNotNull(output);
            assertTrue(output.getOutput().isPresent());
            assertEquals("Disconnecting", output.getOutput().get());
            assertFalse(output.hasMore());
            assertTrue(output.isTerminate());

            expectNoMsg();

            exitCommand.tell(PoisonPill.getInstance(), getRef());
        }};
    }

    @Test
    public void testReceiveWithUnhandled() {
        new JavaTestKit(system) {{
            final ActorRef exitCommand =
                    system.actorOf(Props.create(ExitCommand.class), ExitCommand.class.getSimpleName());

            exitCommand.tell("unhandled", getRef());

            expectNoMsg();

            exitCommand.tell(PoisonPill.getInstance(), getRef());
        }};
    }
}
