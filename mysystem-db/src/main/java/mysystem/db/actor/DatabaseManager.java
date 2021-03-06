package mysystem.db.actor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.pattern.CircuitBreaker;
import mysystem.common.model.SystemRole;
import mysystem.common.util.cluster.ClusterUtils;
import mysystem.db.config.DatabaseConfig;
import mysystem.db.model.DataType;
import mysystem.db.model.DatabaseManagerConfig;
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
    private final Map<DataType, ActorRef> actors = new TreeMap<>();

    /**
     * @param refFactory the {@link ActorRefFactory} that will host the actor
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(final ActorRefFactory refFactory) {
        final Props props = Props.create(DatabaseManager.class);
        return Objects.requireNonNull(refFactory).actorOf(props, DatabaseManager.class.getSimpleName());
    }

    /**
     * @param actorSystem the {@link ActorSystem} hosting the actor used to find the selection
     * @param cluster the {@link Cluster} from which the selection should be retrieved
     * @return an {@link ActorSelection} referencing this actor
     */
    public static Optional<ActorSelection> getActorSelection(final ActorSystem actorSystem, final Cluster cluster) {
        final Optional<Address> address =
                new ClusterUtils().getRandomNode(Objects.requireNonNull(cluster), SystemRole.SYSTEM);
        if (!address.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(Objects.requireNonNull(actorSystem)
                .actorSelection(String.format("%s/user/%s", address.get(), DatabaseManager.class.getSimpleName())));
    }

    public DatabaseManager(final DataSource dataSource) {
        final Config config = context().system().settings().config();
        createDatabaseActors(config, dataSource, this.actors);
    }

    public DatabaseManager() {
        final Config config = context().system().settings().config();
        final DataSource dataSource = getDataSource(config);
        createDatabaseActors(config, dataSource, this.actors);
    }

    protected DataSource getDataSource(final Config config) {
        Objects.requireNonNull(config);

        final HikariConfig dbConfig = new HikariConfig();
        dbConfig.setPoolName("hikari-connection-pool");
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
        for (final DatabaseManagerConfig managerConfig : getDatabaseActorConfigs(config)) {
            actorMap.put(managerConfig.getDataType(), actor(context(), managerConfig, dataSource));
        }
    }

    protected ActorRef actor(
            final ActorContext context, final DatabaseManagerConfig managerConfig, final DataSource dataSource) {
        final CircuitBreaker circuitBreaker = managerConfig.getCircuitBreaker(context());
        final Props props = Props.create(DatabaseTableManager.class, managerConfig, dataSource, circuitBreaker);
        return context.actorOf(props, managerConfig.getActorName());
    }

    protected Set<DatabaseManagerConfig> getDatabaseActorConfigs(final Config config) {
        final Set<DatabaseManagerConfig> actors = new TreeSet<>();
        if (config.hasPath(DatabaseConfig.DATABASE_ACTORS.getKey())) {
            config.getObject(DatabaseConfig.DATABASE_ACTORS.getKey());
            final ConfigObject obj = config.getConfig(DatabaseConfig.DATABASE_ACTORS.getKey()).root();
            obj.entrySet().stream().filter(e -> e.getValue().valueType() == ConfigValueType.OBJECT).forEach(entry -> {
                final Config managerConfig = ((ConfigObject) entry.getValue()).toConfig();
                actors.add(new DatabaseManagerConfig.Builder(entry.getKey(), managerConfig).build());
            });
        }
        return actors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        if (message instanceof HasDataType) {
            final DataType dataType = ((HasDataType) message).getDataType();
            final Optional<ActorRef> actorRef = getActor(dataType);
            if (actorRef.isPresent()) {
                actorRef.get().forward(message, context());
            } else {
                unhandled(message);
            }
        } else {
            unhandled(message);
        }
    }
}
