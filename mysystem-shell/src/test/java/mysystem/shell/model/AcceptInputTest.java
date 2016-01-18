package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link AcceptInput} class and builder.
 */
public class AcceptInputTest {
    @Test
    public void testCompareTo() {
        final AcceptInput a = new AcceptInput.Builder().build();
        final AcceptInput b = new AcceptInput.Builder().build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(0, a.compareTo(b));
        assertEquals(0, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final AcceptInput a = new AcceptInput.Builder().build();
        final AcceptInput b = new AcceptInput.Builder().build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(b.equals(b));
    }

    @Test
    public void testHashCode() {
        assertEquals(-870946324, new AcceptInput.Builder().build().hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("mysystem.shell.model.AcceptInput", new AcceptInput.Builder().build().toString());
    }
}
