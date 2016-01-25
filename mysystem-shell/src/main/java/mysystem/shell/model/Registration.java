package mysystem.shell.model;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import akka.actor.ActorRef;
import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable representation of a command registration available for use within the shell.
 */
public class Registration implements Model, Comparable<Registration> {
    private final static String SERIALIZATION_MANIFEST = Registration.class.getSimpleName();

    private final String actorPath;
    private final CommandPath path;
    private final Optional<Options> options;
    private final Optional<String> description;

    /**
     * @param actorPath the path to the actor that implements the command
     * @param path the fully qualified path to the command
     * @param options the options available for the command
     * @param description a description of the command defined in this registration
     */
    private Registration(final String actorPath, final CommandPath path, final Optional<Options> options,
            final Optional<String> description) {
        this.actorPath = actorPath;
        this.path = path;
        this.options = options;
        this.description = description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
    }

    /**
     * @return the path to the actor that implements the command
     */
    public String getActorPath() {
        return this.actorPath;
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
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("actorPath", getActorPath());
        json.add("path", getPath().toJson());
        if (getOptions().isPresent()) {
            json.add("options", getOptions().get().toJson());
        }
        if (getDescription().isPresent()) {
            json.addProperty("description", getDescription().get());
        }
        json.addProperty("manifest", getSerializationManifest());
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("actorPath", getActorPath());
        str.append("path", getPath());
        str.append("options", getOptions());
        str.append("description", getDescription());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Registration registration) {
        if (registration == null) {
            return 1;
        }

        return getPath().compareTo(registration.getPath());
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
     * Used to create {@link Registration} instances.
     */
    public static class Builder implements ModelBuilder<Registration> {
        private Optional<String> actorPath = Optional.empty();
        private Optional<CommandPath> path = Optional.empty();
        private Optional<Options> options = Optional.empty();
        private Optional<String> description = Optional.empty();

        /**
         * {@inheritDoc}
         */
        public Builder() {
        }

        /**
         * @param registration the registration to duplicate
         */
        public Builder(final Registration registration) {
            setActorPath(Objects.requireNonNull(registration).getActorPath());
            setPath(registration.getPath());

            final Optional<Options> options = registration.getOptions();
            if (options.isPresent()) {
                setOptions(options.get());
            }
            final Optional<String> description = registration.getDescription();
            if (description.isPresent()) {
                setDescription(description.get());
            }
        }

        /**
         * @param actorRef a reference to the actor that implements the command
         * @return {@code this} for fluent-style usage
         */
        public Builder setActorPath(final ActorRef actorRef) {
            this.actorPath = Optional.of(Objects.requireNonNull(actorRef).path().toSerializationFormat());
            return this;
        }

        /**
         * @param actorPath the path to the actor that implements the command
         * @return {@code this} for fluent-style usage
         */
        public Builder setActorPath(final String actorPath) {
            this.actorPath = Optional.of(Objects.requireNonNull(actorPath));
            return this;
        }

        /**
         * @param path the fully qualified path to the command
         * @return {@code this} for fluent-style usage
         */
        public Builder setPath(final CommandPath path) {
            this.path = Optional.of(Objects.requireNonNull(path));
            return this;
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
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("actorPath")) {
                setActorPath(json.getAsJsonPrimitive("actorPath").getAsString());
            }
            if (json.has("path")) {
                setPath(new CommandPath.Builder().fromJson(mapping, json.getAsJsonObject("path")).build());
            }
            if (json.has("options")) {
                setOptions(new Options.Builder().fromJson(mapping, json.getAsJsonObject("options")).build());
            }
            if (json.has("description")) {
                setDescription(json.getAsJsonPrimitive("description").getAsString());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Registration build() {
            if (!this.actorPath.isPresent()) {
                throw new IllegalStateException("Registration requires an actor path");
            }
            if (!this.path.isPresent()) {
                throw new IllegalStateException("Registration requires a command path");
            }

            return new Registration(this.actorPath.get(), this.path.get(), this.options, this.description);
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
