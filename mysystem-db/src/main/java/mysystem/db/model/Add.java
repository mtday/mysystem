package mysystem.db.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
 * An immutable class that represents the information needed to add model objects to a table in the database.
 */
public class Add<M extends Model> implements HasDataType, Comparable<Add<M>>, Serializable {
    private final static long serialVersionUID = 1L;

    private final DataType dataType;
    private final SortedSet<M> models;

    /**
     * @param dataType the type of data to be added to the database
     * @param models the model objects to add to the database
     */
    private Add(final DataType dataType, final SortedSet<M> models) {
        this.dataType = dataType;
        this.models = new TreeSet<>(models);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType getDataType() {
        return this.dataType;
    }

    /**
     * @return an unmodifiable set containing the model objects to add to the database
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
        str.append("dataType", getDataType());
        str.append("models", getModels());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Add<M> other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getDataType(), other.getDataType());
        cmp.append(getModels(), other.getModels(), new CollectionComparator<M>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object other) {
        return (other instanceof Add) && compareTo((Add<M>) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getDataType().name());
        hash.append(getModels());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link Add} instances.
     */
    public static class Builder<M extends Model> {
        private final DataType dataType;
        private final SortedSet<M> models = new TreeSet<>();

        /**
         * @param dataType the {@link DataType} describing the type of data to be added to the database
         */
        public Builder(final DataType dataType) {
            this.dataType = Objects.requireNonNull(dataType);
        }

        /**
         * @param dataType the {@link DataType} describing the type of data to be added to the database
         * @param models the model objects to be added to the database
         */
        @SafeVarargs
        public Builder(final DataType dataType, final M... models) {
            this(dataType, Arrays.asList(Objects.requireNonNull(models)));
        }

        /**
         * @param dataType the {@link DataType} describing the type of data to be added to the database
         * @param models the model objects to be added to the database
         */
        public Builder(final DataType dataType, final Collection<M> models) {
            this.dataType = Objects.requireNonNull(dataType);
            this.models.addAll(Objects.requireNonNull(models));
        }

        /**
         * @param models the model objects to be added to the database
         * @return {@code this} for fluent-style usage
         */
        @SafeVarargs
        public final Builder<M> add(final M... models) {
            return add(Arrays.asList(Objects.requireNonNull(models)));
        }

        /**
         * @param models the model objects to be added to the database
         * @return {@code this} for fluent-style usage
         */
        public Builder<M> add(final Collection<M> models) {
            this.models.addAll(Objects.requireNonNull(models));
            return this;
        }

        /**
         * @return the {@link Add} represented by this builder
         */
        public Add<M> build() {
            if (this.models.isEmpty()) {
                throw new IllegalStateException("At least one model object is required");
            }

            return new Add<>(this.dataType, this.models);
        }
    }
}
