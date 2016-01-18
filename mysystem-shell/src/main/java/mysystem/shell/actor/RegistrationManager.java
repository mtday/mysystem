package mysystem.shell.actor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueType;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import mysystem.shell.config.ShellConfig;
import mysystem.shell.model.CommandConfig;
import mysystem.shell.model.CommandPath;
import mysystem.shell.model.Registration;
import mysystem.shell.model.RegistrationLookup;
import mysystem.shell.model.RegistrationRequest;
import mysystem.shell.model.RegistrationResponse;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Responsible for managing all of the command registrations.
 */
public class RegistrationManager extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    /**
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        Objects.requireNonNull(actorSystem);
        return actorSystem.actorOf(Props.create(RegistrationManager.class), RegistrationManager.class.getSimpleName());
    }

    /**
     * @param actorSystem the {@link ActorSystem} hosting the actor
     * @return an {@link ActorSelection} referencing this actor
     */
    public static ActorSelection getActorSelection(final ActorSystem actorSystem) {
        return Objects.requireNonNull(actorSystem).actorSelection("/user/" + RegistrationManager.class.getSimpleName());
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
        final Config config = context().system().settings().config();
        if (config.hasPath(ShellConfig.SHELL_COMMANDS.getKey())) {
            final RegistrationRequest registrationRequest = new RegistrationRequest.Builder().build();
            final ConfigObject obj = config.getConfig(ShellConfig.SHELL_COMMANDS.getKey()).root();
            obj.entrySet().stream().filter(e -> e.getValue().valueType() == ConfigValueType.OBJECT).forEach(entry -> {
                final Config commandConfig = ((ConfigObject) entry.getValue()).toConfig();
                final CommandConfig command = new CommandConfig.Builder(entry.getKey(), commandConfig).build();
                context().system().actorOf(Props.create(command.getCommandClass()), command.getCommandName())
                        .tell(registrationRequest, self());
            });
        }
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
}
