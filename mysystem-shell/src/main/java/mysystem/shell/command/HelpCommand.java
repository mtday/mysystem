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
 * This actor implements the {@code help} command in the shell.
 */
public class HelpCommand extends UntypedActor {
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
        final CommandPath help = new CommandPath.Builder("help").build();
        final Registration helpRegistration = new Registration.Builder(self(), help).build();
        sender().tell(new RegistrationResponse.Builder().add(helpRegistration).build(), self());
    }

    protected void handleCommand(final Command command) {
        sender().tell(new ConsoleOutput.Builder("help information goes here").build(), self());
    }
}
