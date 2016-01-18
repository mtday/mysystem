package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link UnrecognizedCommand} class and builder.
 */
public class UnrecognizedCommandTest {
    @Test
    public void testCompareTo() {
        final UnrecognizedCommand a = new UnrecognizedCommand.Builder(new UserInput.Builder("a").build()).build();
        final UnrecognizedCommand b = new UnrecognizedCommand.Builder(new UserInput.Builder("b").build()).build();
        final UnrecognizedCommand c = new UnrecognizedCommand.Builder(new UserInput.Builder("c").build()).build();

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
        final UnrecognizedCommand a = new UnrecognizedCommand.Builder(new UserInput.Builder("a").build()).build();
        final UnrecognizedCommand b = new UnrecognizedCommand.Builder(new UserInput.Builder("b").build()).build();
        final UnrecognizedCommand c = new UnrecognizedCommand.Builder(new UserInput.Builder("c").build()).build();

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
        final UnrecognizedCommand a = new UnrecognizedCommand.Builder(new UserInput.Builder("a").build()).build();
        final UnrecognizedCommand b = new UnrecognizedCommand.Builder(new UserInput.Builder("b").build()).build();
        final UnrecognizedCommand c = new UnrecognizedCommand.Builder(new UserInput.Builder("c").build()).build();

        assertEquals(97, a.hashCode());
        assertEquals(98, b.hashCode());
        assertEquals(99, c.hashCode());
    }

    @Test
    public void testToString() {
        final UnrecognizedCommand a = new UnrecognizedCommand.Builder(new UserInput.Builder("a").build()).build();
        assertEquals("UnrecognizedCommand[userInput=a]", a.toString());
    }
}
