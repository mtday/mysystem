package mysystem.shell.actor;

import com.typesafe.config.Config;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import jline.console.ConsoleReader;
import mysystem.core.config.CoreConfig;
import mysystem.shell.model.AcceptInput;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.InvalidInput;
import mysystem.shell.model.Terminate;
import mysystem.shell.model.UnrecognizedCommand;
import mysystem.shell.model.UserInput;

import java.io.IOException;
import java.util.Objects;

/**
 * Responsible for managing the console reader, along with user input and console output.
 */
public class ConsoleManager extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorSelection inputParser;
    private final ConsoleReader consoleReader;

    /**
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        final Props props = Props.create(ConsoleManager.class);
        return Objects.requireNonNull(actorSystem).actorOf(props, ConsoleManager.class.getSimpleName());
    }

    /**
     * @param actorSystem the {@link ActorSystem} hosting the actor
     * @return an {@link ActorSelection} referencing this actor
     */
    public static ActorSelection getActorSelection(final ActorSystem actorSystem) {
        return Objects.requireNonNull(actorSystem).actorSelection("/user/" + ConsoleManager.class.getSimpleName());
    }

    /**
     * @throws IOException if there is a problem creating the console reader
     */
    public ConsoleManager() throws IOException {
        this.inputParser = InputFilter.getActorSelection(context().system());
        this.consoleReader = new ConsoleReader();
        this.consoleReader.setHandleUserInterrupt(false);
        this.consoleReader.setPaginationEnabled(true);
        this.consoleReader.setPrompt("shell> ");
    }

    /**
     * @return a reference to the actor used to parse user input
     */
    protected ActorSelection getInputParser() {
        return this.inputParser;
    }

    /**
     * @return the console reader used to manage user input and console output
     */
    protected ConsoleReader getConsoleReader() {
        return this.consoleReader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        final Config config = context().system().settings().config();
        if (config.hasPath(CoreConfig.ACTOR_SYSTEM_NAME.getKey()) && config.hasPath(CoreConfig.VERSION.getKey())) {
            final String system = config.getString(CoreConfig.ACTOR_SYSTEM_NAME.getKey());
            final String version = config.getString(CoreConfig.VERSION.getKey());

            getConsoleReader().println();
            getConsoleReader().println(String.format("%s %s", system, version));
            getConsoleReader().println();
            getConsoleReader().println("Type 'help' to list the available commands");
        }

        self().tell(new AcceptInput.Builder().build(), ActorRef.noSender());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) throws Exception {
        if (message instanceof AcceptInput) {
            handleAcceptInput();
        } else if (message instanceof ConsoleOutput) {
            handleConsoleOutput((ConsoleOutput) message);
        } else if (message instanceof UnrecognizedCommand) {
            handleUnrecognizedCommand((UnrecognizedCommand) message);
        } else if (message instanceof InvalidInput) {
            handleInvalidInput((InvalidInput) message);
        } else if (message instanceof Terminate) {
            handleTerminate();
        } else {
            unhandled(message);
        }
    }

    protected void handleAcceptInput() throws Exception {
        final String input = getConsoleReader().readLine();
        if (input == null) {
            // User typed Ctrl-D.
            self().tell(new Terminate.Builder().build(), self());
        } else {
            // Send the user input down-stream.
            getInputParser().tell(new UserInput.Builder(input).build(), self());
        }
        getConsoleReader().flush();
    }

    protected void handleConsoleOutput(final ConsoleOutput consoleOutput) throws Exception {
        if (consoleOutput.getOutput().isPresent()) {
            getConsoleReader().println(consoleOutput.getOutput().get());
        }

        if (consoleOutput.isTerminate()) {
            self().tell(new Terminate.Builder().build(), self());
        } else if (!consoleOutput.hasMore()) {
            self().tell(new AcceptInput.Builder().build(), self());
        }
    }

    protected void handleUnrecognizedCommand(final UnrecognizedCommand unrecognizedCommand) throws IOException {
        getConsoleReader().println("The specified command was not recognized: " + unrecognizedCommand.getUserInput());
        getConsoleReader().println("Use 'help' to see all the available commands.");
        getConsoleReader().flush();
        self().tell(new AcceptInput.Builder().build(), self());
    }

    protected void handleInvalidInput(final InvalidInput invalidInput) throws IOException {
        if (invalidInput.getLocation().isPresent()) {
            getConsoleReader().println(StringUtils.leftPad("^", invalidInput.getLocation().get(), "-"));
        }
        getConsoleReader().println(invalidInput.getError());
        getConsoleReader().flush();
        self().tell(new AcceptInput.Builder().build(), self());
    }

    protected void handleTerminate() throws IOException {
        getConsoleReader().println();
        getConsoleReader().getTerminal().setEchoEnabled(true);
        getConsoleReader().shutdown();
        context().system().shutdown();
    }
}
