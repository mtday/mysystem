package mysystem.common.util;

import com.google.common.base.Optional;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * Perform comparisons between two {@link Optional} objects.
 */
public class OptionalComparator<T> implements Comparator<Optional<T>>, Serializable {
    private final static long serialVersionUID = 1L;

    private final Optional<Comparator<T>> comparator;

    /**
     * Default constructor, uses natural ordering of the elements in the collections.
     */
    public OptionalComparator() {
        this.comparator = Optional.absent();
    }

    /**
     * @param comparator the {@link Comparator} used to perform comparisons on the objects within the optionals
     */
    public OptionalComparator(final Comparator<T> comparator) {
        this.comparator = Optional.of(Objects.requireNonNull(comparator));
    }

    /**
     * @return the {@link Comparator} used to perform comparisons on the objects within the optionals in which case
     * the natural ordering will be used
     */
    public Optional<Comparator<T>> getComparator() {
        return this.comparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final Optional<T> a, final Optional<T> b) {
        // Parameters expected to not be null. That is why optionals are used, after all.
        if (a.isPresent() && b.isPresent()) {
            final CompareToBuilder cmp = new CompareToBuilder();
            if (getComparator().isPresent()) {
                cmp.append(a.get(), b.get(), getComparator().get());
            } else {
                cmp.append(a.get(), b.get());
            }
            return cmp.toComparison();
        } else if (!a.isPresent() && !b.isPresent()) {
            return 0;
        } else if (a.isPresent()) {
            return 1;
        } else {
            return -1;
        }
    }
}
