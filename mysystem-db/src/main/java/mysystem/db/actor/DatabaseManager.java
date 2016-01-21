package mysystem.db.actor;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import mysystem.db.config.DatabaseConfig;

import java.util.Objects;

import javax.sql.DataSource;

/**
 * This actor is responsible for managing all of the database actors, and is the supervisor for all of them.
 */
public class DatabaseManager extends UntypedActor {
    private final DataSource dataSource;

    /**
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        final Props props = Props.create(DatabaseManager.class, actorSystem.settings().config());
        return Objects.requireNonNull(actorSystem).actorOf(props, DatabaseManager.class.getSimpleName());
    }

    /**
     * @param config the system configuration
     */
    public DatabaseManager(final Config config) {
        final HikariConfig dbconfig = new HikariConfig();
        dbconfig.setAutoCommit(true);
        dbconfig.setDriverClassName(config.getString(DatabaseConfig.DATABASE_DRIVER_CLASS.getKey()));
        dbconfig.setUsername(config.getString(DatabaseConfig.DATABASE_USERNAME.getKey()));
        dbconfig.setPassword(config.getString(DatabaseConfig.DATABASE_PASSWORD.getKey()));
        dbconfig.setJdbcUrl(config.getString(DatabaseConfig.DATABASE_JDBC_URL.getKey()));

        this.dataSource = new HikariDataSource(dbconfig);
    }

    /**
     * @return the {@link DataSource} used to retrieve database connection objects
     */
    protected DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof String) {

        } else {
            unhandled(message);
        }
    }
}
