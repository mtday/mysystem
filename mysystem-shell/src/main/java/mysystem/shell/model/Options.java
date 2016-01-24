package mysystem.shell.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.util.CollectionComparator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable class used to manage the options available to a command.
 */
public class Options implements Model, Comparable<Options> {
    private final SortedSet<Option> options = new TreeSet<>();

    /**
     * @param options the individual option objects supported for the command
     */
    private Options(final Set<Option> options) {
        this.options.addAll(options);
    }

    /**
     * @return an unmodifiable sorted set of the options supported for the command
     */
    public SortedSet<Option> getOptions() {
        return Collections.unmodifiableSortedSet(this.options);
    }

    /**
     * @return the commons-cli options implementation corresponding to this object
     */
    public org.apache.commons.cli.Options asOptions() {
        final org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        getOptions().forEach(o -> options.addOption(o.asOption()));
        return options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonArray optionArr = new JsonArray();
        getOptions().forEach(o -> optionArr.add(o.toJson()));

        final JsonObject json = new JsonObject();
        json.add("options", optionArr);
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("options", getOptions());
        return str.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Options other) {
        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getOptions(), other.getOptions(), new CollectionComparator<Option>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof Options) && compareTo((Options) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getOptions());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link Options} objects.
     */
    public static class Builder implements ModelBuilder<Options> {
        private final SortedSet<Option> options = new TreeSet<>();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param options the additional option objects to add
         */
        public Builder(final Option... options) {
            add(Arrays.asList(Objects.requireNonNull(options)));
        }

        /**
         * @param options the additional option objects to add
         */
        public Builder(final Collection<Option> options) {
            add(Objects.requireNonNull(options));
        }

        /**
         * @param options the additional option objects to add
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Option... options) {
            return add(Arrays.asList(Objects.requireNonNull(options)));
        }

        /**
         * @param options the additional option objects to add
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Collection<Option> options) {
            this.options.addAll(Objects.requireNonNull(options));
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final JsonObject json) {
            if (Objects.requireNonNull(json).has("options")) {
                json.getAsJsonArray("options")
                        .forEach(e -> add(new Option.Builder().fromJson(e.getAsJsonObject()).build()));
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Options build() {
            if (this.options.isEmpty()) {
                throw new IllegalStateException("At least one option is required");
            }

            return new Options(this.options);
        }
    }
}
