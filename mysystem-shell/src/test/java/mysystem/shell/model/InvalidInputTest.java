package mysystem.shell.model;

import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * Perform testing of the {@link InvalidInput} class and builder.
 */
public class InvalidInputTest {
    @Test
    public void testCompareTo() {
        final ParseException ex = new ParseException("Message", 10);
        final InvalidInput a = new InvalidInput.Builder(new UserInput.Builder("a").build(), ex).build();
        final InvalidInput b = new InvalidInput.Builder(new UserInput.Builder("b").build(), ex).build();
        final InvalidInput c = new InvalidInput.Builder(new UserInput.Builder("c").build(), ex).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-2, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(2, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final ParseException ex = new ParseException("Message", 10);
        final InvalidInput a = new InvalidInput.Builder(new UserInput.Builder("a").build(), ex).build();
        final InvalidInput b = new InvalidInput.Builder(new UserInput.Builder("b").build(), ex).build();
        final InvalidInput c = new InvalidInput.Builder(new UserInput.Builder("c").build(), ex).build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(b.equals(a));
        assertTrue(b.equals(b));
        assertFalse(b.equals(c));
        assertFalse(c.equals(a));
        assertFalse(c.equals(b));
        assertTrue(c.equals(c));
    }

    @Test
    public void testHashCode() {
        final ParseException ex = new ParseException("Message", 10);
        final InvalidInput a = new InvalidInput.Builder(new UserInput.Builder("a").build(), ex).build();
        final InvalidInput b = new InvalidInput.Builder(new UserInput.Builder("b").build(), ex).build();
        final InvalidInput c = new InvalidInput.Builder(new UserInput.Builder("c").build(), ex).build();

        assertEquals(-1858855213, a.hashCode());
        assertEquals(-1858853844, b.hashCode());
        assertEquals(-1858852475, c.hashCode());
    }

    @Test
    public void testToString() {
        final ParseException ex = new ParseException("Message", 10);
        final InvalidInput a = new InvalidInput.Builder(new UserInput.Builder("a").build(), ex).build();
        assertEquals("InvalidInput[userInput=a,error=Message,location=10]", a.toString());
    }

    @Test
    public void testBuilderCopy() {
        final ParseException ex = new ParseException("Message", 10);
        final InvalidInput a = new InvalidInput.Builder(new UserInput.Builder("a").build(), ex).build();
        final InvalidInput b = new InvalidInput.Builder(a).build();

        assertEquals(a, b);
    }
}
