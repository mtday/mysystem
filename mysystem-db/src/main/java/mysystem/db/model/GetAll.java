package mysystem.db.model;

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

import javax.annotation.Nullable;

/**
 * An immutable class that represents the information needed to fetch all objects from a table in the database.
 */
public class GetAll implements Model, HasDataType, Comparable<GetAll> {
    private final static String SERIALIZATION_MANIFEST = GetAll.class.getSimpleName();

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
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
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
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("dataType", getDataType().name());
        if (getActive().isPresent()) {
            json.addProperty("active", getActive().get());
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
        str.append("dataType", getDataType());
        str.append("active", getActive());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final GetAll other) {
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
    public static class Builder implements ModelBuilder<GetAll> {
        private Optional<DataType> dataType = Optional.empty();
        private Optional<Boolean> active = Optional.empty();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param dataType the {@link DataType} describing the type of data for which this database request applies
         */
        public Builder(final DataType dataType) {
            setDataType(dataType);
        }

        /**
         * @param dataType the {@link DataType} describing the type of data for which this database request applies
         * @return {@code this} for fluent-style usage
         */
        public Builder setDataType(final DataType dataType) {
            this.dataType = Optional.of(Objects.requireNonNull(dataType));
            return this;
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
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("dataType")) {
                setDataType(DataType.valueOf(json.getAsJsonPrimitive("dataType").getAsString()));
            }
            if (json.has("active")) {
                setActive(json.getAsJsonPrimitive("active").getAsBoolean());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public GetAll build() {
            if (!this.dataType.isPresent()) {
                throw new IllegalStateException("Data type is required");
            }

            return new GetAll(this.dataType.get(), this.active);
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
