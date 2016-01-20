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
import mysystem.shell.model.InvalidInput;
import mysystem.shell.model.TokenizedUserInput;
import mysystem.shell.model.UserInput;

/**
 * Perform testing of the {@link InputTokenizer} class.
 */
public class InputTokenizerTest {
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
    public void testReceiveWithUserInput() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef finder =
                    system.actorOf(Props.create(Forwarder.class, getRef()), RegistrationFinder.class.getSimpleName());
            final ActorRef inputTokenizer = InputTokenizer.create(system);

            try {
                final UserInput userInput = new UserInput.Builder("input").build();
                inputTokenizer.tell(userInput, getRef());

                final TokenizedUserInput expected = new TokenizedUserInput.Builder(userInput).build();
                final TokenizedUserInput response = expectMsgClass(TokenizedUserInput.class);
                assertEquals(expected, response);
            } finally {
                inputTokenizer.tell(PoisonPill.getInstance(), getRef());
                finder.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithUserInputInvalid() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef consoleManager =
                    system.actorOf(Props.create(Forwarder.class, getRef()), ConsoleManager.class.getSimpleName());
            final ActorRef inputTokenizer = InputTokenizer.create(system);

            try {
                final UserInput userInput = new UserInput.Builder("invalid '").build();
                inputTokenizer.tell(userInput, getRef());

                expectMsgClass(InvalidInput.class);
            } finally {
                inputTokenizer.tell(PoisonPill.getInstance(), getRef());
                consoleManager.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithUnhandled() {
        new JavaTestKit(system) {{
            final ActorRef inputTokenizer = InputTokenizer.create(system);

            try {
                inputTokenizer.tell("unhandled", getRef());

                expectNoMsg();
            } finally {
                inputTokenizer.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testGetActorSelection() {
        final ActorSelection expected = system.actorSelection("/user/" + InputTokenizer.class.getSimpleName());
        assertEquals(expected, InputTokenizer.getActorSelection(system));
    }
}
