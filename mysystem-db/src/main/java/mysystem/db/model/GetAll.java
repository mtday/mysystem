package mysystem.db.model;

import com.google.common.base.Optional;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.util.OptionalComparator;

import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable class that represents the information needed to fetch all objects from a table in the database.
 */
public class GetAll implements HasDataType, Comparable<GetAll>, Serializable {
    private final static long serialVersionUID = 1L;

    private final DataType dataType;
    private final Optional<Boolean> active;

    /**
     * @param dataType the type of data that should be retrieved using the request object
     * @param active return whether only active objects should be retrieved (present and true), or only inactive
     * objects (present and false), or all objects regardless (empty)
     */
    private GetAll(final DataType dataType, final Optional<Boolean> active) {
        this.dataType = dataType;
        this.active = active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType getDataType() {
        return this.dataType;
    }

    /**
     * @return whether only active objects should be retrieved (present and true), or only inactive objects (present
     * and false), or all objects regardless (empty)
     */
    public Optional<Boolean> getActive() {
        return this.active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("dataType", getDataType());
        str.append("active", getActive());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final GetAll other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getDataType(), other.getDataType());
        cmp.append(getActive(), other.getActive(), new OptionalComparator<Boolean>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof GetAll) && compareTo((GetAll) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getDataType().name());
        hash.append(getActive());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link GetAll} instances.
     */
    public static class Builder {
        private final DataType dataType;
        private Optional<Boolean> active = Optional.absent();

        /**
         * @param dataType the {@link DataType} describing the type of data for which this database request applies
         */
        public Builder(final DataType dataType) {
            this.dataType = Objects.requireNonNull(dataType);
        }

        /**
         * @param active the new value indicating whether only active or inactive objects should be retrieved (when
         * present), or all values should be retrieved (when empty)
         * @return {@code this} for fluent-style usage
         */
        public Builder setActive(final Optional<Boolean> active) {
            this.active = Objects.requireNonNull(active);
            return this;
        }

        /**
         * @param active the new value indicating whether only active or inactive objects should be retrieved
         * @return {@code this} for fluent-style usage
         */
        public Builder setActive(final boolean active) {
            return setActive(Optional.of(active));
        }

        /**
         * @return the {@link GetAll} represented by this builder
         */
        public GetAll build() {
            return new GetAll(this.dataType, this.active);
        }
    }
}
