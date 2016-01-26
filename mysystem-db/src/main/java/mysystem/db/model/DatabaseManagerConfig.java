package mysystem.db.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import akka.actor.ActorContext;
import akka.pattern.CircuitBreaker;
import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;
import mysystem.common.util.CollectionComparator;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/**
 * An immutable object representing the configuration for the actor that manages operations on an individual database
 * table.
 */
public class DatabaseManagerConfig implements Model, Comparable<DatabaseManagerConfig> {
    private final static String SERIALIZATION_MANIFEST = DatabaseManagerConfig.class.getSimpleName();

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
     * {@inheritDoc}
     */
    @Override
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
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
    public JsonObject toJson() {
        final JsonArray actorConfArr = new JsonArray();
        getActorConfigs().forEach(c -> actorConfArr.add(c.toJson()));

        final JsonObject json = new JsonObject();
        json.addProperty("actorName", getActorName());
        json.addProperty("dataType", getDataType().name());
        json.addProperty("maxFailures", getMaxFailures());
        json.addProperty("callTimeout", getCallTimeout().toMillis());
        json.addProperty("resetTimeout", getResetTimeout().toMillis());
        json.add("actorConfigs", actorConfArr);
        json.addProperty("manifest", getSerializationManifest());
        return json;
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
    public int compareTo(@Nullable final DatabaseManagerConfig other) {
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
    public static class Builder implements ModelBuilder<DatabaseManagerConfig> {
        private Optional<String> actorName = Optional.empty();
        private Optional<DataType> dataType = Optional.empty();

        // The circuit breaker configuration for the database actor.
        private Optional<Integer> maxFailures = Optional.empty();
        private Optional<FiniteDuration> callTimeout = Optional.empty();
        private Optional<FiniteDuration> resetTimeout = Optional.empty();

        // The database actor configurations.
        private final Set<DatabaseActorConfig> actorConfigs = new TreeSet<>();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param other the {@link DatabaseManagerConfig} to duplicate
         */
        public Builder(final DatabaseManagerConfig other) {
            Objects.requireNonNull(other);
            setActorName(other.getActorName());
            setDataType(other.getDataType());
            setMaxFailures(other.getMaxFailures());
            setCallTimeout(other.getCallTimeout());
            setResetTimeout(other.getResetTimeout());
            add(other.getActorConfigs());
        }

        /**
         * @param actorName the name of the actor as defined in the configuration
         * @param managerConfig the configuration defined for the actor
         */
        public Builder(final String actorName, final Config managerConfig) {
            setActorName(actorName);

            if (managerConfig.hasPath("data-type")) {
                setDataType(DataType.valueOf(managerConfig.getString("data-type")));
            }
            if (managerConfig.hasPath("max-failures")) {
                setMaxFailures(managerConfig.getInt("max-failures"));
            }
            if (managerConfig.hasPath("call-timeout")) {
                final long millis = managerConfig.getDuration("call-timeout").toMillis();
                setCallTimeout(FiniteDuration.create(millis, TimeUnit.MILLISECONDS));
            }
            if (managerConfig.hasPath("reset-timeout")) {
                final long millis = managerConfig.getDuration("reset-timeout").toMillis();
                setResetTimeout(FiniteDuration.create(millis, TimeUnit.MILLISECONDS));
            }

            if (managerConfig.hasPath("actors")) {
                final ConfigObject obj = managerConfig.getConfig("actors").root();
                obj.entrySet().stream().filter(e -> e.getValue().valueType() == ConfigValueType.OBJECT).forEach(e -> {
                    final Config actorConfig = ((ConfigObject) e.getValue()).toConfig();
                    add(new DatabaseActorConfig.Builder(e.getKey(), actorConfig).build());
                });
            }
        }

        /**
         * @param actorName the name of the manager actor
         * @return {@code this} for fluent-style usage
         */
        public Builder setActorName(final String actorName) {
            Objects.requireNonNull(actorName);
            Preconditions.checkArgument(StringUtils.isNotBlank(actorName), "Actor name cannot be blank");
            this.actorName = Optional.of(actorName);
            return this;
        }

        /**
         * @param dataType the type of data handled by the manager
         * @return {@code this} for fluent-style usage
         */
        public Builder setDataType(final DataType dataType) {
            this.dataType = Optional.of(Objects.requireNonNull(dataType));
            return this;
        }

        /**
         * @param maxFailures the maximum number of failures in the circuit breaker before the breaker is opened
         * @return {@code this} for fluent-style usage
         */
        public Builder setMaxFailures(final int maxFailures) {
            Preconditions.checkArgument(maxFailures > 0, "Maximum failures must be positive");
            this.maxFailures = Optional.of(maxFailures);
            return this;
        }

        /**
         * @param callTimeout when a call takes longer than this duration, it will be treated as an error
         * @return {@code this} for fluent-style usage
         */
        public Builder setCallTimeout(final FiniteDuration callTimeout) {
            this.callTimeout = Optional.of(Objects.requireNonNull(callTimeout));
            return this;
        }

        /**
         * @param resetTimeout when the circuit breaker is open, the duration to wait before switching to half-closed
         * @return {@code this} for fluent-style usage
         */
        public Builder setResetTimeout(final FiniteDuration resetTimeout) {
            this.resetTimeout = Optional.of(Objects.requireNonNull(resetTimeout));
            return this;
        }

        /**
         * @param actorConfigs the database actor configurations of the actors to be managed
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Collection<DatabaseActorConfig> actorConfigs) {
            this.actorConfigs.addAll(Objects.requireNonNull(actorConfigs));
            return this;
        }

        /**
         * @param actorConfigs the database actor configurations of the actors to be managed
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final DatabaseActorConfig... actorConfigs) {
            return add(Arrays.asList(Objects.requireNonNull(actorConfigs)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("actorName")) {
                setActorName(json.getAsJsonPrimitive("actorName").getAsString());
            }
            if (json.has("dataType")) {
                setDataType(DataType.valueOf(json.getAsJsonPrimitive("dataType").getAsString()));
            }
            if (json.has("maxFailures")) {
                setMaxFailures(json.getAsJsonPrimitive("maxFailures").getAsInt());
            }
            if (json.has("callTimeout")) {
                setCallTimeout(
                        Duration.create(json.getAsJsonPrimitive("callTimeout").getAsLong(), TimeUnit.MILLISECONDS));
            }
            if (json.has("resetTimeout")) {
                setResetTimeout(
                        Duration.create(json.getAsJsonPrimitive("resetTimeout").getAsLong(), TimeUnit.MILLISECONDS));
            }
            if (json.has("actorConfigs")) {
                json.getAsJsonArray("actorConfigs").forEach(
                        e -> add(new DatabaseActorConfig.Builder().fromJson(mapping, e.getAsJsonObject()).build()));
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DatabaseManagerConfig build() {
            if (!this.actorName.isPresent()) {
                throw new IllegalStateException("Actor name is required");
            }
            if (!this.dataType.isPresent()) {
                throw new IllegalStateException("Data type is required");
            }
            if (!this.maxFailures.isPresent()) {
                throw new IllegalStateException("Max failures is required");
            }
            if (!this.callTimeout.isPresent()) {
                throw new IllegalStateException("Call timeout is required");
            }
            if (!this.resetTimeout.isPresent()) {
                throw new IllegalStateException("Reset timeout is required");
            }
            if (this.actorConfigs.isEmpty()) {
                throw new IllegalStateException("At least one database actor configuration is required");
            }

            return new DatabaseManagerConfig(this.actorName.get(), this.dataType.get(), this.maxFailures.get(),
                    this.callTimeout.get(), this.resetTimeout.get(), this.actorConfigs);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSerializationManifest() {
            return SERIALIZATION_MANIFEST;
        }
    }
}
