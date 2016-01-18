package mysystem.shell.run;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import mysystem.core.config.CoreConfig;
import mysystem.shell.actor.CommandExecutor;
import mysystem.shell.actor.ConsoleManager;
import mysystem.shell.actor.InputFilter;
import mysystem.shell.actor.InputTokenizer;
import mysystem.shell.actor.RegistrationFinder;
import mysystem.shell.actor.RegistrationManager;

/**
 * Launch the shell.
 */
public class Shell {
    private final ActorSystem actorSystem;

    /**
     * Create the shell.
     */
    public Shell() {
        final Config config = ConfigFactory.load("shell-config").withFallback(ConfigFactory.load());
        final String systemName = config.getString(CoreConfig.ACTOR_SYSTEM_NAME.getKey());
        this.actorSystem = ActorSystem.create(systemName, config);

        createActors();
        registerShutdownHook();
    }

    private ActorSystem getActorSystem() {
        return this.actorSystem;
    }

    private void createActors() {
        RegistrationManager.create(getActorSystem());
        ConsoleManager.create(getActorSystem());
        CommandExecutor.create(getActorSystem());
        InputFilter.create(getActorSystem());
        InputTokenizer.create(getActorSystem());
        RegistrationFinder.create(getActorSystem());
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                getActorSystem().shutdown();
            }
        });
    }

    /**
     * @param args the command-line arguments
     */
    public static void main(final String... args) {
        new Shell();
    }
}
