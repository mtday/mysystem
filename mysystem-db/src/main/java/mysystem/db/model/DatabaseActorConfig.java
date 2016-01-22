package mysystem.db.model;

import com.typesafe.config.Config;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import akka.actor.ActorContext;
import akka.actor.UntypedActor;
import akka.pattern.CircuitBreaker;
import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * An immutable object representing the configuration for the actor that manages operations on an individual database
 * table.
 */
public class DatabaseActorConfig implements Comparable<DatabaseActorConfig>, Serializable {
    private final static long serialVersionUID = 1L;

    private final String actorName;
    private final Class<? extends UntypedActor> actorClass;
    private final DataType dataType;

    // The circuit breaker configuration for the database actor.
    private final int maxFailures;
    private final FiniteDuration callTimeout;
    private final FiniteDuration resetTimeout;

    /**
     * @param actorName the name of the actor as defined in the configuration
     * @param actorClass the class that implements the database actor
     * @param dataType the type of data processed by the actor
     * @param maxFailures the maximum number of failures from the actor before opening the circuit breaker
     * @param callTimeout the amount of time to allow the actor to respond before the call is treated as an error
     * @param resetTimeout the amount of time to leave the circuit breaker open during failure situations
     */
    private DatabaseActorConfig(
            final String actorName, final Class<? extends UntypedActor> actorClass, final DataType dataType,
            final int maxFailures, final FiniteDuration callTimeout, final FiniteDuration resetTimeout) {
        this.actorName = actorName;
        this.actorClass = actorClass;
        this.dataType = dataType;
        this.maxFailures = maxFailures;
        this.callTimeout = callTimeout;
        this.resetTimeout = resetTimeout;
    }

    /**
     * @return the name of the actor as defined in the configuration
     */
    public String getActorName() {
        return this.actorName;
    }

    /**
     * @return the class that implements the database actor
     */
    public Class<? extends UntypedActor> getActorClass() {
        return this.actorClass;
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
        str.append("actorClass", getActorClass().getName());
        str.append("dataType", getDataType());
        str.append("maxFailures", getMaxFailures());
        str.append("callTimeout", getCallTimeout());
        str.append("resetTimeout", getResetTimeout());
        return str.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final DatabaseActorConfig other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getActorName(), other.getActorName());
        cmp.append(getActorClass().getName(), other.getActorClass().getName());
        cmp.append(getDataType(), other.getDataType());
        cmp.append(getMaxFailures(), other.getMaxFailures());
        cmp.append(getCallTimeout(), other.getCallTimeout());
        cmp.append(getResetTimeout(), other.getResetTimeout());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof DatabaseActorConfig) && compareTo((DatabaseActorConfig) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getActorName());
        hash.append(getActorClass().getName());
        hash.append(getDataType().name());
        hash.append(getMaxFailures());
        hash.append(getCallTimeout());
        hash.append(getResetTimeout());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link DatabaseActorConfig} objects.
     */
    public static class Builder {
        private final String actorName;
        private final Class<? extends UntypedActor> actorClass;
        private final DataType dataType;

        // The circuit breaker configuration for the database actor.
        private final int maxFailures;
        private final FiniteDuration callTimeout;
        private final FiniteDuration resetTimeout;

        /**
         * @param other the {@link DatabaseActorConfig} to duplicate
         */
        public Builder(final DatabaseActorConfig other) {
            Objects.requireNonNull(other);
            this.actorName = other.getActorName();
            this.actorClass = other.getActorClass();
            this.dataType = other.getDataType();
            this.maxFailures = other.getMaxFailures();
            this.callTimeout = other.getCallTimeout();
            this.resetTimeout = other.getResetTimeout();
        }

        /**
         * @param actorName the name of the actor as defined in the configuration
         * @param actorConfig the configuration defined for the actor
         */
        public Builder(final String actorName, final Config actorConfig) {
            this.actorName = Objects.requireNonNull(actorName);

            if (Objects.requireNonNull(actorConfig).hasPath("class")) {
                final String className = actorConfig.getString("class");
                try {
                    this.actorClass = Class.forName(className).asSubclass(UntypedActor.class);
                } catch (final ClassNotFoundException notFound) {
                    throw new IllegalArgumentException("Database actor class not found: " + className);
                }
            } else {
                throw new IllegalArgumentException("Database actor config must specify a class: " + actorName);
            }

            if (actorConfig.hasPath("data-type")) {
                this.dataType = DataType.valueOf(actorConfig.getString("data-type"));
            } else {
                throw new IllegalArgumentException("Database actor config must specify the data-type config");
            }

            if (actorConfig.hasPath("max-failures")) {
                this.maxFailures = actorConfig.getInt("max-failures");
            } else {
                throw new IllegalArgumentException("Database actor config must specify the max-failures config");
            }

            if (actorConfig.hasPath("call-timeout")) {
                final long millis = actorConfig.getDuration("call-timeout").toMillis();
                this.callTimeout = FiniteDuration.create(millis, TimeUnit.MILLISECONDS);
            } else {
                throw new IllegalArgumentException("Database actor config must specify the call-timeout config");
            }

            if (actorConfig.hasPath("reset-timeout")) {
                final long millis = actorConfig.getDuration("reset-timeout").toMillis();
                this.resetTimeout = FiniteDuration.create(millis, TimeUnit.MILLISECONDS);
            } else {
                throw new IllegalArgumentException("Database actor config must specify the reset-timeout config");
            }
        }

        /**
         * @return the {@link DatabaseActorConfig} object
         */
        public DatabaseActorConfig build() {
            return new DatabaseActorConfig(
                    this.actorName, this.actorClass, this.dataType, this.maxFailures, this.callTimeout,
                    this.resetTimeout);
        }
    }
}
