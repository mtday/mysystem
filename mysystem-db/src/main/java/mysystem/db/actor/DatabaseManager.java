package mysystem.db.actor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.CircuitBreaker;
import mysystem.db.config.DatabaseConfig;
import mysystem.db.model.DataType;
import mysystem.db.model.DatabaseActorConfig;
import mysystem.db.model.GetById;
import mysystem.db.model.HasDataType;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

/**
 * This actor is responsible for managing all of the database actors, and is the top-level supervisor for all of them.
 * This actor delegates the work to the next level of actors.
 */
public class DatabaseManager extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final Map<DataType, ActorRef> actors = new TreeMap<>();

    /**
     * @param actorSystem the {@link ActorSystem} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorSystem actorSystem) {
        final Props props = Props.create(DatabaseManager.class);
        return Objects.requireNonNull(actorSystem).actorOf(props, DatabaseManager.class.getSimpleName());
    }

    public DatabaseManager() {
        final Config config = context().system().settings().config();
        final DataSource dataSource = getDataSource(config);
        createDatabaseActors(config, dataSource, this.actors);
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

    protected Optional<ActorRef> getActor(final DataType dataType) {
        return Optional.ofNullable(this.actors.get(Objects.requireNonNull(dataType)));
    }

    protected void createDatabaseActors(
            final Config config, final DataSource dataSource, final Map<DataType, ActorRef> actorMap) {
        for (final DatabaseActorConfig actorConfig : getDatabaseActorConfigs(config)) {
            actorMap.put(actorConfig.getDataType(), actor(context(), actorConfig, dataSource));
        }
    }

    protected ActorRef actor(
            final ActorContext context, final DatabaseActorConfig actorConfig, final DataSource dataSource) {
        final CircuitBreaker circuitBreaker = actorConfig.getCircuitBreaker(context());
        final Props props = Props.create(actorConfig.getActorClass(), dataSource, circuitBreaker);
        return context.actorOf(props, actorConfig.getActorName());
    }

    protected Set<DatabaseActorConfig> getDatabaseActorConfigs(final Config config) {
        final Set<DatabaseActorConfig> actors = new TreeSet<>();
        if (config.hasPath(DatabaseConfig.DATABASE_ACTORS.getKey())) {
            config.getObject(DatabaseConfig.DATABASE_ACTORS.getKey());
            final ConfigObject obj = config.getConfig(DatabaseConfig.DATABASE_ACTORS.getKey()).root();
            obj.entrySet().stream().filter(e -> e.getValue().valueType() == ConfigValueType.OBJECT).forEach(entry -> {
                final Config actorConfig = ((ConfigObject) entry.getValue()).toConfig();
                actors.add(new DatabaseActorConfig.Builder(entry.getKey(), actorConfig).build());
            });
        }
        return actors;
    }

    @Override
    public void preStart() {
        self().tell(new GetById.Builder(DataType.COMPANY, 1).build(), self());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        log.info("Received: {}", message);
        if (message instanceof HasDataType) {
            final DataType dataType = ((HasDataType) message).getDataType();
            log.info("  Data Type is: {}", dataType);
            final Optional<ActorRef> actorRef = getActor(dataType);
            if (actorRef.isPresent()) {
                log.info("  Forwarding to: {}", actorRef.get());
                actorRef.get().forward(message, context());
            } else {
                log.info("  Unhandled since no actor ref is present");
                unhandled(message);
            }
        } else {
            unhandled(message);
        }
    }
}
