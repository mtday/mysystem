package mysystem.db.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;
import mysystem.common.serialization.ManifestMapping;
import mysystem.common.util.CollectionComparator;
import mysystem.common.util.OptionalComparator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable class that represents the information needed to fetch objects with specific unique ids from a table
 * in the database.
 */
public class GetById implements Model, HasDataType, Comparable<GetById> {
    private final static String SERIALIZATION_MANIFEST = GetById.class.getSimpleName();

    private final DataType dataType;
    private final SortedSet<Integer> ids;
    private final Optional<Boolean> active;

    /**
     * @param dataType the type of data that should be retrieved using the request object
     * @param ids the unique identifiers of the objects to fetch
     * @param active return whether only active objects should be retrieved (present and true), or only inactive
     * objects (present and false), or all objects regardless (empty)
     */
    private GetById(final DataType dataType, final SortedSet<Integer> ids, final Optional<Boolean> active) {
        this.dataType = dataType;
        this.ids = new TreeSet<>(ids);
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
     * @return an unmodifiable set containing the unique identifiers of the objects to fetch
     */
    public SortedSet<Integer> getIds() {
        return Collections.unmodifiableSortedSet(this.ids);
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
        final JsonArray idsArr = new JsonArray();
        getIds().forEach(idsArr::add);

        final JsonObject json = new JsonObject();
        json.addProperty("dataType", getDataType().name());
        if (getActive().isPresent()) {
            json.addProperty("active", getActive().get());
        }
        json.add("ids", idsArr);
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
        str.append("ids", getIds());
        str.append("active", getActive());
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
        cmp.append(getActive(), other.getActive(), new OptionalComparator<Boolean>());
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
        hash.append(getActive());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link GetById} instances.
     */
    public static class Builder implements ModelBuilder<GetById> {
        private Optional<DataType> dataType = Optional.empty();
        private Optional<Boolean> active = Optional.empty();
        private final SortedSet<Integer> ids = new TreeSet<>();

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
         * @param ids the unique identifiers of the objects to fetch
         */
        public Builder(final DataType dataType, final Integer... ids) {
            this(dataType, Arrays.asList(Objects.requireNonNull(ids)));
        }

        /**
         * @param dataType the {@link DataType} describing the type of data for which this database request applies
         * @param ids the unique identifiers of the objects to fetch
         */
        public Builder(final DataType dataType, final Collection<Integer> ids) {
            setDataType(dataType);
            add(ids);
        }

        /**
         * @param dataType the {@link DataType} describing the type of data for which this delete request applies
         * @return {@code this} for fluent-style usage
         */
        public Builder setDataType(final DataType dataType) {
            this.dataType = Optional.of(Objects.requireNonNull(dataType));
            return this;
        }

        /**
         * @param ids the unique identifiers of the objects to fetch
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Integer... ids) {
            return add(Arrays.asList(Objects.requireNonNull(ids)));
        }

        /**
         * @param ids the unique identifiers of the objects to fetch
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Collection<Integer> ids) {
            this.ids.addAll(Objects.requireNonNull(ids));
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
            if (json.has("ids")) {
                json.getAsJsonArray("ids").forEach(e -> add(e.getAsInt()));
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public GetById build() {
            if (!this.dataType.isPresent()) {
                throw new IllegalStateException("Data type is required");
            }
            if (this.ids.isEmpty()) {
                throw new IllegalStateException("At least one id is required");
            }

            return new GetById(this.dataType.get(), this.ids, this.active);
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
