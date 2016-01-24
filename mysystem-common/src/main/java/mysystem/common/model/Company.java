package mysystem.common.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.util.OptionalComparator;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable representation of a company.
 */
public class Company implements Model, HasOptionalId, HasActive, Comparable<Company> {
    private final Optional<Integer> id;
    private final String name;
    private final boolean active;

    /**
     * @param id the unique identifier of the company, possibly empty
     * @param name the unique name of the company
     * @param active whether the company is active
     */
    private Company(final Optional<Integer> id, final String name, final boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Integer> getId() {
        return this.id;
    }

    /**
     * @return the unique name of the company
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return whether the company is active
     */
    @Override
    public boolean isActive() {
        return this.active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        if (getId().isPresent()) {
            json.addProperty("id", getId().get());
        }
        json.addProperty("name", getName());
        json.addProperty("active", isActive());
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("id", getId());
        str.append("name", getName());
        str.append("active", isActive());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Company other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getId(), other.getId(), new OptionalComparator());
        cmp.append(getName(), other.getName());
        cmp.append(isActive(), other.isActive());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof Company) && compareTo((Company) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getId());
        hash.append(getName());
        hash.append(isActive());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link Company} instances.
     */
    public static class Builder implements ModelBuilder<Company> {
        private Optional<Integer> id = Optional.empty();
        private Optional<String> name = Optional.empty();
        private boolean active = true;

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param other the company to duplicate
         */
        public Builder(final Company other) {
            setId(Objects.requireNonNull(other).getId());
            setName(other.getName());
            setActive(other.isActive());
        }

        /**
         * @param id the new unique identifier of this company, possibly empty
         * @return {@code this} for fluent-style usage
         */
        public Builder setId(final Optional<Integer> id) {
            this.id = id;
            return this;
        }

        /**
         * @param id the new unique identifier of this company
         * @return {@code this} for fluent-style usage
         */
        public Builder setId(final int id) {
            return setId(Optional.of(id));
        }

        /**
         * @param name the unique name of the company
         * @return {@code this} for fluent-style usage
         */
        public Builder setName(final String name) {
            Objects.requireNonNull(name);
            Preconditions.checkArgument(!StringUtils.isBlank(name), "Company name cannot be blank");
            this.name = Optional.of(name);
            return this;
        }

        /**
         * @param active whether the company is active
         * @return {@code this} for fluent-style usage
         */
        public Builder setActive(final boolean active) {
            this.active = active;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final JsonObject json) {
            if (json.has("id")) {
                setId(json.getAsJsonPrimitive("id").getAsInt());
            }
            if (json.has("name")) {
                setName(json.getAsJsonPrimitive("name").getAsString());
            }
            if (json.has("active")) {
                setActive(json.getAsJsonPrimitive("active").getAsBoolean());
            }

            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Company build() {
            if (!this.name.isPresent()) {
                throw new IllegalStateException("A name is required for company objects");
            }

            return new Company(this.id, this.name.get(), this.active);
        }
    }
}
