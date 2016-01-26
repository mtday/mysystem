package mysystem.shell.actor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueType;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import mysystem.shell.config.ShellConfig;
import mysystem.shell.model.CommandConfig;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationLookup;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Responsible for managing all of the command registrations.
 */
public class RegistrationManager extends UntypedActor {
    /**
     * @param refFactory the {@link ActorRefFactory} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorRefFactory refFactory) {
        Objects.requireNonNull(refFactory);
        return refFactory.actorOf(Props.create(RegistrationManager.class), RegistrationManager.class.getSimpleName());
    }

    /**
     * @param refFactory the {@link ActorRefFactory} hosting the actor
     * @return an {@link ActorSelection} referencing this actor
     */
    public static ActorSelection getActorSelection(final ActorRefFactory refFactory) {
        return Objects.requireNonNull(refFactory).actorSelection("/user/" + RegistrationManager.class.getSimpleName());
    }

    private final Map<CommandPath, Registration> registrations = new TreeMap<>();

    /**
     * @return the available registrations
     */
    public Map<CommandPath, Registration> getRegistrations() {
        return Collections.unmodifiableMap(this.registrations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() {
        createActorsAndRequestCommandRegistrations(context().system().settings().config());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof RegistrationResponse) {
            // Add the provided registrations to the local registration state.
            for (final Registration registration : ((RegistrationResponse) message).getRegistrations()) {
                this.registrations.put(registration.getPath(), registration);
            }
        } else if (message instanceof RegistrationLookup) {
            // Send all of the matching registrations back to the caller.
            final RegistrationLookup lookup = (RegistrationLookup) message;
            sender().tell(new RegistrationResponse.Builder(lookup, getRegistrations()).build(), self());
        } else if (message instanceof RegistrationRequest) {
            // Send all of the known registrations back to the caller.
            sender().tell(new RegistrationResponse.Builder(getRegistrations().values()).build(), self());
        } else {
            unhandled(message);
        }
    }

    protected void createActorsAndRequestCommandRegistrations(final Config config) {
        final RegistrationRequest request = new RegistrationRequest.Builder().build();
        createCommandActors(config).forEach(actorRef -> actorRef.tell(request, self()));
    }

    protected List<ActorRef> createCommandActors(final Config config) {
        return getCommandConfigs(config).stream().map(cmd -> actor(context().system(), cmd))
                .collect(Collectors.toList());
    }

    protected ActorRef actor(final ActorSystem system, final CommandConfig commandConfig) {
        final Props props = Props.create(commandConfig.getCommandClass());
        return system.actorOf(props, commandConfig.getCommandName());
    }

    protected List<CommandConfig> getCommandConfigs(final Config config) {
        final List<CommandConfig> commandConfigs = new LinkedList<>();
        if (config.hasPath(ShellConfig.SHELL_COMMANDS.getKey())) {
            final ConfigObject obj = config.getConfig(ShellConfig.SHELL_COMMANDS.getKey()).root();
            obj.entrySet().stream().filter(e -> e.getValue().valueType() == ConfigValueType.OBJECT).forEach(entry -> {
                final Config commandConfig = ((ConfigObject) entry.getValue()).toConfig();
                commandConfigs.add(new CommandConfig.Builder(entry.getKey(), commandConfig).build());
            });
        }
        return commandConfigs;
    }
}
