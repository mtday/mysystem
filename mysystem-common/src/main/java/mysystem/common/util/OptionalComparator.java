package mysystem.common.util;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;
import java.util.Optional;

/**
 * Perform comparisons between two {@link Optional} objects.
 */
public class OptionalComparator implements Comparator<Optional<?>> {
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final Optional<?> a, final Optional<?> b) {
        // Parameters expected to not be null. That is why optionals are used, after all.
        if (a.isPresent() && b.isPresent()) {
            final CompareToBuilder cmp = new CompareToBuilder();
            cmp.append(a.get(), b.get());
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
