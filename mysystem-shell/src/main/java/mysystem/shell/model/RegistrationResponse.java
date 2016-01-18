package mysystem.shell.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * An immutable object used to response to a registration request with information describing the commands made
 * available in the shell.
 */
public class RegistrationResponse implements Comparable<RegistrationResponse>, Serializable {
    private final static long serialVersionUID = 1L;

    private final Set<Registration> registrations = new TreeSet<>();
    private final Optional<TokenizedUserInput> userInput;

    /**
     * @param registrations the registrations describing commands available in the shell
     * @param userInput the tokenized user input
     */
    private RegistrationResponse(final Collection<Registration> registrations, final Optional<TokenizedUserInput> userInput) {
        this.registrations.addAll(registrations);
        this.userInput = userInput;
    }

    /**
     * @return the registrations describing commands available in the shell
     */
    public Set<Registration> getRegistrations() {
        return Collections.unmodifiableSet(this.registrations);
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
        str.append("registrations", getRegistrations());
        str.append("userInput", getUserInput());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final RegistrationResponse other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();

        final Iterator<Registration> regA = getRegistrations().iterator();
        final Iterator<Registration> regB = other.getRegistrations().iterator();

        while (cmp.toComparison() == 0 && regA.hasNext() && regB.hasNext()) {
            cmp.append(regA.next(), regB.next());
        }

        if (cmp.toComparison() == 0) {
            if (regA.hasNext()) {
                return 1;
            } else if (regB.hasNext()) {
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
        return (other instanceof RegistrationResponse) && compareTo((RegistrationResponse) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getRegistrations());
        hash.append(getUserInput());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link RegistrationResponse} objects.
     */
    public static class Builder {
        private final Set<Registration> registrations = new TreeSet<>();
        private Optional<TokenizedUserInput> userInput = Optional.empty();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param other the {@link RegistrationResponse} to duplicate
         */
        public Builder(final RegistrationResponse other) {
            add(Objects.requireNonNull(other).getRegistrations());
        }

        /**
         * @param lookup        the {@link RegistrationLookup} indicating the desired registrations
         * @param registrations the map containing all registrations
         */
        public Builder(final RegistrationLookup lookup, final Map<CommandPath, Registration> registrations) {
            setUserInput(lookup.getUserInput());
            final CommandPath path = new CommandPath.Builder(lookup.getUserInput()).build();
            registrations.keySet().stream().filter(cp -> cp.isPrefix(path)).forEach(cp -> add(registrations.get(cp)));
        }

        /**
         * @param registrations the registrations to add to the response being built
         */
        public Builder(final Collection<Registration> registrations) {
            add(Objects.requireNonNull(registrations));
        }

        /**
         * @param registrations the registrations to add to the response being built
         */
        public Builder(final Registration... registrations) {
            add(Objects.requireNonNull(registrations));
        }

        /**
         * @param registrations the registrations to add to the response being built
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Collection<Registration> registrations) {
            this.registrations.addAll(Objects.requireNonNull(registrations));
            return this;
        }

        /**
         * @param registrations the registrations to add to the response being built
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Registration... registrations) {
            return add(Arrays.asList(Objects.requireNonNull(registrations)));
        }

        /**
         * @param userInput the user input from the shell used to determine which registrations are in this response
         * @return {@code this} for fluent-style usage
         */
        public Builder setUserInput(final TokenizedUserInput userInput) {
            this.userInput = Optional.of(Objects.requireNonNull(userInput));
            return this;
        }

        /**
         * @return a new {@link RegistrationResponse} instance based on this builder
         */
        public RegistrationResponse build() {
            return new RegistrationResponse(this.registrations, this.userInput);
        }
    }
}
