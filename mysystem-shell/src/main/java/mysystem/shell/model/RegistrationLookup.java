package mysystem.shell.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import mysystem.common.util.CollectionComparator;
import mysystem.common.util.OptionalComparator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * An immutable object used to lookup registration information for the command paths.
 */
public class RegistrationLookup implements Comparable<RegistrationLookup>, Serializable {
    private final static long serialVersionUID = 1L;

    private final Set<CommandPath> paths = new TreeSet<>();
    private final Optional<TokenizedUserInput> userInput;

    /**
     * @param paths the {@link CommandPath} values to lookup
     * @param userInput the tokenized user input, possibly empty
     */
    private RegistrationLookup(final Collection<CommandPath> paths, final Optional<TokenizedUserInput> userInput) {
        this.paths.addAll(paths);
        this.userInput = userInput;
    }

    /**
     * @return the unmodifiable {@link CommandPath} objects for which command registrations should be found
     */
    public Set<CommandPath> getPaths() {
        return this.paths;
    }

    /**
     * @return the tokenized user input from the shell used to determine which registrations are in this response
     */
    public Optional<TokenizedUserInput> getUserInput() {
        return this.userInput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("paths", getPaths());
        str.append("userInput", getUserInput());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final RegistrationLookup other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getPaths(), other.getPaths(), new CollectionComparator<CommandPath>());
        cmp.append(getUserInput(), other.getUserInput(), new OptionalComparator<UserInput>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof RegistrationLookup) && compareTo((RegistrationLookup) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash  = new HashCodeBuilder();
        hash.append(getPaths());
        hash.append(getUserInput());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link RegistrationLookup} objects.
     */
    public static class Builder {
        private final Set<CommandPath> paths = new TreeSet<>();
        private Optional<TokenizedUserInput> userInput = Optional.empty();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param paths the command path to include in the lookup request
         */
        public Builder(final CommandPath... paths) {
            add(Arrays.asList(Objects.requireNonNull(paths)));
        }

        /**
         * @param userInput the tokenized user input to include in the lookup request
         */
        public Builder(final TokenizedUserInput userInput) {
            add(new CommandPath.Builder(Objects.requireNonNull(userInput)).build());
            this.userInput = Optional.of(userInput);
        }

        /**
         * @param paths the command paths to include in the lookup request
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Collection<CommandPath> paths) {
            this.paths.addAll(Objects.requireNonNull(paths));
            return this;
        }

        /**
         * @param paths the command paths to include in the lookup request
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final CommandPath... paths) {
            return add(Arrays.asList(Objects.requireNonNull(paths)));
        }

        /**
         * @return a new {@link RegistrationLookup} instance based on this builder
         */
        public RegistrationLookup build() {
            if (this.paths.isEmpty()) {
                throw new IllegalStateException("Unable to create registration lookup without paths");
            }

            return new RegistrationLookup(this.paths, this.userInput);
        }
    }
}
