package mysystem.db.model;

import com.typesafe.config.Config;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import akka.actor.UntypedActor;

import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable object representing the configuration for the actor that manages a database operation.
 */
public class DatabaseActorConfig implements Comparable<DatabaseActorConfig>, Serializable {
    private final static long serialVersionUID = 1L;

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
    public static class Builder {
        private final String actorName;
        private final Class<? extends UntypedActor> actorClass;
        private final Class<?> messageClass;

        /**
         * @param other the {@link DatabaseActorConfig} to duplicate
         */
        public Builder(final DatabaseActorConfig other) {
            Objects.requireNonNull(other);
            this.actorName = other.getActorName();
            this.actorClass = other.getActorClass();
            this.messageClass = other.getMessageClass();
        }

        /**
         * @param actorName the name of the actor as defined in the configuration
         * @param actorConfig the configuration defined for the actor
         */
        public Builder(final String actorName, final Config actorConfig) {
            this.actorName = Objects.requireNonNull(actorName);

            if (Objects.requireNonNull(actorConfig).hasPath("actor-class")) {
                final String className = actorConfig.getString("actor-class");
                try {
                    this.actorClass = Class.forName(className).asSubclass(UntypedActor.class);
                } catch (final ClassNotFoundException notFound) {
                    throw new IllegalArgumentException("Database actor class not found: " + className);
                }
            } else {
                throw new IllegalArgumentException("Database actor config must specify an actor class: " + actorName);
            }

            if (Objects.requireNonNull(actorConfig).hasPath("message-class")) {
                final String className = actorConfig.getString("message-class");
                try {
                    this.messageClass = Class.forName(className);
                } catch (final ClassNotFoundException notFound) {
                    throw new IllegalArgumentException("Database actor message class not found: " + className);
                }
            } else {
                throw new IllegalArgumentException("Database actor config must specify a message class: " + actorName);
            }
        }

        /**
         * @return the {@link DatabaseActorConfig} object
         */
        public DatabaseActorConfig build() {
            return new DatabaseActorConfig(this.actorName, this.actorClass, this.messageClass);
        }
    }
}
