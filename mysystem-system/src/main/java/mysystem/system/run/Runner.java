package mysystem.system.run;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import mysystem.common.config.CommonConfig;
import mysystem.db.actor.DatabaseManager;
import mysystem.tomcat.actor.TomcatManager;

/**
 * Launch the system.
 */
public class Runner {
    private final ActorSystem actorSystem;

    /**
     * Create the runner.
     */
    protected Runner() {
        final Config config = ConfigFactory.load();
        final String systemName = config.getString(CommonConfig.ACTOR_SYSTEM_NAME.getKey());
        this.actorSystem = ActorSystem.create(systemName, config);

        createActors(this.actorSystem);
        registerShutdownHook();
    }

    protected void createActors(final ActorSystem actorSystem) {
        DatabaseManager.create(actorSystem);
        TomcatManager.create(actorSystem);
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
