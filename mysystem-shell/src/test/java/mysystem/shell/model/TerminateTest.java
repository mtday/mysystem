package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link Terminate} class and builder.
 */
public class TerminateTest {
    @Test
    public void testCompareTo() {
        final Terminate a = new Terminate.Builder().build();
        final Terminate b = new Terminate.Builder().build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(0, a.compareTo(b));
        assertEquals(0, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final Terminate a = new Terminate.Builder().build();
        final Terminate b = new Terminate.Builder().build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(b.equals(b));
    }

    @Test
    public void testHashCode() {
        assertEquals(1882495787, new Terminate.Builder().build().hashCode());
    }

    @Test
    public void testToJson() {
        assertEquals("{}", new Terminate.Builder().build().toJson().toString());
    }

    @Test
    public void testToString() {
        assertEquals("mysystem.shell.model.Terminate", new Terminate.Builder().build().toString());
    }
}
