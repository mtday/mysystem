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

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable object representing a command configuration to be made available in the shell.
 */
public class CommandConfig implements Model, Comparable<CommandConfig> {
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
    public int compareTo(final CommandConfig other) {
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
        private Optional<String> commandName;
        private Optional<Class<? extends UntypedActor>> commandClass;

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
            this.commandName = Optional.of(other.getCommandName());
            this.commandClass = Optional.of(other.getCommandClass());
        }

        /**
         * @param commandName the name of the command as defined in the configuration
         * @param commandConfig the configuration defined for the command
         */
        public Builder(final String commandName, final Config commandConfig) {
            this.commandName = Optional.of(Objects.requireNonNull(commandName));

            if (Objects.requireNonNull(commandConfig).hasPath("class")) {
                final String className = commandConfig.getString("class");
                try {
                    this.commandClass = Optional.of(Class.forName(className).asSubclass(UntypedActor.class));
                } catch (final ClassNotFoundException notFound) {
                    throw new IllegalArgumentException("Shell command class not found: " + className);
                }
            } else {
                throw new IllegalArgumentException("Shell command config must specify a class: " + commandName);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final JsonObject json) {
            Objects.requireNonNull(json);
            this.commandName = Optional.of(json.getAsJsonPrimitive("commandName").getAsString());

            final String className = json.getAsJsonPrimitive("commandClass").getAsString();
            try {
                this.commandClass = Optional.of(Class.forName(className).asSubclass(UntypedActor.class));
            } catch (final ClassNotFoundException notFound) {
                throw new IllegalArgumentException("Shell command class not found: " + className);
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
    }
}
