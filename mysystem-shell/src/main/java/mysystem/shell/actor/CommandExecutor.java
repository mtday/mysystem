package mysystem.shell.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.shell.model.Command;
import mysystem.shell.model.ConsoleOutput;

import java.util.Objects;

/**
 * Responsible for executing commands.
 */
public class CommandExecutor extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorSelection consoleManager;

    /**
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        final Props props = Props.create(CommandExecutor.class);
        return Objects.requireNonNull(actorSystem).actorOf(props, CommandExecutor.class.getSimpleName());
    }

    /**
     * Default constructor.
     */
    public CommandExecutor() {
        this.consoleManager = context().system().actorSelection("/user/" + ConsoleManager.class.getSimpleName());
    }

    /**
     * @return a reference to the console manager actor
     */
    protected ActorSelection getConsoleManager() {
        return this.consoleManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        log.error("Received: {}", message);
        if (message instanceof Command) {
            final Command command = (Command) message;
            // Proxy the command to the command's implementation actor.
            command.getRegistration().getActor().tell(command, self());
        } else if (message instanceof ConsoleOutput) {
            getConsoleManager().tell(message, self());
        } else {
            unhandled(message);
        }
    }
}
