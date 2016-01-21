package mysystem.system.run;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import mysystem.common.config.CommonConfig;

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
        final String systemName = config.getString(CommonConfig.ACTOR_SYSTEM_NAME.getKey());
        this.actorSystem = ActorSystem.create(systemName, config);

        createActors(this.actorSystem);
        registerShutdownHook();
    }

    protected void createActors(final ActorSystem actorSystem) {
    }

    protected ActorSystem getActorSystem() {
        return this.actorSystem;
    }

    protected void terminate() {
        getActorSystem().terminate();
    }

    protected void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                terminate();
            }
        });
    }

    /**
     * @param args the command-line arguments
     */
    public static void main(final String... args) {
        new Runner();
    }
}
