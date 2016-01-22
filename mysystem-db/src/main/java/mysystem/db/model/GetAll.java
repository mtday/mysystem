package mysystem.db.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable class that represents the information needed to fetch all objects from a table in the database.
 */
public class GetAll implements HasDataType, Comparable<GetAll>, Serializable {
    private final static long serialVersionUID = 1L;

    private final DataType dataType;

    /**
     * @param dataType the type of data that should be retrieved using the request object
     */
    private GetAll(final DataType dataType) {
        this.dataType = dataType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType getDataType() {
        return this.dataType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("dataType", getDataType());
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

        return getDataType().compareTo(other.getDataType());
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
        return getDataType().name().hashCode();
    }

    /**
     * Used to create {@link GetAll} instances.
     */
    public static class Builder {
        private final DataType dataType;

        /**
         * @param dataType the {@link DataType} describing the type of data for which this database request applies
         */
        public Builder(final DataType dataType) {
            this.dataType = Objects.requireNonNull(dataType);
        }

        /**
         * @return the {@link GetAll} represented by this builder
         */
        public GetAll build() {
            return new GetAll(this.dataType);
        }
    }
}
