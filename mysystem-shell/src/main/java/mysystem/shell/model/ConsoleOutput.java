package mysystem.shell.model;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;
import mysystem.common.util.OptionalComparator;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable representation of output to be sent to the shell display.
 */
public class ConsoleOutput implements Model, Comparable<ConsoleOutput> {
    private final static String SERIALIZATION_MANIFEST = ConsoleOutput.class.getSimpleName();

    private final Optional<String> output;
    private final boolean hasMore;
    private final boolean terminate;

    /**
     * @param output the output text to be sent to the shell display
     * @param hasMore whether more output will follow in subsequent messages
     * @param terminate whether the shell should terminate after displaying this output
     */
    private ConsoleOutput(final Optional<String> output, final boolean hasMore, final boolean terminate) {
        this.output = output;
        this.hasMore = hasMore;
        this.terminate = terminate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
    }

    /**
     * @return the output text to be sent to the shell display
     */
    public Optional<String> getOutput() {
        return this.output;
    }

    /**
     * @return whether more output will follow in subsequent messages
     */
    public boolean hasMore() {
        return this.hasMore;
    }

    /**
     * @return whether the shell should terminate after displaying this output
     */
    public boolean isTerminate() {
        return this.terminate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        if (getOutput().isPresent()) {
            json.addProperty("output", getOutput().get());
        }
        json.addProperty("hasMore", hasMore());
        json.addProperty("terminate", isTerminate());
        json.addProperty("manifest", getSerializationManifest());
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("output", getOutput());
        str.append("hasMore", hasMore());
        str.append("terminate", isTerminate());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final ConsoleOutput other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getOutput(), other.getOutput(), new OptionalComparator());
        cmp.append(hasMore(), other.hasMore());
        cmp.append(isTerminate(), other.isTerminate());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof ConsoleOutput) && compareTo((ConsoleOutput) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getOutput());
        hash.append(hasMore());
        hash.append(isTerminate());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link ConsoleOutput} instances.
     */
    public static class Builder implements ModelBuilder<ConsoleOutput> {
        private Optional<String> output = Optional.empty();
        private boolean hasMore = false;
        private boolean terminate = false;

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Copy constructor.
         *
         * @param other the {@link ConsoleOutput} to duplicate
         */
        public Builder(final ConsoleOutput other) {
            this.output = Objects.requireNonNull(other).getOutput();
        }

        /**
         * @param output the output to be sent to the shell display
         */
        public Builder(final String output) {
            this.output = Optional.of(Objects.requireNonNull(output));
        }

        /**
         * @param hasMore whether more output will follow in subsequent messages
         * @return {@code this} for fluent-style usage
         */
        public Builder setHasMore(final boolean hasMore) {
            this.hasMore = hasMore;
            return this;
        }

        /**
         * @param terminate whether the shell should terminate after displaying the output
         * @return {@code this} for fluent-style usage
         */
        public Builder setTerminate(final boolean terminate) {
            this.terminate = terminate;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("output")) {
                this.output = Optional.of(json.getAsJsonPrimitive("output").getAsString());
            }
            if (json.has("hasMore")) {
                this.hasMore = json.getAsJsonPrimitive("hasMore").getAsBoolean();
            }
            if (json.has("terminate")) {
                this.terminate = json.getAsJsonPrimitive("terminate").getAsBoolean();
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConsoleOutput build() {
            return new ConsoleOutput(this.output, this.hasMore, this.terminate);
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
