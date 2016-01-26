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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;

/**
 * An immutable class that represents the information needed to delete objects with specific unique ids from a table
 * in the database.
 */
public class DeleteById implements Model, HasDataType, Comparable<DeleteById> {
    private final static String SERIALIZATION_MANIFEST = DeleteById.class.getSimpleName();

    private final DataType dataType;
    private final SortedSet<Integer> ids;

    /**
     * @param dataType the type of data that should be retrieved using the request object
     * @param ids the unique identifiers of the objects to fetch
     */
    private DeleteById(final DataType dataType, final SortedSet<Integer> ids) {
        this.dataType = dataType;
        this.ids = new TreeSet<>(ids);
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
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonArray idsArr = new JsonArray();
        getIds().forEach(idsArr::add);

        final JsonObject json = new JsonObject();
        json.addProperty("dataType", getDataType().name());
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
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final DeleteById other) {
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
        return (other instanceof DeleteById) && compareTo((DeleteById) other) == 0;
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
     * Used to create {@link DeleteById} instances.
     */
    public static class Builder implements ModelBuilder<DeleteById> {
        private Optional<DataType> dataType = Optional.empty();
        private final SortedSet<Integer> ids = new TreeSet<>();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param dataType the {@link DataType} describing the type of data for which this delete request applies
         */
        public Builder(final DataType dataType) {
            setDataType(dataType);
        }

        /**
         * @param dataType the {@link DataType} describing the type of data for which this delete request applies
         * @param ids the unique identifiers of the objects to fetch
         */
        public Builder(final DataType dataType, final Integer... ids) {
            this(dataType, Arrays.asList(Objects.requireNonNull(ids)));
        }

        /**
         * @param dataType the {@link DataType} describing the type of data for which this delete request applies
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
         * @param ids the unique identifiers of the objects to be deleted
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Integer... ids) {
            return add(Arrays.asList(Objects.requireNonNull(ids)));
        }

        /**
         * @param ids the unique identifiers of the objects to be deleted
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Collection<Integer> ids) {
            this.ids.addAll(Objects.requireNonNull(ids));
            return this;
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
            if (json.has("ids")) {
                json.getAsJsonArray("ids").forEach(e -> add(e.getAsInt()));
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DeleteById build() {
            if (!this.dataType.isPresent()) {
                throw new IllegalStateException("Data type is required");
            }
            if (this.ids.isEmpty()) {
                throw new IllegalStateException("At least one id is required");
            }

            return new DeleteById(this.dataType.get(), this.ids);
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
