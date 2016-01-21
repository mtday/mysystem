package mysystem.db.model.company;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.model.Company;
import mysystem.common.util.CollectionComparator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable class that represents the company objects retrieved from the database.
 */
public class CompanyResponse implements Comparable<CompanyResponse>, Serializable {
    private final static long serialVersionUID = 1L;

    private final SortedSet<Company> companies;

    /**
     * @param companies the companies retrieved from the database
     */
    private CompanyResponse(final SortedSet<Company> companies) {
        this.companies = new TreeSet<>(companies);
    }

    /**
     * @return an unmodifiable set containing the company objects retrieved from the database
     */
    public SortedSet<Company> getCompanies() {
        return Collections.unmodifiableSortedSet(this.companies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("companies", getCompanies());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final CompanyResponse other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getCompanies(), other.getCompanies(), new CollectionComparator<Company>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof CompanyResponse) && compareTo((CompanyResponse) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getCompanies().hashCode();
    }

    /**
     * Used to create {@link CompanyResponse} instances.
     */
    public static class Builder {
        private final SortedSet<Company> companies = new TreeSet<>();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param companies the company objects retrieved from the database
         */
        public Builder(final Company... companies) {
            this(Arrays.asList(Objects.requireNonNull(companies)));
        }

        /**
         * @param companies the company objects retrieved from the database
         */
        public Builder(final Collection<Company> companies) {
            this.companies.addAll(Objects.requireNonNull(companies));
        }

        /**
         * @param companies the company objects retrieved from the database
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Company... companies) {
            return add(Arrays.asList(Objects.requireNonNull(companies)));
        }

        /**
         * @param companies the company objects retrieved from the database
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Collection<Company> companies) {
            this.companies.addAll(Objects.requireNonNull(companies));
            return this;
        }

        /**
         * @return the {@link CompanyResponse} represented by this builder
         */
        public CompanyResponse build() {
            return new CompanyResponse(this.companies);
        }
    }
}
