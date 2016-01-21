package mysystem.db.model.company;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.*;

/**
 * An immutable class that represents the information needed to fetch company objects from the database.
 */
public class CompanyGet extends CompanyModel implements Comparable<CompanyGet>, Serializable {
    private final static long serialVersionUID = 1L;

    private final SortedSet<Integer> ids;

    /**
     * @param ids the unique identifiers of the company objects to fetch
     */
    private CompanyGet(final SortedSet<Integer> ids) {
        this.ids = new TreeSet<>(ids);
    }

    /**
     * @return an unmodifiable set containing the unique identifiers of the company objects to fetch
     */
    public SortedSet<Integer> getIds() {
        return Collections.unmodifiableSortedSet(this.ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("ids", getIds());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final CompanyGet other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        final Iterator<Integer> pathA = getIds().iterator();
        final Iterator<Integer> pathB = other.getIds().iterator();

        while (pathA.hasNext() && pathB.hasNext() && cmp.toComparison() == 0) {
            cmp.append(pathA.next(), pathB.next());
        }

        if (cmp.toComparison() == 0) {
            if (pathA.hasNext()) {
                return 1;
            } else if (pathB.hasNext()) {
                return -1;
            }
        }

        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof CompanyGet) && compareTo((CompanyGet) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getIds().hashCode();
    }

    /**
     * Used to create {@link CompanyGet} instances.
     */
    public static class Builder {
        private final SortedSet<Integer> ids = new TreeSet<>();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param ids the unique identifiers of the company objects to fetch
         */
        public Builder(final Integer... ids) {
            this(Arrays.asList(Objects.requireNonNull(ids)));
        }

        /**
         * @param ids the unique identifiers of the company objects to fetch
         */
        public Builder(final Collection<Integer> ids) {
            this.ids.addAll(Objects.requireNonNull(ids));
        }

        /**
         * @param ids the unique identifiers of the company objects to fetch
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Integer... ids) {
            return add(Arrays.asList(Objects.requireNonNull(ids)));
        }

        /**
         * @param ids the unique identifiers of the company objects to fetch
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Collection<Integer> ids) {
            this.ids.addAll(Objects.requireNonNull(ids));
            return this;
        }

        /**
         * @return the {@link CompanyGet} represented by this builder
         */
        public CompanyGet build() {
            if (this.ids.isEmpty()) {
                throw new IllegalStateException("Cannot create CompanyGet without ids");
            }

            return new CompanyGet(this.ids);
        }
    }
}
