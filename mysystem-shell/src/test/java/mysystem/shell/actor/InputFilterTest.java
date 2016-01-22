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
import mysystem.shell.model.AcceptInput;
import mysystem.shell.model.UserInput;

/**
 * Perform testing of the {@link InputFilter} class.
 */
public class InputFilterTest {
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
            final ActorRef tokenizer =
                    system.actorOf(Props.create(Forwarder.class, getRef()), InputTokenizer.class.getSimpleName());
            final ActorRef inputFilter = InputFilter.create(system);

            try {
                final UserInput userInput = new UserInput.Builder("input").build();
                inputFilter.tell(userInput, getRef());

                final UserInput response = expectMsgClass(duration("500 ms"), UserInput.class);
                assertEquals(userInput, response);
            } finally {
                inputFilter.tell(PoisonPill.getInstance(), getRef());
                tokenizer.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithUserInputEmpty() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef tokenizer =
                    system.actorOf(Props.create(Forwarder.class, getRef()), InputTokenizer.class.getSimpleName());
            final ActorRef inputFilter = InputFilter.create(system);

            try {
                final UserInput userInput = new UserInput.Builder(" ").build();
                inputFilter.tell(userInput, getRef());

                expectMsgClass(duration("500 ms"), AcceptInput.class);
            } finally {
                inputFilter.tell(PoisonPill.getInstance(), getRef());
                tokenizer.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithUserInputComment() throws Exception {
        new JavaTestKit(system) {{
            final ActorRef tokenizer =
                    system.actorOf(Props.create(Forwarder.class, getRef()), InputTokenizer.class.getSimpleName());
            final ActorRef inputFilter = InputFilter.create(system);

            try {
                final UserInput userInput = new UserInput.Builder("# comment ").build();
                inputFilter.tell(userInput, getRef());

                expectMsgClass(duration("500 ms"), AcceptInput.class);
            } finally {
                inputFilter.tell(PoisonPill.getInstance(), getRef());
                tokenizer.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testReceiveWithUnhandled() {
        new JavaTestKit(system) {{
            final ActorRef inputFilter = InputFilter.create(system);

            try {
                inputFilter.tell("unhandled", getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                inputFilter.tell(PoisonPill.getInstance(), getRef());
            }
        }};
    }

    @Test
    public void testGetActorSelection() {
        final ActorSelection expected = system.actorSelection("/user/" + InputFilter.class.getSimpleName());
        assertEquals(expected, InputFilter.getActorSelection(system));
    }
}
