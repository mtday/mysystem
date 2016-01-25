package mysystem.shell.model;

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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable object used to response to a registration request with information describing the commands made
 * available in the shell.
 */
public class RegistrationResponse implements Model, Comparable<RegistrationResponse> {
    private final static String SERIALIZATION_MANIFEST = RegistrationResponse.class.getSimpleName();

    private final SortedSet<Registration> registrations = new TreeSet<>();
    private final Optional<TokenizedUserInput> userInput;

    /**
     * @param registrations the registrations describing commands available in the shell
     * @param userInput the tokenized user input, possibly empty
     */
    private RegistrationResponse(
            final Collection<Registration> registrations, final Optional<TokenizedUserInput> userInput) {
        this.registrations.addAll(registrations);
        this.userInput = userInput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializationManifest() {
        return SERIALIZATION_MANIFEST;
    }

    /**
     * @return the registrations describing commands available in the shell
     */
    public SortedSet<Registration> getRegistrations() {
        return Collections.unmodifiableSortedSet(this.registrations);
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
    public JsonObject toJson() {
        final JsonArray regArr = new JsonArray();
        getRegistrations().forEach(r -> regArr.add(r.toJson()));

        final JsonObject json = new JsonObject();
        json.add("registrations", regArr);
        if (getUserInput().isPresent()) {
            json.add("userInput", getUserInput().get().toJson());
        }
        json.addProperty("manifest", getSerializationManifest());
        return json;
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
        cmp.append(getRegistrations(), other.getRegistrations(), new CollectionComparator<Registration>());
        cmp.append(getUserInput(), other.getUserInput(), new OptionalComparator<TokenizedUserInput>());
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
    public static class Builder implements ModelBuilder<RegistrationResponse> {
        private final SortedSet<Registration> registrations = new TreeSet<>();
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
         * @param lookup the {@link RegistrationLookup} indicating the desired registrations
         * @param registrationMap the map containing all registrations
         */
        public Builder(final RegistrationLookup lookup, final Map<CommandPath, Registration> registrationMap) {
            if (Objects.requireNonNull(lookup).getUserInput().isPresent()) {
                setUserInput(lookup.getUserInput().get());
            }
            for (final CommandPath path : lookup.getPaths()) {
                boolean foundMatch = false;
                for (final Map.Entry<CommandPath, Registration> entry : registrationMap.entrySet()) {
                    if (entry.getKey().isPrefix(path)) {
                        add(entry.getValue());
                        foundMatch = true;
                    }
                }

                if (!foundMatch) {
                    Optional<CommandPath> commandPath = Optional.of(path);
                    while (commandPath.isPresent()) {
                        final Optional<Registration> match =
                                Optional.ofNullable(registrationMap.get(commandPath.get()));
                        if (match.isPresent()) {
                            add(match.get());

                            // Now that we have found a match, do not process any more parents.
                            commandPath = Optional.empty();
                        } else {
                            commandPath = commandPath.get().getParent();
                        }
                    }
                }
            }
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
         * {@inheritDoc}
         */
        @Override
        public Builder fromJson(final ManifestMapping mapping, final JsonObject json) {
            Objects.requireNonNull(json);
            if (json.has("registrations")) {
                json.getAsJsonArray("registrations")
                        .forEach(e -> add(new Registration.Builder().fromJson(mapping, e.getAsJsonObject()).build()));
            }
            if (json.has("userInput")) {
                setUserInput(
                        new TokenizedUserInput.Builder().fromJson(mapping, json.getAsJsonObject("userInput")).build());
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public RegistrationResponse build() {
            return new RegistrationResponse(this.registrations, this.userInput);
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
