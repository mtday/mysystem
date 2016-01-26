package mysystem.shell.model;

import com.google.gson.JsonObject;
import com.typesafe.config.Config;

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

import javax.annotation.Nullable;

/**
 * An immutable object representing a command configuration to be made available in the shell.
 */
public class CommandConfig implements Model, Comparable<CommandConfig> {
    private final static String SERIALIZATION_MANIFEST = CommandConfig.class.getSimpleName();

    private final String commandName;
    private final Class<? extends UntypedActor> commandClass;

    /**
     * @param commandName the name of the command as defined in the configuration
     * @param commandClass the class that implements the command
     */
    private CommandConfig(final String commandName, final Class<? extends UntypedActor> commandClass) {
        this.commandName = commandName;
        this.commandClass = commandClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
    }

    /**
     * @return the name of the command as defined in the configuration
     */
    public String getCommandName() {
        return this.commandName;
    }

    /**
     * @return the class that implements the command
     */
    public Class<? extends UntypedActor> getCommandClass() {
        return this.commandClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("commandName", getCommandName());
        json.addProperty("commandClass", getCommandClass().getName());
        json.addProperty("manifest", getSerializationManifest());
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("commandName", getCommandName());
        str.append("commandClass", getCommandClass().getName());
        return str.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final CommandConfig other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getCommandName(), other.getCommandName());
        cmp.append(getCommandClass().getName(), other.getCommandClass().getName());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof CommandConfig) && compareTo((CommandConfig) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getCommandName());
        hash.append(getCommandClass().getName());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link CommandConfig} objects.
     */
    public static class Builder implements ModelBuilder<CommandConfig> {
        private Optional<String> commandName = Optional.empty();
        private Optional<Class<? extends UntypedActor>> commandClass = Optional.empty();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param other the {@link CommandConfig} to duplicate
         */
        public Builder(final CommandConfig other) {
            Objects.requireNonNull(other);
            setCommandName(other.getCommandName());
            setCommandClass(other.getCommandClass());
        }

        /**
         * @param commandName the name of the command as defined in the configuration
         * @param commandConfig the configuration defined for the command
         */
        public Builder(final String commandName, final Config commandConfig) {
            setCommandName(Objects.requireNonNull(commandName));

            if (Objects.requireNonNull(commandConfig).hasPath("class")) {
                setCommandClass(commandConfig.getString("class"));
            }
        }

        /**
         * @param commandName the name of the command as defined in the configuration
         * @return {@code this} for fluent-style usage
         */
        public Builder setCommandName(final String commandName) {
            this.commandName = Optional.of(Objects.requireNonNull(commandName));
            return this;
        }

        /**
         * @param commandClass the class that specifies the command actor
         * @return {@code this} for fluent-style usage
         */
        public Builder setCommandClass(final Class<? extends UntypedActor> commandClass) {
            this.commandClass = Optional.of(Objects.requireNonNull(commandClass));
            return this;
        }

        /**
         * @param className the name of the class that specifies the command actor
         * @return {@code this} for fluent-style usage
         */
        public Builder setCommandClass(final String className) {
            try {
                setCommandClass(Class.forName(Objects.requireNonNull(className)).asSubclass(UntypedActor.class));
            } catch (final ClassNotFoundException notFound) {
                throw new IllegalArgumentException("Shell command class not found: " + className);
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("commandName")) {
                setCommandName(json.getAsJsonPrimitive("commandName").getAsString());
            }
            if (json.has("commandClass")) {
                setCommandClass(json.getAsJsonPrimitive("commandClass").getAsString());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CommandConfig build() {
            if (!this.commandName.isPresent()) {
                throw new IllegalStateException("The command name is required when building a command config");
            }
            if (!this.commandClass.isPresent()) {
                throw new IllegalStateException("The command class is required when building a command config");
            }

            return new CommandConfig(this.commandName.get(), this.commandClass.get());
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
