package mysystem.shell.actor;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import mysystem.shell.model.Command;

import java.util.Objects;

/**
 * Responsible for executing commands.
 */
public class CommandExecutor extends UntypedActor {
    private final ActorSelection consoleManager;

    /**
     * @param refFactory the {@link ActorRefFactory} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorRefFactory refFactory) {
        final Props props = Props.create(CommandExecutor.class);
        return Objects.requireNonNull(refFactory).actorOf(props, CommandExecutor.class.getSimpleName());
    }

    /**
     * @param refFactory the {@link ActorRefFactory} hosting the actor
     * @return an {@link ActorSelection} referencing this actor
     */
    public static ActorSelection getActorSelection(final ActorRefFactory refFactory) {
        return Objects.requireNonNull(refFactory).actorSelection("/user/" + CommandExecutor.class.getSimpleName());
    }

    /**
     * Default constructor.
     */
    public CommandExecutor() {
        this.consoleManager = ConsoleManager.getActorSelection(context().system());
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
        if (message instanceof Command) {
            // Proxy the command to the command's implementation actor.
            context().actorSelection(((Command) message).getRegistration().getActorPath()).tell(message, self());
        } else {
            // Send everything else back to the console manager.
            getConsoleManager().tell(message, self());
        }
    }
}
