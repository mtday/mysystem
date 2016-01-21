package mysystem.shell.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import mysystem.common.util.CollectionComparator;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

/**
 * An immutable representation of tokenized user input received from the shell interface.
 */
public class TokenizedUserInput implements Comparable<TokenizedUserInput>, Serializable {
    private final static long serialVersionUID = 1L;

    private final UserInput userInput;
    private final List<String> tokens;

    /**
     * @param userInput the original user-provided input
     * @param tokens the tokenized user-provided input from the shell interface
     */
    private TokenizedUserInput(final UserInput userInput, final List<String> tokens) {
        this.userInput = userInput;
        this.tokens = new ArrayList<>(tokens);
    }

    /**
     * @return the original user-provided input
     */
    public UserInput getUserInput() {
        return this.userInput;
    }

    /**
     * @return the tokens parsed from the user-provided input from the shell interface
     */
    public List<String> getTokens() {
        return Collections.unmodifiableList(this.tokens);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.join(" ", getTokens());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final TokenizedUserInput other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getUserInput(), other.getUserInput());
        cmp.append(getTokens(), other.getTokens(), new CollectionComparator<String>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof TokenizedUserInput) && compareTo((TokenizedUserInput) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getUserInput());
        hash.append(getTokens());
        return hash.toHashCode();
    }

    /**
     * Used to create {@link TokenizedUserInput} instances.
     */
    public static class Builder {
        private final UserInput userInput;
        private final List<String> tokens;

        /**
         * Copy constructor.
         *
         * @param other the {@link TokenizedUserInput} to duplicate
         */
        public Builder(final TokenizedUserInput other) {
            this.userInput = other.getUserInput();
            this.tokens = new ArrayList<>(Objects.requireNonNull(other).getTokens());
        }

        /**
         * @param input the unprocessed user input provided by the user from the shell interface
         * @throws ParseException if there is a problem with the input text
         */
        public Builder(final String input) throws ParseException {
            this(new UserInput.Builder(Objects.requireNonNull(input)).build());
        }

        /**
         * @param userInput the unprocessed user input provided by the user from the shell interface
         * @throws ParseException if there is a problem with the input text
         */
        public Builder(final UserInput userInput) throws ParseException {
            this.userInput = Objects.requireNonNull(userInput);
            this.tokens = tokenize(userInput.getInput());
        }

        private List<String> tokenize(final String input) throws ParseException {
            final List<String> tokens = new ArrayList<>();
            boolean inQuote = false;
            boolean inEscapeSequence = false;
            String hexChars = null;
            char inQuoteChar = '"';

            final byte[] token = new byte[input.length()];
            final byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
            int tokenLength = 0;
            for (int i = 0; i < input.length(); ++i) {
                final char ch = input.charAt(i);

                // if I ended up in an escape sequence, check for valid escapable character, and add it as a literal
                if (inEscapeSequence) {
                    inEscapeSequence = false;
                    if (ch == 'x') {
                        hexChars = "";
                    } else if (Character.isWhitespace(ch) || ch == '\'' || ch == '"' || ch == '\\') {
                        token[tokenLength++] = inputBytes[i];
                    } else {
                        throw new ParseException("Illegal escape sequence", i);
                    }
                } else if (hexChars != null) {
                    // in a hex escape sequence
                    final int digit = Character.digit(ch, 16);
                    if (digit < 0) {
                        throw new ParseException("Expected hex character", i);
                    }
                    hexChars += ch;
                    if (hexChars.length() == 2) {
                        token[tokenLength++] = (byte) (0xff & Short.parseShort(hexChars, 16));
                        hexChars = null;
                    }
                } else if (inQuote) {
                    // in a quote, either end the quote, start escape, or continue a token
                    if (ch == inQuoteChar) {
                        inQuote = false;
                        tokens.add(new String(token, 0, tokenLength, StandardCharsets.ISO_8859_1));
                        tokenLength = 0;
                    } else if (ch == '\\') {
                        inEscapeSequence = true;
                    } else {
                        token[tokenLength++] = inputBytes[i];
                    }
                } else {
                    // not in a quote, either enter a quote, end a token, start escape, or continue a token
                    if (ch == '\'' || ch == '"') {
                        if (tokenLength > 0) {
                            tokens.add(new String(token, 0, tokenLength, StandardCharsets.ISO_8859_1));
                            tokenLength = 0;
                        }
                        inQuote = true;
                        inQuoteChar = ch;
                    } else if (Character.isWhitespace(ch) && tokenLength > 0) {
                        tokens.add(new String(token, 0, tokenLength, StandardCharsets.ISO_8859_1));
                        tokenLength = 0;
                    } else if (ch == '\\') {
                        inEscapeSequence = true;
                    } else if (!Character.isWhitespace(ch)) {
                        token[tokenLength++] = inputBytes[i];
                    }
                }
            }
            if (inQuote) {
                throw new ParseException("Missing terminating quote", input.length());
            } else if (inEscapeSequence || hexChars != null) {
                throw new ParseException("Escape sequence not complete", input.length());
            }
            if (tokenLength > 0) {
                tokens.add(new String(token, 0, tokenLength, StandardCharsets.ISO_8859_1));
            }
            return tokens;
        }

        /**
         * @return the {@link TokenizedUserInput} defined in this builder
         */
        public TokenizedUserInput build() {
            return new TokenizedUserInput(this.userInput, this.tokens);
        }
    }
}
