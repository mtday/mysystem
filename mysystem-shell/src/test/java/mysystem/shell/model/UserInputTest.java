package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link UserInput} class and builder.
 */
public class UserInputTest {
    @Test
    public void testCompareTo() {
        final UserInput a = new UserInput.Builder("input").build();
        final UserInput b = new UserInput.Builder("more input").build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-4, a.compareTo(b));
        assertEquals(4, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final UserInput a = new UserInput.Builder("input").build();
        final UserInput b = new UserInput.Builder("more input").build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertTrue(b.equals(b));
    }

    @Test
    public void testHashCode() {
        final UserInput a = new UserInput.Builder("input").build();
        final UserInput b = new UserInput.Builder("more input").build();

        assertEquals(100358090, a.hashCode());
        assertEquals(-1432153281, b.hashCode());
    }

    @Test
    public void testToString() {
        final UserInput a = new UserInput.Builder("input").build();
        final UserInput b = new UserInput.Builder("more input").build();

        assertEquals("input", a.toString());
        assertEquals("more input", b.toString());
    }

    @Test
    public void testBuilderWithWhitespace() {
        final UserInput a = new UserInput.Builder("  input  ").build();
        final UserInput b = new UserInput.Builder("  \t").build();

        assertEquals("input", a.toString());
        assertEquals("", b.toString());
    }

    @Test
    public void testBuilderCopy() {
        final UserInput a = new UserInput.Builder("input").build();
        final UserInput b = new UserInput.Builder(a).build();

        assertEquals(a, b);
    }

    @Test
    public void testIsEmpty() {
        final UserInput a = new UserInput.Builder("  input  ").build();
        final UserInput b = new UserInput.Builder("  \t").build();

        assertFalse(a.isEmpty());
        assertTrue(b.isEmpty());
    }

    @Test
    public void testIsComment() {
        final UserInput a = new UserInput.Builder("  input  ").build();
        final UserInput b = new UserInput.Builder("  \t").build();
        final UserInput c = new UserInput.Builder("  # comment").build();

        assertFalse(a.isComment());
        assertFalse(b.isComment());
        assertTrue(c.isComment());
    }
}
