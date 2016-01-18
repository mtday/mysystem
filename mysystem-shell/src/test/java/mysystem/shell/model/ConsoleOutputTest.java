package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link ConsoleOutput} class and builder.
 */
public class ConsoleOutputTest {
    @Test
    public void testCompareTo() {
        final ConsoleOutput a = new ConsoleOutput.Builder("output").build();
        final ConsoleOutput b = new ConsoleOutput.Builder("more output").build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(2, a.compareTo(b));
        assertEquals(-2, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final ConsoleOutput a = new ConsoleOutput.Builder().build();
        final ConsoleOutput b = new ConsoleOutput.Builder("more output").build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertTrue(b.equals(b));
    }

    @Test
    public void testHashCode() {
        final ConsoleOutput a = new ConsoleOutput.Builder("output").build();
        final ConsoleOutput b = new ConsoleOutput.Builder("more output").build();

        assertEquals(2138823212, a.hashCode());
        assertEquals(-1716454625, b.hashCode());
    }

    @Test
    public void testToString() {
        final ConsoleOutput a = new ConsoleOutput.Builder("output").setTerminate(true).build();
        final ConsoleOutput b = new ConsoleOutput.Builder("more output").setHasMore(true).build();

        assertEquals("ConsoleOutput[output=Optional[output],hasMore=false,terminate=true]", a.toString());
        assertEquals("ConsoleOutput[output=Optional[more output],hasMore=true,terminate=false]", b.toString());
    }

    @Test
    public void testBuilderCopy() {
        final ConsoleOutput a = new ConsoleOutput.Builder("output").build();
        final ConsoleOutput b = new ConsoleOutput.Builder(a).build();

        assertEquals(a, b);
    }
}
