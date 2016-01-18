package mysystem.shell.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * An immutable representation of the fully qualified path to a shell command.
 */
public class CommandPath implements Comparable<CommandPath>, Serializable {
    private final static long serialVersionUID = 1L;
    private final List<String> path;

    /**
     * @param path the fully qualified path representing a shell command
     */
    private CommandPath(final List<String> path) {
        this.path = path;
    }

    /**
     * @return the individual path components
     */
    public List<String> getPath() {
        return Collections.unmodifiableList(this.path);
    }

    /**
     * Check to see if the provided path is a prefix for this path. Since command paths can have multiple parts, this
     * check is used to determine whether the provided path matches the beginning part of this path. Examples:
     *
     * {@code
     *     "a b c".isPrefix("a") => true
     *     "a b c".isPrefix("a b c") => true
     *     "a b c".isPrefix("a b c d") => false
     *     "one two three".isPrefix("one tw") => true
     *     "one two three".isPrefix("one three") => false
     * }
     *
     * @param other the path to check to see if it is a prefix for this path
     * @return whether the provided path is a prefix of this path
     */
    public boolean isPrefix(final CommandPath other) {
        Objects.requireNonNull(other);

        final Iterator<String> iterA = getPath().iterator();
        final Iterator<String> iterB = other.getPath().iterator();

        while (iterA.hasNext() && iterB.hasNext()) {
            final String a = iterA.next();
            final String b = iterB.next();
            if (!a.equals(b) && !a.startsWith(b)) {
                // Found a mis-match path element
                return false;
            }
        }

        if (iterB.hasNext()) {
            // The other path is longer than this path.
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.join(" ", getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final CommandPath other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        final Iterator<String> pathA = getPath().iterator();
        final Iterator<String> pathB = other.getPath().iterator();

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
        return (other instanceof CommandPath) && compareTo((CommandPath) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    /**
     * Used to create {@link CommandPath} instances.
     */
    public static class Builder {
        private List<String> path = new ArrayList<>();

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param userInput the user input from which a {@link CommandPath} will be generated
         */
        public Builder(final UserInput userInput) {
            final String[] parts = userInput.getInput().split("\\s");
            for (final String part : parts) {
                final String trimmed = StringUtils.trimToEmpty(part);
                if (StringUtils.isEmpty(trimmed)) {
                    continue;
                }
                if (StringUtils.startsWith(trimmed, "-")) {
                    // Found the beginning of the options, no more command path parts.
                    break;
                }

                add(part);
            }
        }

        /**
         * Copy constructor.
         *
         * @param other the {@link CommandPath} to duplicate
         */
        public Builder(final CommandPath other) {
            this.path.addAll(other.getPath());
        }

        /**
         * @param path the path components to include in the {@link CommandPath}, possibly empty
         */
        public Builder(final Collection<String> path) {
            this.path.addAll(Objects.requireNonNull(path));
        }

        /**
         * @param path the path components to include in the {@link CommandPath}
         */
        public Builder(final String... path) {
            add(path);
        }

        /**
         * @param path the new path components to add to the {@link CommandPath}, possibly empty
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final Collection<String> path) {
            this.path.addAll(Objects.requireNonNull(path));
            return this;
        }

        /**
         * @param path the new path components to add to the {@link CommandPath}
         * @return {@code this} for fluent-style usage
         */
        public Builder add(final String... path) {
            return add(Arrays.asList(Objects.requireNonNull(path)));
        }

        /**
         * @return the {@link CommandPath} defined in this builder
         */
        public CommandPath build() {
            if (this.path.isEmpty()) {
                throw new IllegalStateException("Unable to create empty command path");
            }

            return new CommandPath(this.path);
        }
    }
}
