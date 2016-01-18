package mysystem.shell.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 */
public class CommandTokenizer implements Iterable<String> {
    private final static Charset CHARSET = StandardCharsets.ISO_8859_1;
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private ArrayList<String> tokens;
    private String input;

    public CommandTokenizer(final String t) throws BadArgumentException {
        tokens = new ArrayList<String>();
        this.input = t;
        try {
            createTokens();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public String[] getTokens() {
        return tokens.toArray(new String[tokens.size()]);
    }

    private void createTokens() throws BadArgumentException, UnsupportedEncodingException {
        boolean inQuote = false;
        boolean inEscapeSequence = false;
        String hexChars = null;
        char inQuoteChar = '"';

        final byte[] token = new byte[input.length()];
        int tokenLength = 0;
        final byte[] inputBytes = input.getBytes(UTF_8);
        for (int i = 0; i < input.length(); ++i) {
            final char ch = input.charAt(i);

            // if I ended up in an escape sequence, check for valid escapable character, and add it as a literal
            if (inEscapeSequence) {
                inEscapeSequence = false;
                if (ch == 'x') {
                    hexChars = "";
                } else if (ch == ' ' || ch == '\'' || ch == '"' || ch == '\\') {
                    token[tokenLength++] = inputBytes[i];
                } else {
                    throw new BadArgumentException(
                            "can only escape single quotes, double quotes, the space character, the backslash, and "
                                    + "hex input",
                            input, i);
                }
            } else if (hexChars != null) {
                // in a hex escape sequence
                final int digit = Character.digit(ch, 16);
                if (digit < 0) {
                    throw new BadArgumentException("expected hex character", input, i);
                }
                hexChars += ch;
                if (hexChars.length() == 2) {
                    byte b;
                    try {
                        b = (byte) (0xff & Short.parseShort(hexChars, 16));
                        if (!Character.isValidCodePoint(0xff & b)) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        throw new BadArgumentException("unsupported non-ascii character", input, i);
                    }
                    token[tokenLength++] = b;
                    hexChars = null;
                }
            } else if (inQuote) {
                // in a quote, either end the quote, start escape, or continue a token
                if (ch == inQuoteChar) {
                    inQuote = false;
                    tokens.add(new String(token, 0, tokenLength, CHARSET));
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
                        tokens.add(new String(token, 0, tokenLength, CHARSET));
                        tokenLength = 0;
                    }
                    inQuote = true;
                    inQuoteChar = ch;
                } else if (ch == ' ' && tokenLength > 0) {
                    tokens.add(new String(token, 0, tokenLength, CHARSET));
                    tokenLength = 0;
                } else if (ch == '\\') {
                    inEscapeSequence = true;
                } else if (ch != ' ') {
                    token[tokenLength++] = inputBytes[i];
                }
            }
        }
        if (inQuote) {
            throw new BadArgumentException("missing terminating quote", input, input.length());
        } else if (inEscapeSequence || hexChars != null) {
            throw new BadArgumentException("escape sequence not complete", input, input.length());
        }
        if (tokenLength > 0) {
            tokens.add(new String(token, 0, tokenLength, CHARSET));
        }
    }

    @Override
    public Iterator<String> iterator() {
        return tokens.iterator();
    }
}
