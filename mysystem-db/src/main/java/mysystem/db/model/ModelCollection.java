package mysystem.db.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.model.Model;
import mysystem.common.util.CollectionComparator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable class that represents the company objects retrieved from the database.
 */
public class ModelCollection<M extends Model> implements Comparable<ModelCollection<M>>, Serializable {
    private final static long serialVersionUID = 1L;

    private final SortedSet<M> models;

    /**
     * @param models the model objects retrieved from the database
     */
    private ModelCollection(final SortedSet<M> models) {
        this.models = new TreeSet<>(models);
    }

    /**
     * @return an unmodifiable set containing the model objects retrieved from the database
     */
    public SortedSet<M> getModels() {
        return Collections.unmodifiableSortedSet(this.models);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("models", getModels());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final ModelCollection other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getModels(), other.getModels(), new CollectionComparator<M>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof ModelCollection) && compareTo((ModelCollection) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getModels().hashCode();
    }

    /**
     * Used to create {@link ModelCollection} instances.
     */
    public static class Builder<M extends Model> {
        private final SortedSet<M> models = new TreeSet<>();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param models the model objects retrieved from the database
         */
        @SafeVarargs
        public Builder(final M... models) {
            this(Arrays.asList(Objects.requireNonNull(models)));
        }

        /**
         * @param models the company objects retrieved from the database
         */
        public Builder(final Collection<M> models) {
            this.models.addAll(Objects.requireNonNull(models));
        }

        /**
         * @param models the company objects retrieved from the database
         * @return {@code this} for fluent-style usage
         */
        @SafeVarargs
        public final Builder<M> add(final M... models) {
            return add(Arrays.asList(Objects.requireNonNull(models)));
        }

        /**
         * @param models the company objects retrieved from the database
         * @return {@code this} for fluent-style usage
         */
        public Builder<M> add(final Collection<M> models) {
            this.models.addAll(Objects.requireNonNull(models));
            return this;
        }

        /**
         * @return the {@link ModelCollection} represented by this builder
         */
        public ModelCollection<M> build() {
            return new ModelCollection<>(this.models);
        }
    }
}
