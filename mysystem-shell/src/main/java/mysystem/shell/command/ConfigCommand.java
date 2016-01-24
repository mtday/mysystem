package mysystem.shell.command;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import akka.actor.UntypedActor;
import mysystem.shell.model.Command;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.ConsoleOutput;
import mysystem.shell.model.Option;
import mysystem.shell.model.Options;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;

import java.util.Map;
import java.util.SortedSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This actor implements the {@code config} commands in the shell.
 */
public class ConfigCommand extends UntypedActor {
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
        final String description = "provides information about the system configuration";

        final Option filter = new Option.Builder().setDescription("a regular expression used to filter the output")
                .setShortOption("f").setLongOption("filter").setArguments(1).setArgName("filter-expression").build();
        final Options options = new Options.Builder(filter).build();

        final CommandPath config = new CommandPath.Builder("config").build();
        final Registration reg = new Registration.Builder().setActorPath(self()).setPath(config).setOptions(options)
                .setDescription(description).build();
        sender().tell(new RegistrationResponse.Builder().add(reg).build(), self());
    }

    protected void handleCommand(final Command command) {
        final Config config = context().system().settings().config();

        // Send the filtered config to the console.
        getFilteredConfig(config, command).stream().map(this::getOutput).forEach(o -> sender().tell(o, self()));

        // Send one last output with hasMore = false.
        sender().tell(new ConsoleOutput.Builder().build(), self());
    }

    protected SortedSet<String> getFilteredConfig(final Config config, final Command command) {
        final Optional<Pattern> pattern = getFilterPattern(command);
        final Supplier<SortedSet<String>> supplier = TreeSet::new;
        return config.entrySet().stream().filter(e -> accept(e, pattern)).map(this::asString).sorted()
                .collect(Collectors.toCollection(supplier));
    }

    protected String asString(final ConfigValue configValue) {
        if (configValue.valueType() == ConfigValueType.STRING) {
            return StringUtils.substringBetween(configValue.render(), "\"");
        }
        return configValue.render();
    }

    protected String asString(final Map.Entry<String, ConfigValue> entry) {
        return String.format("  %s => %s", entry.getKey(), asString(entry.getValue()));
    }

    protected ConsoleOutput getOutput(final String configString) {
        return new ConsoleOutput.Builder(configString).setHasMore(true).build();
    }

    protected Optional<Pattern> getFilterPattern(final Command command) {
        final Optional<CommandLine> commandLine = command.getCommandLine();
        if (commandLine.isPresent() && commandLine.get().hasOption('f')) {
            final String regex = commandLine.get().getOptionValue('f');
            if (StringUtils.isEmpty(regex)) {
                return Optional.empty();
            } else if (StringUtils.containsAny(regex, '*', '+', '^', '$', '[')) {
                // If the command specifies a filter pattern that has regex characters, assume the user knows what
                // they are doing and provided a valid regex.
                return Optional.of(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            } else {
                // The command filter does not look like a regex, so turn it into one that is more likely to match
                // the available configuration data.
                return Optional.of(Pattern.compile(".*" + regex + ".*", Pattern.CASE_INSENSITIVE));
            }
        }
        return Optional.empty();
    }

    protected boolean accept(final Map.Entry<String, ConfigValue> entry, final Optional<Pattern> pattern) {
        if (pattern.isPresent()) {
            final Matcher keyMatcher = pattern.get().matcher(entry.getKey());
            final Matcher valMatcher = pattern.get().matcher(asString(entry.getValue()));
            return keyMatcher.matches() || valMatcher.matches();
        }
        return true;
    }
}
