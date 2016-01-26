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
 * An immutable class that represents the company objects retrieved from the database.
 */
public class ModelCollection<M extends Model> implements Model, Comparable<ModelCollection<M>> {
    private final static String SERIALIZATION_MANIFEST = ModelCollection.class.getSimpleName();

    private final SortedSet<M> models = new TreeSet<>();

    /**
     * @param models the model objects retrieved from the database
     */
    private ModelCollection(final SortedSet<M> models) {
        this.models.addAll(models);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
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
        json.addProperty("manifest", getSerializationManifest());
        return json;
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
    public int compareTo(@Nullable final ModelCollection other) {
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
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getModels());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link ModelCollection} instances.
     */
    public static class Builder<M extends Model> implements ModelBuilder<ModelCollection<M>> {
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
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public Builder<M> fromJson(final ManifestMapping mapping, final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("models")) {
                json.getAsJsonArray("models").forEach(jsonElement -> {
                    final JsonObject obj = jsonElement.getAsJsonObject();
                    final Optional<ModelBuilder<?>> builder =
                            mapping.getBuilder(obj.getAsJsonPrimitive("manifest").getAsString());
                    if (builder.isPresent()) {
                        add((M) builder.get().fromJson(mapping, obj).build());
                    }
                });
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ModelCollection<M> build() {
            return new ModelCollection<>(this.models);
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
