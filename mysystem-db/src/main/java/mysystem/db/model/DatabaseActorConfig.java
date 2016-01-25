package mysystem.db.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import akka.actor.UntypedActor;
import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable object representing the configuration for the actor that manages a database operation.
 */
public class DatabaseActorConfig implements Model, Comparable<DatabaseActorConfig> {
    private final static String SERIALIZATION_MANIFEST = DatabaseActorConfig.class.getSimpleName();

    private final String actorName;
    private final Class<? extends UntypedActor> actorClass;
    private final Class<?> messageClass;

    /**
     * @param actorName the name of the actor as defined in the configuration
     * @param actorClass the class that implements the database actor
     * @param messageClass the class representing the message type to be processed by the database actor
     */
    private DatabaseActorConfig(
            final String actorName, final Class<? extends UntypedActor> actorClass, final Class<?> messageClass) {
        this.actorName = actorName;
        this.actorClass = actorClass;
        this.messageClass = messageClass;
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
     * @return the class that implements the database actor
     */
    public Class<? extends UntypedActor> getActorClass() {
        return this.actorClass;
    }

    /**
     * @return the class that implements the database actor
     */
    public Class<?> getMessageClass() {
        return this.messageClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("actorName", getActorName());
        json.addProperty("actorClass", getActorClass().getName());
        json.addProperty("messageClass", getMessageClass().getName());
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
        str.append("actorClass", getActorClass().getName());
        str.append("messageClass", getMessageClass().getName());
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
        cmp.append(getMessageClass().getName(), other.getMessageClass().getName());
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
        hash.append(getMessageClass().getName());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link DatabaseActorConfig} objects.
     */
    public static class Builder implements ModelBuilder<DatabaseActorConfig> {
        private Optional<String> actorName = Optional.empty();
        private Optional<Class<? extends UntypedActor>> actorClass = Optional.empty();
        private Optional<Class<?>> messageClass = Optional.empty();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param other the {@link DatabaseActorConfig} to duplicate
         */
        public Builder(final DatabaseActorConfig other) {
            Objects.requireNonNull(other);
            this.actorName = Optional.of(other.getActorName());
            this.actorClass = Optional.of(other.getActorClass());
            this.messageClass = Optional.of(other.getMessageClass());
        }

        /**
         * @param actorName the name of the actor as defined in the configuration
         * @param actorConfig the configuration defined for the actor
         */
        public Builder(final String actorName, final Config actorConfig) {
            setActorName(actorName);

            if (Objects.requireNonNull(actorConfig).hasPath("actor-class")) {
                setActorClass(actorConfig.getString("actor-class"));
            } else {
                throw new IllegalArgumentException("Database actor config must specify an actor class: " + actorName);
            }

            if (Objects.requireNonNull(actorConfig).hasPath("message-class")) {
                setMessageClass(actorConfig.getString("message-class"));
            } else {
                throw new IllegalArgumentException("Database actor config must specify a message class: " + actorName);
            }
        }

        /**
         * @param actorName the name of the actor when deployed into an actor system
         * @return {@code this} for fluent-style usage
         */
        public Builder setActorName(final String actorName) {
            Objects.requireNonNull(actorName);
            Preconditions.checkArgument(StringUtils.isNotBlank(actorName), "Invalid actor name");
            this.actorName = Optional.of(actorName);
            return this;
        }

        /**
         * @param actorClass the name of the class indicating the actor that will process the messages
         * @return {@code this} for fluent-style usage
         */
        public Builder setActorClass(final String actorClass) {
            try {
                this.actorClass = Optional.of(Class.forName(Objects.requireNonNull(actorClass)).asSubclass(UntypedActor.class));
            } catch (final ClassNotFoundException notFound) {
                throw new IllegalArgumentException("Database actor class not found: " + actorClass);
            }
            return this;
        }

        /**
         * @param messageClass the name of the class indicating the message object type
         * @return {@code this} for fluent-style usage
         */
        public Builder setMessageClass(final String messageClass) {
            try {
                this.messageClass = Optional.of(Class.forName(Objects.requireNonNull(messageClass)));
            } catch (final ClassNotFoundException notFound) {
                throw new IllegalArgumentException("Database actor message class not found: " + messageClass);
            }
            return this;
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
            if (json.has("actorClass")) {
                setActorClass(json.getAsJsonPrimitive("actorClass").getAsString());
            }
            if (json.has("messageClass")) {
                setMessageClass(json.getAsJsonPrimitive("messageClass").getAsString());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DatabaseActorConfig build() {
            if (!this.actorName.isPresent()) {
                throw new IllegalArgumentException("The actor name is required");
            }
            if (!this.actorClass.isPresent()) {
                throw new IllegalArgumentException("The actor class is required");
            }
            if (!this.messageClass.isPresent()) {
                throw new IllegalArgumentException("The message class is required");
            }

            return new DatabaseActorConfig(this.actorName.get(), this.actorClass.get(), this.messageClass.get());
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
