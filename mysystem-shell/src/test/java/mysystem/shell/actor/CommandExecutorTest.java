package mysystem.shell.actor;

import static org.junit.Assert.assertEquals;

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
import mysystem.shell.Forwarder;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationResponse;
import mysystem.shell.model.TokenizedUserInput;

/**
 * Perform testing of the {@link CommandExecutor} class.
 */
public class CommandExecutorTest {
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
    public void testReceiveWithCommand() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef cmdExec = CommandExecutor.create(system);

            try {
                final TokenizedUserInput userInput = new TokenizedUserInput.Builder("exit").build();
                final CommandPath path = new CommandPath.Builder("exit").build();
                final Registration reg = new Registration.Builder(getRef(), path).build();
                final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(userInput).build();
                final Command command = new Command.Builder(response).build();

                cmdExec.tell(command, getRef());

                final Command message = expectMsgClass(duration("500 ms"), Command.class);
                assertEquals(command, message);
            } finally {
                cmdExec.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithString() {
        new JavaTestKit(system) {{
            final ActorRef consoleManager =
                    system.actorOf(Props.create(Forwarder.class, getRef()), ConsoleManager.class.getSimpleName());
            final ActorRef cmdExec = CommandExecutor.create(system);

            try {
                cmdExec.tell("unhandled", getRef());

                final String message = expectMsgClass(duration("500 ms"), String.class);
                assertEquals("unhandled", message);
            } finally {
                cmdExec.tell(PoisonPill.getInstance(), getRef());
                consoleManager.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testGetActorSelection() {
        final ActorSelection expected = system.actorSelection("/user/" + CommandExecutor.class.getSimpleName());
        assertEquals(expected, CommandExecutor.getActorSelection(system));
    }
}
