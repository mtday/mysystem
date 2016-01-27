package mysystem.shell.command;

import com.google.common.annotations.VisibleForTesting;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import mysystem.db.actor.DatabaseManager;
import mysystem.db.model.DataType;
import mysystem.db.model.GetAll;
import mysystem.db.model.ModelCollection;
import mysystem.shell.actor.ConsoleManager;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;

import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;

/**
 * This actor implements the {@code database} command in the shell. It is only used to test communication into a
 * cluster and is not intended to implement real functionality.
 */
public class DatabaseCommand extends UntypedActor {
    private final Cluster cluster;

    /**
     * Default constructor.
     */
    public DatabaseCommand() {
        this.cluster = Cluster.get(context().system());
    }

    /**
     * @param cluster the {@link Cluster} to which this command belongs
     */
    @VisibleForTesting
    public DatabaseCommand(final Cluster cluster) {
        this.cluster = Objects.requireNonNull(cluster);
    }

    /**
     * @return the {@link Cluster} to which this command belongs
     */
    protected Cluster getCluster() {
        return this.cluster;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof RegistrationRequest) {
            handleRegistrationRequest();
        } else if (message instanceof Command) {
            handleCommand();
        } else if (message instanceof ModelCollection) {
            handleModelCollection((ModelCollection<?>) message);
        } else {
            unhandled(message);
        }
    }

    private void handleRegistrationRequest() {
        final String description = "provides the ability to invoke database operations";

        final CommandPath config = new CommandPath.Builder("database").build();
        final Registration reg =
                new Registration.Builder().setActorPath(self()).setPath(config).setDescription(description).build();
        sender().tell(new RegistrationResponse.Builder().add(reg).build(), self());
    }

    protected void handleCommand() {
        final Optional<ActorSelection> dbmanager = DatabaseManager.getActorSelection(context().system(), getCluster());
        if (dbmanager.isPresent()) {
            dbmanager.get().tell(new GetAll.Builder().setDataType(DataType.COMPANY).build(), self());
        } else {
            final ActorSelection console = ConsoleManager.getActorSelection(context().system());
            console.tell(
                    new ConsoleOutput.Builder("Failed to find remote actor, check cluster status").build(), self());
        }
    }

    protected void handleModelCollection(final ModelCollection<?> modelCollection) {
        final SortedSet<?> models = modelCollection.getModels();

        final ActorSelection console = ConsoleManager.getActorSelection(context());

        if (models.isEmpty()) {
            console.tell(new ConsoleOutput.Builder("No companies available").build(), self());
        } else {
            console.tell(new ConsoleOutput.Builder("Companies: " + models.size()).setHasMore(true).build(), self());
            models.forEach(c -> console.tell(new ConsoleOutput.Builder(c.toString()).setHasMore(true).build(), self()));
            console.tell(new ConsoleOutput.Builder().build(), self());
        }
    }
}
