package mysystem.shell.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

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
import mysystem.common.config.CommonConfig;
import mysystem.shell.model.AcceptInput;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.InvalidInput;
import mysystem.shell.model.Terminate;
import mysystem.shell.model.TokenizedUserInput;
import mysystem.shell.model.UnrecognizedCommand;
import mysystem.shell.model.UserInput;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Perform testing of the {@link ConsoleManager} class.
 */
public class ConsoleManagerTest {
    private Class<?>[] getExpectedClasses() {
        final List<Class<?>> list = new ArrayList<>(3);
        list.add(AcceptInput.class);
        list.add(Terminate.class);
        list.add(Terminated.class);
        return list.toArray(new Class[3]);
    }

    private String getSystemAndVersion(final Config config) {
        String systemName = "";
        if (config.hasPath(CommonConfig.ACTOR_SYSTEM_NAME.getKey())) {
            systemName = config.getString(CommonConfig.ACTOR_SYSTEM_NAME.getKey());
        }
        String version = "";
        if (config.hasPath(CommonConfig.VERSION.getKey())) {
            version = config.getString(CommonConfig.VERSION.getKey());
        }
        return String.format("%s %s", systemName, version);
    }

    @Test
    public void testCreate() throws Exception {
        final ActorSystem system = ActorSystem.create("console-output", ConfigFactory.load("test-config"));
        final ActorRef ref = ConsoleManager.create(system);
        try {
            ref.tell(new Terminate.Builder().build(), ActorRef.noSender());
        } finally {
            system.terminate();
        }
    }

    @Test
    public void testPreStart() throws Exception {
        testPreStartWithSystemNameAndVersion(Optional.of("system-name"), Optional.empty());
        testPreStartWithSystemNameAndVersion(Optional.empty(), Optional.of("version"));
        testPreStartWithSystemNameAndVersion(Optional.of("system-name"), Optional.of("version"));
    }

    public void testPreStartWithSystemNameAndVersion(final Optional<String> systemName, final Optional<String> version) throws Exception {
        final Map<String, ConfigValue> map = new HashMap<>();
        if (systemName.isPresent()) {
            map.put(CommonConfig.ACTOR_SYSTEM_NAME.getKey(), ConfigValueFactory.fromAnyRef(systemName.get()));
        }
        if (version.isPresent()) {
            map.put(CommonConfig.VERSION.getKey(), ConfigValueFactory.fromAnyRef(version.get()));
        }
        final Config config = ConfigFactory.parseMap(map);

        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final ActorSystem system = ActorSystem.create("console-output-" + new Random().nextInt(), config);

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                consoleManager.tell(new Terminate.Builder().build(), getRef());

                expectMsgAnyClassOf(duration("500 ms"), getExpectedClasses());

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                if (systemName.isPresent() && version.isPresent()) {
                    assertEquals(3, outputLines.size());

                    final Iterator<String> iter = outputLines.iterator();
                    assertEquals("\n", iter.next());
                    assertEquals("Type 'help' to list the available commands", iter.next());
                    assertEquals("system-name version", iter.next());
                    assertFalse(iter.hasNext());
                } else {
                    assertEquals(1, outputLines.size());
                    assertTrue(outputLines.contains("\n"));
                }

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveWithConsoleOutput() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final Config config = ConfigFactory.load("test-config");
        final ActorSystem system = ActorSystem.create("console-output", config);

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                consoleManager.tell(new ConsoleOutput.Builder("output").build(), getRef());

                expectMsgAnyClassOf(duration("500 ms"), getExpectedClasses());

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(4, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertEquals("Type 'help' to list the available commands", iter.next());
                assertEquals(getSystemAndVersion(config), iter.next());
                assertEquals("output", iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveWithConsoleOutputHasMore() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final Config config = ConfigFactory.load("test-config");
        final ActorSystem system = ActorSystem.create("console-output-more", ConfigFactory.load("test-config"));

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                consoleManager.tell(new ConsoleOutput.Builder("output").setHasMore(true).build(), getRef());

                expectMsgAnyClassOf(duration("500 ms"), getExpectedClasses());

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(4, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertEquals("Type 'help' to list the available commands", iter.next());
                assertEquals(getSystemAndVersion(config), iter.next());
                assertEquals("output", iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
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

                expectMsgAnyClassOf(duration("500 ms"), getExpectedClasses());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveWithConsoleOutputEmpty() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final Config config = ConfigFactory.load("test-config");
        final ActorSystem system = ActorSystem.create("console-output-empty", config);

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                consoleManager.tell(new ConsoleOutput.Builder().build(), getRef());

                expectMsgAnyClassOf(duration("500 ms"), getExpectedClasses());

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(3, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertEquals("Type 'help' to list the available commands", iter.next());
                assertEquals(getSystemAndVersion(config), iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveWithUnrecognizedCommand() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader("line");
        final Config config = ConfigFactory.load("test-config");
        final ActorSystem system = ActorSystem.create("unrecognized-command", config);

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                final UserInput userInput = new UserInput.Builder("input").build();
                final TokenizedUserInput tokenized = new TokenizedUserInput.Builder(userInput).build();
                consoleManager.tell(new UnrecognizedCommand.Builder(tokenized).build(), getRef());

                expectMsgAnyClassOf(duration("500 ms"), getExpectedClasses());

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(5, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertEquals("The specified command was not recognized: input", iter.next());
                assertEquals("Type 'help' to list the available commands", iter.next());
                assertEquals("Use 'help' to see all the available commands.", iter.next());
                assertEquals(getSystemAndVersion(config), iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveWithInvalidInputWithLocation() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final Config config = ConfigFactory.load("test-config");
        final ActorSystem system = ActorSystem.create("invalid-input-a", config);

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                final UserInput userInput = new UserInput.Builder("input").build();
                final ParseException parseException = new ParseException("message", 10);
                consoleManager.tell(new InvalidInput.Builder(userInput, parseException).build(), getRef());

                expectMsgAnyClassOf(duration("500 ms"), getExpectedClasses());

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(5, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertEquals("---------^", iter.next());
                assertEquals("Type 'help' to list the available commands", iter.next());
                assertEquals("message", iter.next());
                assertEquals(getSystemAndVersion(config), iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }

    @Test
    public void testReceiveWithInvalidInputWithoutLocation() throws Exception {
        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final Config config = ConfigFactory.load("test-config");
        final ActorSystem system = ActorSystem.create("invalid-input-b", config);

        new JavaTestKit(system) {{
            final ActorRef consoleManager = system.actorOf(Props.create(ConsoleManager.class, consoleReader), "cm");
            watch(consoleManager);

            try {
                final UserInput userInput = new UserInput.Builder("input").build();
                final ParseException parseException = new ParseException("message", -1);
                consoleManager.tell(new InvalidInput.Builder(userInput, parseException).build(), getRef());

                expectMsgAnyClassOf(duration("500 ms"), getExpectedClasses());

                final Set<String> outputLines = new TreeSet<>(consoleReader.getOutputLines());
                assertEquals(4, outputLines.size());

                final Iterator<String> iter = outputLines.iterator();
                assertEquals("\n", iter.next());
                assertEquals("Type 'help' to list the available commands", iter.next());
                assertEquals("message", iter.next());
                assertEquals(getSystemAndVersion(config), iter.next());
                assertFalse(iter.hasNext());

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
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

                expectNoMsg(duration("100 ms"));

                assertTrue(consoleReader.isShutdown());
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
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

                expectNoMsg(duration("100 ms"));
            } finally {
                consoleManager.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
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
            system.terminate();
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
        system.terminate();
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
