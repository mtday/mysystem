package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link RegistrationRequest} class and builder.
 */
public class RegistrationRequestTest {
    @Test
    public void testCompareTo() {
        final RegistrationRequest a = new RegistrationRequest.Builder().build();
        final RegistrationRequest b = new RegistrationRequest.Builder().build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(0, a.compareTo(b));
        assertEquals(0, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final RegistrationRequest a = new RegistrationRequest.Builder().build();
        final RegistrationRequest b = new RegistrationRequest.Builder().build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(b.equals(b));
    }

    @Test
    public void testHashCode() {
        assertEquals(800323552, new RegistrationRequest.Builder().build().hashCode());
    }

    @Test
    public void testToJson() {
        assertEquals(
                "{\"manifest\":\"RegistrationRequest\"}",
                new RegistrationRequest.Builder().build().toJson().toString());
    }

    @Test
    public void testToString() {
        assertEquals("mysystem.shell.model.RegistrationRequest", new RegistrationRequest.Builder().build().toString());
    }
}
