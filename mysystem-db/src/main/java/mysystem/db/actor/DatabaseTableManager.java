package mysystem.db.actor;

import com.google.common.base.Optional;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.CircuitBreaker;
import mysystem.db.model.DatabaseManagerConfig;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * This actor is responsible for managing all of the actors for a specific database table.
 */
public class DatabaseTableManager extends UntypedActor {
    private final Map<Class<?>, ActorRef> actorMap = new HashMap<>();

    /**
     * @param managerConfig the {@link DatabaseManagerConfig} defining the child actors to be managed
     * @param dataSource the {@link DataSource} used to manage database connections
     * @param circuitBreaker the {@link CircuitBreaker} used to manage push-back when the database gets overloaded
     */
    public DatabaseTableManager(
            final DatabaseManagerConfig managerConfig, final DataSource dataSource,
            final CircuitBreaker circuitBreaker) {
        final Map<Class<? extends UntypedActor>, ActorRef> map = getActorMap(managerConfig, dataSource, circuitBreaker);
        managerConfig.getActorConfigs().forEach(
                actorConfig -> this.actorMap.put(actorConfig.getMessageClass(), map.get(actorConfig.getActorClass())));
    }

    protected Map<Class<? extends UntypedActor>, ActorRef> getActorMap(
            final DatabaseManagerConfig managerConfig, final DataSource dataSource,
            final CircuitBreaker circuitBreaker) {
        final Map<Class<? extends UntypedActor>, ActorRef> map = new HashMap<>();
        managerConfig.getActorConfigs().forEach(actorConfig -> {
            if (!map.containsKey(actorConfig.getActorClass())) {
                final Props props = Props.create(actorConfig.getActorClass(), dataSource, circuitBreaker);
                final ActorRef actorRef = context().actorOf(props, actorConfig.getActorName());
                map.put(actorConfig.getActorClass(), actorRef);
            }
        });
        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Object message) {
        final Optional<ActorRef> handler = Optional.fromNullable(this.actorMap.get(message.getClass()));
        if (handler.isPresent()) {
            handler.get().forward(message, context());
        } else {
            unhandled(message);
        }
    }
}
