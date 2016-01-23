package mysystem.db.model;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueType;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import akka.actor.ActorContext;
import akka.pattern.CircuitBreaker;
import mysystem.common.util.CollectionComparator;
import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * An immutable object representing the configuration for the actor that manages operations on an individual database
 * table.
 */
public class DatabaseManagerConfig implements Comparable<DatabaseManagerConfig>, Serializable {
    private final static long serialVersionUID = 1L;

    private final String actorName;
    private final DataType dataType;

    // The circuit breaker configuration for the database actor.
    private final int maxFailures;
    private final FiniteDuration callTimeout;
    private final FiniteDuration resetTimeout;

    // The database actor configurations.
    private final Set<DatabaseActorConfig> actorConfigs = new TreeSet<>();

    /**
     * @param actorName the name of the actor as defined in the configuration
     * @param dataType the type of data processed by the actor
     * @param maxFailures the maximum number of failures from the actor before opening the circuit breaker
     * @param callTimeout the amount of time to allow the actor to respond before the call is treated as an error
     * @param resetTimeout the amount of time to leave the circuit breaker open during failure situations
     * @param actorConfigs the configurations of the individual actors used to manage database operations
     */
    private DatabaseManagerConfig(
            final String actorName, final DataType dataType, final int maxFailures, final FiniteDuration callTimeout,
            final FiniteDuration resetTimeout, final Set<DatabaseActorConfig> actorConfigs) {
        this.actorName = actorName;
        this.dataType = dataType;
        this.maxFailures = maxFailures;
        this.callTimeout = callTimeout;
        this.resetTimeout = resetTimeout;
        this.actorConfigs.addAll(actorConfigs);
    }

    /**
     * @return the name of the actor as defined in the configuration
     */
    public String getActorName() {
        return this.actorName;
    }

    /**
     * @return the type of data processed by the actor
     */
    public DataType getDataType() {
        return this.dataType;
    }

    /**
     * @return the maximum number of failures from the actor before opening the circuit breaker
     */
    public int getMaxFailures() {
        return this.maxFailures;
    }

    /**
     * @return the amount of time to allow the actor to respond before the call is treated as an error
     */
    public FiniteDuration getCallTimeout() {
        return this.callTimeout;
    }

    /**
     * @return the amount of time to leave the circuit breaker open during failure situations
     */
    public FiniteDuration getResetTimeout() {
        return this.resetTimeout;
    }

    /**
     * @return an unmodifiable set of the configurations of the individual actors used to manage database operations
     */
    public Set<DatabaseActorConfig> getActorConfigs() {
        return Collections.unmodifiableSet(this.actorConfigs);
    }

    /**
     * @param context the {@link ActorContext} used to create the circuit breaker
     * @return a {@link CircuitBreaker} based on the configuration for the database actor
     */
    public CircuitBreaker getCircuitBreaker(final ActorContext context) {
        return new CircuitBreaker(context.dispatcher(), context.system().scheduler(), getMaxFailures(),
                getCallTimeout(), getResetTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("actorName", getActorName());
        str.append("dataType", getDataType());
        str.append("maxFailures", getMaxFailures());
        str.append("callTimeout", getCallTimeout());
        str.append("resetTimeout", getResetTimeout());
        str.append("actorConfigs", getActorConfigs());
        return str.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final DatabaseManagerConfig other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getActorName(), other.getActorName());
        cmp.append(getDataType(), other.getDataType());
        cmp.append(getMaxFailures(), other.getMaxFailures());
        cmp.append(getCallTimeout(), other.getCallTimeout());
        cmp.append(getResetTimeout(), other.getResetTimeout());
        cmp.append(getActorConfigs(), other.getActorConfigs(), new CollectionComparator<DatabaseActorConfig>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof DatabaseManagerConfig) && compareTo((DatabaseManagerConfig) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getActorName());
        hash.append(getDataType().name());
        hash.append(getMaxFailures());
        hash.append(getCallTimeout());
        hash.append(getResetTimeout());
        hash.append(getActorConfigs());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link DatabaseManagerConfig} objects.
     */
    public static class Builder {
        private final String actorName;
        private final DataType dataType;

        // The circuit breaker configuration for the database actor.
        private final int maxFailures;
        private final FiniteDuration callTimeout;
        private final FiniteDuration resetTimeout;

        // The database actor configurations.
        private final Set<DatabaseActorConfig> actorConfigs = new TreeSet<>();

        /**
         * @param other the {@link DatabaseManagerConfig} to duplicate
         */
        public Builder(final DatabaseManagerConfig other) {
            Objects.requireNonNull(other);
            this.actorName = other.getActorName();
            this.dataType = other.getDataType();
            this.maxFailures = other.getMaxFailures();
            this.callTimeout = other.getCallTimeout();
            this.resetTimeout = other.getResetTimeout();
            this.actorConfigs.addAll(other.getActorConfigs());
        }

        /**
         * @param actorName the name of the actor as defined in the configuration
         * @param managerConfig the configuration defined for the actor
         */
        public Builder(final String actorName, final Config managerConfig) {
            this.actorName = Objects.requireNonNull(actorName);

            if (managerConfig.hasPath("data-type")) {
                this.dataType = DataType.valueOf(managerConfig.getString("data-type"));
            } else {
                throw new IllegalArgumentException("Database manager config must specify the data-type config");
            }

            if (managerConfig.hasPath("max-failures")) {
                this.maxFailures = managerConfig.getInt("max-failures");
            } else {
                throw new IllegalArgumentException("Database manager config must specify the max-failures config");
            }

            if (managerConfig.hasPath("call-timeout")) {
                final long millis = managerConfig.getDuration("call-timeout").toMillis();
                this.callTimeout = FiniteDuration.create(millis, TimeUnit.MILLISECONDS);
            } else {
                throw new IllegalArgumentException("Database manager config must specify the call-timeout config");
            }

            if (managerConfig.hasPath("reset-timeout")) {
                final long millis = managerConfig.getDuration("reset-timeout").toMillis();
                this.resetTimeout = FiniteDuration.create(millis, TimeUnit.MILLISECONDS);
            } else {
                throw new IllegalArgumentException("Database manager config must specify the reset-timeout config");
            }

            if (managerConfig.hasPath("actors")) {
                final ConfigObject obj = managerConfig.getConfig("actors").root();
                obj.entrySet().stream().filter(e -> e.getValue().valueType() == ConfigValueType.OBJECT).forEach(e -> {
                    final Config actorConfig = ((ConfigObject) e.getValue()).toConfig();
                    this.actorConfigs.add(new DatabaseActorConfig.Builder(e.getKey(), actorConfig).build());
                });
            } else {
                throw new IllegalArgumentException("Database manager config must specify the actors config");
            }
        }

        /**
         * @return the {@link DatabaseManagerConfig} object
         */
        public DatabaseManagerConfig build() {
            return new DatabaseManagerConfig(this.actorName, this.dataType, this.maxFailures, this.callTimeout,
                    this.resetTimeout, this.actorConfigs);
        }
    }
}
