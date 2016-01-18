package mysystem.core.run;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import mysystem.core.config.CoreConfig;

/**
 * Launch the system.
 */
public class Runner {
    private final ActorSystem actorSystem;

    /**
     * Create the runner.
     */
    public Runner() {
        final Config config = ConfigFactory.load();
        final String systemName = config.getString(CoreConfig.ACTOR_SYSTEM_NAME.getKey());
        this.actorSystem = ActorSystem.create(systemName, config);
    }

    /**
     * @param args the command-line arguments
     */
    public static void main(final String... args) {
        new Runner();
    }
}
