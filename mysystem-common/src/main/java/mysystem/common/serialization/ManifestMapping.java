package mysystem.common.serialization;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.reflections.Reflections;

import mysystem.common.model.Model;
import mysystem.common.model.ModelBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Manage the mapping of model objects to their string-based manifest values during serialization and deserialization.
 */
public class ManifestMapping {
    private final Map<Class<? extends Model>, String> manifestMap = new HashMap<>();
    private final Map<String, Class<? extends ModelBuilder>> builderMap = new HashMap<>();

    /**
     * Default constructor populates this mapping through reflection and introspection of available classes, and is
     * therefore an expensive operation.
     */
    public ManifestMapping() {
        populate();
    }

    protected void populate() {
        final Reflections reflections = new Reflections(getPackagePrefix());
        final Set<Class<? extends Model>> models = reflections.getSubTypesOf(Model.class);
        final Set<Class<? extends ModelBuilder>> builders = reflections.getSubTypesOf(ModelBuilder.class);

        sort(getTriples(getPairs(models, builders))).forEach(this::putTriple);
    }

    protected void putTriple(final Triple<String, Class<? extends Model>, Class<? extends ModelBuilder>> triple) {
        // Mapping from serialization manifest to model class.
        this.manifestMap.put(triple.getMiddle(), triple.getLeft());

        // Mapping from serialization manifest to builder class.
        this.builderMap.put(triple.getLeft(), triple.getRight());
    }

    protected Set<Pair<Class<? extends Model>, Class<? extends ModelBuilder>>> getPairs(
            final Set<Class<? extends Model>> models, final Set<Class<? extends ModelBuilder>> builders) {
        final Map<String, Class<? extends ModelBuilder>> map = new HashMap<>();
        builders.forEach(c -> map.put(StringUtils.substringBeforeLast(c.getName(), "$"), c));

        return models.stream()
                .map(c -> Pair.<Class<? extends Model>, Class<? extends ModelBuilder>>of(c, map.get(c.getName())))
                .filter(p -> p.getRight() != null).collect(Collectors.toSet());
    }

    protected SortedSet<Triple<String, Class<? extends Model>, Class<? extends ModelBuilder>>> sort(
            final Set<Triple<String, Class<? extends Model>, Class<? extends ModelBuilder>>> unsorted) {
        final SortedSet<Triple<String, Class<? extends Model>, Class<? extends ModelBuilder>>> set = new TreeSet<>();
        set.addAll(unsorted);
        return set;
    }

    protected Set<Triple<String, Class<? extends Model>, Class<? extends ModelBuilder>>> getTriples(
            final Set<Pair<Class<? extends Model>, Class<? extends ModelBuilder>>> pairs) {
        return pairs.stream().map(this::asTriple).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toSet());
    }

    protected Optional<Triple<String, Class<? extends Model>, Class<? extends ModelBuilder>>> asTriple(
            final Pair<Class<? extends Model>, Class<? extends ModelBuilder>> pair) {
        final Optional<String> manifest = getSerializationManifest(pair);
        if (manifest.isPresent()) {
            return Optional.of(Triple.of(manifest.get(), pair.getLeft(), pair.getRight()));
        }
        return Optional.empty();
    }

    protected Optional<String> getSerializationManifest(
            final Pair<Class<? extends Model>, Class<? extends ModelBuilder>> pair) {
        try {
            return Optional.of(pair.getRight().newInstance().getSerializationManifest());
        } catch (final InstantiationException | IllegalAccessException e) {
            // Ignored.
            return Optional.empty();
        }
    }

    protected String getPackagePrefix() {
        return StringUtils.substringBefore(getClass().getPackage().getName(), ".");
    }

    /**
     * @param clazz the class to be serialized for which a suitable manifest value is needed
     * @return the matching manifest, possibly empty if the class is not recognized
     */
    public Optional<String> getManifest(final Class<? extends Model> clazz) {
        return Optional.ofNullable(this.manifestMap.get(Objects.requireNonNull(clazz)));
    }

    /**
     * @param manifest the string manifest value of a serialized object for which a builder is needed
     * @return the matching builder used to deserialize the model object, possibly empty if the manifest is not
     * recognized
     */
    @SuppressWarnings("unchecked")
    public Optional<ModelBuilder<? extends Model>> getBuilder(final String manifest) {
        final Optional<Class<? extends ModelBuilder>> builder =
                Optional.ofNullable(this.builderMap.get(Objects.requireNonNull(manifest)));
        if (builder.isPresent()) {
            try {
                return Optional.of(builder.get().newInstance());
            } catch (final InstantiationException | IllegalAccessException createFailed) {
                // Failed to create the model builder instance.
            }
        }
        return Optional.empty();
    }
}
