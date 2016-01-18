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
            // Send output with the terminate flag turned on.
            sender().tell(new ConsoleOutput.Builder("Disconnecting").setTerminate(true).build(), self());
        } else {
            unhandled(message);
        }
    }

    protected void handleRegistrationRequest() {
        final String description = "exit the shell";
        final CommandPath exit = new CommandPath.Builder("exit").build();
        final CommandPath quit = new CommandPath.Builder("quit").build();

        final Registration exitReg = new Registration.Builder(self(), exit).setDescription(description).build();
        final Registration quitReg = new Registration.Builder(self(), quit).setDescription(description).build();

        sender().tell(new RegistrationResponse.Builder().add(exitReg, quitReg).build(), self());
    }
}
