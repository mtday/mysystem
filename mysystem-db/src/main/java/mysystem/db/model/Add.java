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

/**
 * An immutable class that represents the information needed to add model objects to a table in the database.
 */
public class Add<M extends Model> implements Model, HasDataType, Comparable<Add<M>> {
    private final String manifest;
    private final DataType dataType;
    private final SortedSet<M> models;

    /**
     * @param manifest the serialization manifest value used to determine the type of model objects in this collection
     * @param dataType the type of data to be added to the database
     * @param models the model objects to add to the database
     */
    private Add(final String manifest, final DataType dataType, final SortedSet<M> models) {
        this.manifest = manifest;
        this.dataType = dataType;
        this.models = new TreeSet<>(models);
    }

    /**
     * @return the serialization manifest value used to determine the type of model objects in this collection
     */
    protected String getManifest() {
        return this.manifest;
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
    public JsonObject toJson() {
        final JsonArray modelArr = new JsonArray();
        getModels().forEach(m -> modelArr.add(m.toJson()));

        final JsonObject json = new JsonObject();
        json.addProperty("manifest", getManifest());
        json.addProperty("dataType", getDataType().name());
        json.add("models", modelArr);
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("manifest", getManifest());
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
        cmp.append(getManifest(), other.getManifest());
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
        hash.append(getManifest());
        hash.append(getDataType().name());
        hash.append(getModels());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link Add} instances.
     */
    public static class Builder<M extends Model> implements ModelBuilder<Add<M>> {
        private final ManifestMapping manifestMapping = new ManifestMapping();
        private Optional<DataType> dataType = Optional.empty();
        private final SortedSet<M> models = new TreeSet<>();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param dataType the {@link DataType} describing the type of data to be added to the database
         */
        public Builder(final DataType dataType) {
            setDataType(dataType);
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
            setDataType(dataType);
            this.models.addAll(Objects.requireNonNull(models));
        }

        /**
         * @param dataType the {@link DataType} describing the type of data to be added to the database
         * @return {@code this} for fluent-style usage
         */
        public Builder<M> setDataType(final DataType dataType) {
            this.dataType = Optional.of(Objects.requireNonNull(dataType));
            return this;
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
         * @return the serialization manifest type for the model objects in this collection
         */
        protected Optional<String> getManifest() {
            if (this.models.isEmpty()) {
                return Optional.empty();
            }
            return this.manifestMapping.getManifest(this.models.first().getClass());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public Builder<M> fromJson(final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("dataType")) {
                setDataType(DataType.valueOf(json.getAsJsonPrimitive("dataType").getAsString()));
            }
            if (json.has("models") && json.has("manifest")) {
                final Optional<ModelBuilder<?>> builder =
                        this.manifestMapping.getBuilder(json.getAsJsonPrimitive("manifest").getAsString());
                if (builder.isPresent()) {
                    json.getAsJsonArray("models")
                            .forEach(e -> add((M) builder.get().fromJson(e.getAsJsonObject()).build()));
                }
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Add<M> build() {
            if (!this.dataType.isPresent()) {
                throw new IllegalStateException("The data type is required");
            }
            if (this.models.isEmpty()) {
                throw new IllegalStateException("At least one model object is required");
            }

            final Optional<String> manifest = getManifest();
            if (!manifest.isPresent()) {
                throw new IllegalStateException("Unable to determine manifest for model objects");
            }

            return new Add<>(manifest.get(), this.dataType.get(), this.models);
        }
    }
}
