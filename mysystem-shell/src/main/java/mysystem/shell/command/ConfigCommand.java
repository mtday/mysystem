package mysystem.shell.command;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;

import java.util.Map;

/**
 * This actor implements the {@code config} commands in the shell.
 */
public class ConfigCommand extends UntypedActor {
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
        final Options options = new Options();
        options.addOption(new Option("f", "filter", true, "a regular expression used to filter the output"));
        options.addOption(new Option("o", "origin", false, "include the origin of the configuration in the output"));

        final CommandPath config = new CommandPath.Builder("config").build();
        final Registration configRegistration = new Registration.Builder(self(), config, options).build();
        sender().tell(new RegistrationResponse.Builder().add(configRegistration).build(), self());
    }

    protected void handleCommand(final Command command) {
        final Config config = context().system().settings().config();
        config.entrySet().stream().map(e -> getOutput(e)).sorted().forEach(c -> sender().tell(c, self()));
        sender().tell(new ConsoleOutput.Builder().build(), self());
    }

    protected ConsoleOutput getOutput(final Map.Entry<String, ConfigValue> entry) {
        final String output = String.format("%s => %s", entry.getKey(), entry.getValue().render());
        return new ConsoleOutput.Builder(output).setHasMore(true).build();
    }
}
