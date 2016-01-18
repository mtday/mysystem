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
 * This actor implements the {@code exit} and {@code quit} commands in the shell.
 */
public class ExitCommand extends UntypedActor {
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

    protected void handleRegistrationRequest() {
        final CommandPath exit = new CommandPath.Builder("exit").build();
        final CommandPath quit = new CommandPath.Builder("quit").build();

        final Registration exitRegistration = new Registration.Builder(self(), exit).build();
        final Registration quitRegistration = new Registration.Builder(self(), quit).build();

        sender().tell(new RegistrationResponse.Builder().add(exitRegistration, quitRegistration).build(), self());
    }

    protected void handleCommand(final Command command) {
        sender().tell(new ConsoleOutput.Builder("Disconnecting").setTerminate(true).build(), self());
    }
}
