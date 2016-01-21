package mysystem.db.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.util.CollectionComparator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable class that represents the information needed to fetch company objects from the database.
 */
public class GetById implements HasDataType, Comparable<GetById>, Serializable {
    private final static long serialVersionUID = 1L;

    private final DataType dataType;
    private final SortedSet<Integer> ids;

    /**
     * @param dataType the type of data that should be retrieved using the request object
     * @param ids the unique identifiers of the company objects to fetch
     */
    private GetById(final DataType dataType, final SortedSet<Integer> ids) {
        this.dataType = dataType;
        this.ids = new TreeSet<>(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType getDataType() {
        return this.dataType;
    }

    /**
     * @return an unmodifiable set containing the unique identifiers of the company objects to fetch
     */
    public SortedSet<Integer> getIds() {
        return Collections.unmodifiableSortedSet(this.ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("dataType", getDataType());
        str.append("ids", getIds());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final GetById other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getDataType(), other.getDataType());
        cmp.append(getIds(), other.getIds(), new CollectionComparator<Integer>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof GetById) && compareTo((GetById) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getDataType().name());
        hash.append(getIds());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link GetById} instances.
     */
    public static class Builder {
        private final DataType dataType;
        private final SortedSet<Integer> ids = new TreeSet<>();

        /**
         * @param dataType the {@link DataType} describing the type of data for which this database request applies
         */
        public Builder(final DataType dataType) {
            this.dataType = Objects.requireNonNull(dataType);
        }

        /**
         * @param dataType the {@link DataType} describing the type of data for which this database request applies
         * @param ids the unique identifiers of the company objects to fetch
         */
        public Builder(final DataType dataType, final Integer... ids) {
            this(dataType, Arrays.asList(Objects.requireNonNull(ids)));
        }

        /**
         * @param dataType the {@link DataType} describing the type of data for which this database request applies
         * @param ids the unique identifiers of the company objects to fetch
         */
        public Builder(final DataType dataType, final Collection<Integer> ids) {
            this.dataType = Objects.requireNonNull(dataType);
            this.ids.addAll(Objects.requireNonNull(ids));
        }

        /**
         * @param ids the unique identifiers of the company objects to fetch
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Integer... ids) {
            return add(Arrays.asList(Objects.requireNonNull(ids)));
        }

        /**
         * @param ids the unique identifiers of the company objects to fetch
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Collection<Integer> ids) {
            this.ids.addAll(Objects.requireNonNull(ids));
            return this;
        }

        /**
         * @return the {@link GetById} represented by this builder
         */
        public GetById build() {
            if (this.ids.isEmpty()) {
                throw new IllegalStateException("At least one id is required");
            }

            return new GetById(this.dataType, this.ids);
        }
    }
}
