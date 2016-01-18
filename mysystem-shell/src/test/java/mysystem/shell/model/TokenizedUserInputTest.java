package mysystem.shell.model;

import com.typesafe.config.ConfigException;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * Perform testing of the {@link TokenizedUserInput} class and builder.
 */
public class TokenizedUserInputTest {
    @Test
    public void testCompareTo() throws ParseException {
        final TokenizedUserInput a = new TokenizedUserInput.Builder("input").build();
        final TokenizedUserInput b = new TokenizedUserInput.Builder("more input").build();
        final TokenizedUserInput c = new TokenizedUserInput.Builder("more input again").build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-4, a.compareTo(b));
        assertEquals(-4, a.compareTo(c));
        assertEquals(4, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(4, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() throws ParseException {
        final TokenizedUserInput a = new TokenizedUserInput.Builder("input").build();
        final TokenizedUserInput b = new TokenizedUserInput.Builder("more input").build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertTrue(b.equals(b));
    }

    @Test
    public void testHashCode() throws ParseException {
        final TokenizedUserInput a = new TokenizedUserInput.Builder("input").build();
        final TokenizedUserInput b = new TokenizedUserInput.Builder("more input").build();

        assertEquals(100358121, a.hashCode());
        assertEquals(204442326, b.hashCode());
    }

    @Test
    public void testToString() throws ParseException {
        final TokenizedUserInput a = new TokenizedUserInput.Builder("input").build();
        final TokenizedUserInput b = new TokenizedUserInput.Builder("more input").build();

        assertEquals("input", a.toString());
        assertEquals("more input", b.toString());
    }

    @Test
    public void testBuilderCopy() throws ParseException {
        final TokenizedUserInput a = new TokenizedUserInput.Builder("input").build();
        final TokenizedUserInput b = new TokenizedUserInput.Builder(a).build();

        assertEquals(a, b);
    }

    @Test
    public void testBuilderWithUserInput() throws ParseException {
        final UserInput userInput = new UserInput.Builder("input").build();
        final TokenizedUserInput tokenized = new TokenizedUserInput.Builder(userInput).build();

        assertEquals("[input]", tokenized.getTokens().toString());
    }

    @Test
    public void testTokenizeWithMultipleSpaces() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("a  b").build();

        assertEquals("[a, b]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithTab() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("a\tb").build();

        assertEquals("[a, b]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithEscapeDoubleQuote() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input \\\"").build();

        assertEquals("[input, \"]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithEscapeSingleQuote() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input \\'").build();

        assertEquals("[input, ']", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithEscapeBackslash() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input \\\\").build();

        assertEquals("[input, \\]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithEscapeSpace() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input a\\ b").build();

        assertEquals("[input, a b]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithEscapedHexChar() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input \\x25").build();

        assertEquals("[input, %]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithDoubleQuotedString() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input \"quoted string\"").build();

        assertEquals("[input, quoted string]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithSingleQuotedString() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input 'quoted string'").build();

        assertEquals("[input, quoted string]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithEmptyDoubleQuotedString() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input \"\"").build();

        assertEquals("[input, ]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithEmptySingleQuotedString() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input ''").build();

        assertEquals("[input, ]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithDoubleQuotedStringRightNextToInput() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input\"a\"").build();

        assertEquals("[input, a]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithEmptySingleQuotedStringRightNextToInput() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input'a'").build();

        assertEquals("[input, a]", t.getTokens().toString());
    }

    @Test
    public void testTokenizeWithEscapeInsideDoubleQuotedString() throws ParseException {
        final TokenizedUserInput t = new TokenizedUserInput.Builder("input \"\\\"\"").build();

        assertEquals("[input, \"]", t.getTokens().toString());
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithIllegalEscapeSequence() throws ParseException {
        new TokenizedUserInput.Builder("input \\-").build();
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithIncompleteEscapeSequence() throws ParseException {
        new TokenizedUserInput.Builder("input \\").build();
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithIncompleteEscapedHex() throws ParseException {
        new TokenizedUserInput.Builder("input \\x0").build();
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithIllegalEscapedHex() throws ParseException {
        new TokenizedUserInput.Builder("input \\xtv").build();
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithUnmatchedDoubleQuote() throws ParseException {
        new TokenizedUserInput.Builder("input \"quoted string").build();
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithUnmatchedSingleQuote() throws ParseException {
        new TokenizedUserInput.Builder("input 'quoted string").build();
    }
}