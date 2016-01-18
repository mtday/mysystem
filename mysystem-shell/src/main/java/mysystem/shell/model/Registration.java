package mysystem.shell.model;

import org.apache.commons.cli.Options;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable representation of a command registration available for use within the shell.
 */
public class Registration implements Comparable<Registration>, Serializable {
    private final static long serialVersionUID = 1L;

    private final ActorRef actor;
    private final CommandPath path;
    private final Optional<Options> options;
    private final Optional<String> description;

    /**
     * @param actor a reference to the actor that implements the command
     * @param path the fully qualified path to the command
     * @param options the options available for the command
     * @param description a description of the command defined in this registration
     */
    private Registration(final ActorRef actor, final CommandPath path, final Optional<Options> options,
            final Optional<String> description) {
        this.actor = actor;
        this.path = path;
        this.options = options;
        this.description = description;
    }

    /**
     * @return a reference to the actor that implements the command
     */
    public ActorRef getActor() {
        return this.actor;
    }

    /**
     * @return the fully qualified path to the command
     */
    public CommandPath getPath() {
        return this.path;
    }

    /**
     * @return the options available for the command
     */
    public Optional<Options> getOptions() {
        return this.options;
    }

    /**
     * @return a description of the command defined in this registration
     */
    public Optional<String> getDescription() {
        return this.description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Registration command) {
        if (command == null) {
            return 1;
        }

        return getPath().compareTo(command.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof Registration) && compareTo((Registration) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("path", getPath());
        str.append("description", getDescription());
        return str.build();
    }

    /**
     * Used to create {@link Registration} instances.
     */
    public static class Builder {
        private final ActorRef actor;
        private final CommandPath path;
        private Optional<Options> options = Optional.empty();
        private Optional<String> description = Optional.empty();

        /**
         * @param actor a reference to the actor that implements the command
         * @param path the fully qualified path to the command
         */
        public Builder(final ActorRef actor, final CommandPath path) {
            this.actor = Objects.requireNonNull(actor);
            this.path = Objects.requireNonNull(path);
        }

        /**
         * @param actor a reference to the actor that implements the command
         * @param path the fully qualified path to the command
         * @param options the options that describe the configuration for the command
         */
        public Builder(final ActorRef actor, final CommandPath path, final Options options) {
            this.actor = Objects.requireNonNull(actor);
            this.path = Objects.requireNonNull(path);
            setOptions(options);
        }

        /**
         * @param actor a reference to the actor that implements the command
         * @param path the fully qualified path to the command
         * @param options the options that describe the configuration for the command
         * @param description a description of the command defined in this registration
         */
        public Builder(final ActorRef actor, final CommandPath path, final Options options, final String description) {
            this.actor = Objects.requireNonNull(actor);
            this.path = Objects.requireNonNull(path);
            setOptions(options);
            setDescription(description);
        }

        /**
         * @param registration the registration to duplicate
         */
        public Builder(final Registration registration) {
            this.actor = Objects.requireNonNull(registration).getActor();
            this.path = registration.getPath();

            final Optional<Options> options = registration.getOptions();
            if (options.isPresent()) {
                setOptions(options.get());
            }
        }

        /**
         * @param options the options that describe the configuration for the command
         * @return {@code this} for fluent-style usage
         */
        public Builder setOptions(final Options options) {
            this.options = Optional.of(Objects.requireNonNull(options));
            return this;
        }

        /**
         * @param description the description of the command defined in this registration
         * @return {@code this} for fluent-style usage
         */
        public Builder setDescription(final String description) {
            this.description = Optional.of(Objects.requireNonNull(description));
            return this;
        }

        /**
         * @return the {@link Registration} represented by this builder
         */
        public Registration build() {
            return new Registration(this.actor, this.path, this.options, this.description);
        }
    }
}
