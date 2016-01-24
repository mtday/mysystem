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
 * An immutable class that represents the company objects retrieved from the database.
 */
public class ModelCollection<M extends Model> implements Model, Comparable<ModelCollection<M>> {
    private final Optional<String> manifest;
    private final SortedSet<M> models = new TreeSet<>();

    /**
     * @param manifest the serialization manifest that describes the type of model objects in this collection,
     * possibly empty if there are no model objects in the collection
     * @param models the model objects retrieved from the database
     */
    private ModelCollection(final Optional<String> manifest, final SortedSet<M> models) {
        this.manifest = manifest;
        this.models.addAll(models);
    }

    /**
     * @return the serialization manifest that describes the type of model objects in this collection, possibly empty
     * if there are no model objects in the collection
     */
    protected Optional<String> getManifest() {
        return this.manifest;
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
    public JsonObject toJson() {
        final JsonArray modelArr = new JsonArray();
        getModels().forEach(m -> modelArr.add(m.toJson()));

        final JsonObject json = new JsonObject();
        json.add("models", modelArr);
        if (getManifest().isPresent()) {
            json.addProperty("manifest", getManifest().get());
        }
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("manifest", getManifest());
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
        cmp.append(getManifest(), other.getManifest(), new OptionalComparator<String>());
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
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getManifest());
        hash.append(getModels());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link ModelCollection} instances.
     */
    public static class Builder<M extends Model> implements ModelBuilder<ModelCollection<M>> {
        private final ManifestMapping manifestMapping = new ManifestMapping();
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
            add(models);
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
        public ModelCollection<M> build() {
            return new ModelCollection<>(getManifest(), this.models);
        }
    }
}
