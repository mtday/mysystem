package mysystem.shell.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.ConfigFactory;

import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import jline.Terminal;
import jline.console.ConsoleReader;
import mysystem.shell.model.AcceptInput;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.InvalidInput;
import mysystem.shell.model.Terminate;
import mysystem.shell.model.TokenizedUserInput;
import mysystem.shell.model.UnrecognizedCommand;
import mysystem.shell.model.UserInput;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Perform testing of the {@link ConsoleManager} class.
 */
public class ConsoleManagerTest {
    @Test
    public void testReceiveWithConsoleOutput() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final ActorSystem system = ActorSystem.create("console-output", ConfigFactory.load("test-config"));

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                consoleManager.tell(new ConsoleOutput.Builder("output").build(), getRef());

                expectMsgAnyClassOf(AcceptInput.class, Terminate.class, Terminated.class);

                final List<String> outputLines = consoleReader.getOutputLines();
                assertEquals(3, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("output", iter.next());
                assertEquals("\n", iter.next());
                assertEquals("\n", iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveWithConsoleOutputHasMore() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final ActorSystem system = ActorSystem.create("console-output-more", ConfigFactory.load("test-config"));

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                consoleManager.tell(new ConsoleOutput.Builder("output").setHasMore(true).build(), getRef());

                expectMsgAnyClassOf(AcceptInput.class, Terminate.class, Terminated.class);

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(2, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertEquals("output", iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveWithConsoleOutputTerminate() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader("line");
        final ActorSystem system = ActorSystem.create("console-output-terminate", ConfigFactory.load("test-config"));

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "c");
            watch(consoleManager);

            try {
                consoleManager.tell(new ConsoleOutput.Builder("a").setHasMore(true).build(), getRef());
                consoleManager.tell(new ConsoleOutput.Builder("b").setTerminate(true).build(), getRef());

                expectMsgAnyClassOf(AcceptInput.class, Terminate.class, Terminated.class);

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveWithConsoleOutputEmpty() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final ActorSystem system = ActorSystem.create("console-output-empty", ConfigFactory.load("test-config"));

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                consoleManager.tell(new ConsoleOutput.Builder().build(), getRef());

                expectMsgAnyClassOf(AcceptInput.class, Terminate.class, Terminated.class);

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(1, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveWithUnrecognizedCommand() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader("line");
        final ActorSystem system = ActorSystem.create("unrecognized-command", ConfigFactory.load("test-config"));

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                final UserInput userInput = new UserInput.Builder("input").build();
                final TokenizedUserInput tokenized = new TokenizedUserInput.Builder(userInput).build();
                consoleManager.tell(new UnrecognizedCommand.Builder(tokenized).build(), getRef());

                expectMsgAnyClassOf(AcceptInput.class, Terminated.class);

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(3, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertEquals("The specified command was not recognized: input", iter.next());
                assertEquals("Use 'help' to see all the available commands.", iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveWithInvalidInputWithLocation() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final ActorSystem system = ActorSystem.create("invalid-input-a", ConfigFactory.load("test-config"));

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                final UserInput userInput = new UserInput.Builder("input").build();
                final ParseException parseException = new ParseException("message", 10);
                consoleManager.tell(new InvalidInput.Builder(userInput, parseException).build(), getRef());

                expectMsgAnyClassOf(AcceptInput.class, Terminate.class, Terminated.class);

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(3, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertEquals("---------^", iter.next());
                assertEquals("message", iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveWithInvalidInputWithoutLocation() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final ActorSystem system = ActorSystem.create("invalid-input-b", ConfigFactory.load("test-config"));

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                final UserInput userInput = new UserInput.Builder("input").build();
                final ParseException parseException = new ParseException("message", -1);
                consoleManager.tell(new InvalidInput.Builder(userInput, parseException).build(), getRef());

                expectMsgAnyClassOf(AcceptInput.class, Terminate.class, Terminated.class);

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(2, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertEquals("message", iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveWithTerminate() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final ActorSystem system = ActorSystem.create("terminate", ConfigFactory.load("test-config"));

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "actor");
            try {
                consoleManager.tell(new Terminate.Builder().build(), getRef());

                expectNoMsg();

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testReceiveWithString() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final ActorSystem system = ActorSystem.create("unhandled", ConfigFactory.load("test-config"));

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "actor");
            try {
                consoleManager.tell("unhandled", getRef());

                expectNoMsg();
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.shutdown();
            }
        }};
    }

    @Test
    public void testGetActorSelection() {
        final ActorSystem system = ActorSystem.create("selection", ConfigFactory.load("test-config"));
        try {
            final ActorSelection expected = system.actorSelection("/user/" + ConsoleManager.class.getSimpleName());
            assertEquals(expected, ConsoleManager.getActorSelection(system));
        } finally {
            system.shutdown();
        }
    }

    @Test
    public void testHandleTerminate() throws IOException {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader("line");
        final ActorSystem system = ActorSystem.create("terminate-actor-system", ConfigFactory.load("test-config"));
        final TestActorRef<ConsoleManager> actorRef =
                TestActorRef.create(system, Props.create(ConsoleManager.class, consoleReader), "actor");

        final ConsoleManager consoleManager = actorRef.underlyingActor();
        consoleManager.handleTerminate();

        assertTrue(consoleReader.isShutdown());

        actorRef.tell(PoisonPill.getInstance(), ActorRef.noSender());
        system.shutdown();
    }

    private static class CapturingConsoleReader extends ConsoleReader {
        private final List<String> output = new LinkedList<>();
        private final List<String> lines = new LinkedList<>();

        private boolean shutdown = false;

        public CapturingConsoleReader() throws IOException {
            super();
            super.shutdown();
        }

        public CapturingConsoleReader(final String... lines) throws IOException {
            if (lines != null) {
                this.lines.addAll(Arrays.asList(lines));
            }
            super.shutdown();
        }

        @Override
        public String readLine() throws IOException {
            if (lines.isEmpty()) {
                return null;
            }
            return lines.remove(0);
        }

        @Override
        public void print(final CharSequence s) {
            if (s != null) {
                this.output.add(s.toString());
            }
        }

        @Override
        public void println() {
            this.output.add("\n");
        }

        @Override
        public void println(final CharSequence line) {
            if (line != null) {
                this.output.add(line.toString());
            }
        }

        @Override
        public void shutdown() {
            this.shutdown = true;
        }

        @Override
        public Terminal getTerminal() {
            return Mockito.mock(Terminal.class);
        }

        public boolean isShutdown() {
            return this.shutdown;
        }

        public List<String> getOutputLines() {
            return this.output;
        }
    }
}
