package mysystem.db.actor;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import mysystem.db.actor.company.CompanyManager;
import mysystem.db.config.DatabaseConfig;
import mysystem.db.model.DataType;
import mysystem.db.model.HasDataType;

import java.util.Objects;

import javax.sql.DataSource;

/**
 * This actor is responsible for managing all of the database actors, and is the supervisor for all of them.
 */
public class DatabaseManager extends UntypedActor {
    private final ActorRef companyManager;

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
        final DataSource dataSource = getDataSource(Objects.requireNonNull(config));
        this.companyManager = CompanyManager.create(context(), dataSource);
    }

    protected DataSource getDataSource(final Config config) {
        Objects.requireNonNull(config);

        final HikariConfig dbConfig = new HikariConfig();
        dbConfig.setAutoCommit(true);
        dbConfig.setDriverClassName(config.getString(DatabaseConfig.DATABASE_DRIVER_CLASS.getKey()));
        dbConfig.setUsername(config.getString(DatabaseConfig.DATABASE_USERNAME.getKey()));
        dbConfig.setPassword(config.getString(DatabaseConfig.DATABASE_PASSWORD.getKey()));
        dbConfig.setJdbcUrl(config.getString(DatabaseConfig.DATABASE_JDBC_URL.getKey()));

        return new HikariDataSource(dbConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof HasDataType) {
            final DataType dataType = ((HasDataType) message).getDataType();
            if (dataType == DataType.COMPANY) {
                this.companyManager.forward(message, context());
            }
        } else {
            unhandled(message);
        }
    }
}
