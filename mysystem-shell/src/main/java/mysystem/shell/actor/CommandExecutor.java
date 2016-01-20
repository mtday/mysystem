package mysystem.shell.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
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
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        final Props props = Props.create(CommandExecutor.class);
        return Objects.requireNonNull(actorSystem).actorOf(props, CommandExecutor.class.getSimpleName());
    }

    /**
     * @param actorSystem the {@link ActorSystem} hosting the actor
     * @return an {@link ActorSelection} referencing this actor
     */
    public static ActorSelection getActorSelection(final ActorSystem actorSystem) {
        return Objects.requireNonNull(actorSystem).actorSelection("/user/" + CommandExecutor.class.getSimpleName());
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
            ((Command) message).getRegistration().getActor().tell(message, self());
        } else {
            // Send everything else back to the console manager.
            getConsoleManager().tell(message, self());
        }
    }
}
