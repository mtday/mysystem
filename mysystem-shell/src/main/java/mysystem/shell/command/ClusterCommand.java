package mysystem.shell.command;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;

/**
 * This actor implements the {@code cluster} commands in the shell.
 */
public class ClusterCommand extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof RegistrationRequest) {
            handleRegistrationRequest();
        } else if (message instanceof Command) {
            handleCommand((Command) message);
        } else {
            unhandled(message);
        }
    }

    private void handleRegistrationRequest() {
        final String description = "provides information about the system cluster and its members";
        final CommandPath cluster = new CommandPath.Builder("cluster").build();
        final Registration reg = new Registration.Builder(self(), cluster).setDescription(description).build();
        sender().tell(new RegistrationResponse.Builder().add(reg).build(), self());
    }

    protected void handleCommand(final Command command) {
        sender().tell(new ConsoleOutput.Builder("cluster info goes here").build(), self());
    }
}
