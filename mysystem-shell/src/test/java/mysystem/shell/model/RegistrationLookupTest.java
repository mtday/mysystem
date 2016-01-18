package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.ConfigException;
import org.junit.Test;

import java.text.ParseException;

/**
 * Perform testing of the {@link RegistrationLookup} class and builder.
 */
public class RegistrationLookupTest {
    @Test
    public void testCompareTo() throws ParseException {
        final TokenizedUserInput uA = new TokenizedUserInput.Builder("a").build();
        final TokenizedUserInput uB = new TokenizedUserInput.Builder("b").build();
        final TokenizedUserInput uC = new TokenizedUserInput.Builder("c").build();

        final RegistrationLookup a = new RegistrationLookup.Builder(uA).build();
        final RegistrationLookup b = new RegistrationLookup.Builder(uB).build();
        final RegistrationLookup c = new RegistrationLookup.Builder(uC).build();

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
    public void testEquals() throws ParseException {
        final TokenizedUserInput uA = new TokenizedUserInput.Builder("a").build();
        final TokenizedUserInput uB = new TokenizedUserInput.Builder("b").build();
        final TokenizedUserInput uC = new TokenizedUserInput.Builder("c").build();

        final RegistrationLookup a = new RegistrationLookup.Builder(uA).build();
        final RegistrationLookup b = new RegistrationLookup.Builder(uB).build();
        final RegistrationLookup c = new RegistrationLookup.Builder(uC).build();

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
    public void testHashCode() throws ParseException {
        final TokenizedUserInput uA = new TokenizedUserInput.Builder("a").build();
        final TokenizedUserInput uB = new TokenizedUserInput.Builder("b").build();
        final TokenizedUserInput uC = new TokenizedUserInput.Builder("c").build();

        final RegistrationLookup a = new RegistrationLookup.Builder(uA).build();
        final RegistrationLookup b = new RegistrationLookup.Builder(uB).build();
        final RegistrationLookup c = new RegistrationLookup.Builder(uC).build();

        assertEquals(128, a.hashCode());
        assertEquals(129, b.hashCode());
        assertEquals(130, c.hashCode());
    }

    @Test
    public void testToString() throws ParseException {
        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("a").build();
        final RegistrationLookup lookup = new RegistrationLookup.Builder(userInput).build();
        assertEquals("RegistrationLookup[userInput=a]", lookup.toString());
    }
}