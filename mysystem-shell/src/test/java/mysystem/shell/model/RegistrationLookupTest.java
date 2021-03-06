package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import mysystem.common.serialization.ManifestMapping;

import java.text.ParseException;

/**
 * Perform testing of the {@link RegistrationLookup} class and builder.
 */
public class RegistrationLookupTest {
    private final ManifestMapping mapping = new ManifestMapping();

    @Test
    public void testCompareTo() throws ParseException {
        final CommandPath cA = new CommandPath.Builder("a b").build();
        final CommandPath cB = new CommandPath.Builder("a c").build();
        final CommandPath cC = new CommandPath.Builder("b c").build();

        final TokenizedUserInput u = new TokenizedUserInput.Builder("a").build();

        final RegistrationLookup a = new RegistrationLookup.Builder(cA, cB).build();
        final RegistrationLookup b = new RegistrationLookup.Builder(cA, cB, cC).build();
        final RegistrationLookup c = new RegistrationLookup.Builder(u).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(2, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(2, b.compareTo(c));
        assertEquals(-2, c.compareTo(a));
        assertEquals(-2, c.compareTo(b));
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

        assertEquals(54999, a.hashCode());
        assertEquals(55074, b.hashCode());
        assertEquals(55149, c.hashCode());
    }

    @Test
    public void testToJson() throws ParseException {
        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("a").build();
        final RegistrationLookup lookup = new RegistrationLookup.Builder(userInput).build();
        assertEquals("{\"paths\":[{\"path\":[\"a\"],\"manifest\":\"CommandPath\"}],"
                        + "\"userInput\":{\"userInput\":{\"input\":\"a\",\"manifest\":\"UserInput\"},"
                        + "\"tokens\":[\"a\"],\"manifest\":\"TokenizedUserInput\"}," +
                "\"manifest\":\"RegistrationLookup\"}",
                lookup.toJson().toString());
    }

    @Test
    public void testToString() throws ParseException {
        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("a").build();
        final RegistrationLookup lookup = new RegistrationLookup.Builder(userInput).build();
        assertEquals("RegistrationLookup[paths=[a],userInput=Optional[TokenizedUserInput[userInput=a,tokens=[a]]]]",
                lookup.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoPaths() throws ParseException {
        new RegistrationLookup.Builder().build();
    }

    @Test
    public void testBuilderFromJson() throws ParseException {
        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("a").build();
        final RegistrationLookup original = new RegistrationLookup.Builder(userInput).build();
        final RegistrationLookup copy = new RegistrationLookup.Builder().fromJson(mapping, original.toJson()).build();
        assertEquals(original, copy);
    }

    @Test
    public void testBuilderFromJsonNoPaths() throws ParseException {
        final String jsonStr = "{\"userInput\":{\"userInput\":{\"input\":\"a\",\"manifest\":\"UserInput\"},"
                + "\"tokens\":[\"a\"],\"manifest\":\"TokenizedUserInput\"}," + "\"manifest\":\"RegistrationLookup\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();

        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("a").build();
        final RegistrationLookup expected = new RegistrationLookup.Builder(userInput).build();

        final RegistrationLookup copy = new RegistrationLookup.Builder().fromJson(mapping, json).build();

        assertEquals(expected, copy);
    }
}
