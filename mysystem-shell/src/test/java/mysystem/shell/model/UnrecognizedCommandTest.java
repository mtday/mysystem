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
 * Perform testing of the {@link UnrecognizedCommand} class and builder.
 */
public class UnrecognizedCommandTest {
    private final ManifestMapping mapping = new ManifestMapping();

    @Test
    public void testCompareTo() throws ParseException {
        final TokenizedUserInput uA = new TokenizedUserInput.Builder("a").build();
        final TokenizedUserInput uB = new TokenizedUserInput.Builder("b").build();
        final TokenizedUserInput uC = new TokenizedUserInput.Builder("c").build();

        final UnrecognizedCommand a = new UnrecognizedCommand.Builder(uA).build();
        final UnrecognizedCommand b = new UnrecognizedCommand.Builder(uB).build();
        final UnrecognizedCommand c = new UnrecognizedCommand.Builder(uC).build();

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

        final UnrecognizedCommand a = new UnrecognizedCommand.Builder(uA).build();
        final UnrecognizedCommand b = new UnrecognizedCommand.Builder(uB).build();
        final UnrecognizedCommand c = new UnrecognizedCommand.Builder(uC).build();

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

        final UnrecognizedCommand a = new UnrecognizedCommand.Builder(uA).build();
        final UnrecognizedCommand b = new UnrecognizedCommand.Builder(uB).build();
        final UnrecognizedCommand c = new UnrecognizedCommand.Builder(uC).build();

        assertEquals(26990, a.hashCode());
        assertEquals(27028, b.hashCode());
        assertEquals(27066, c.hashCode());
    }

    @Test
    public void testToJson() throws ParseException {
        final TokenizedUserInput input = new TokenizedUserInput.Builder("a").build();
        final UnrecognizedCommand cmd = new UnrecognizedCommand.Builder(input).build();
        assertEquals(
                "{\"userInput\":{\"userInput\":{\"input\":\"a\",\"manifest\":\"UserInput\"},\"tokens\":[\"a\"],"
                        + "\"manifest\":\"TokenizedUserInput\"},\"manifest\":\"UnrecognizedCommand\"}",
                cmd.toJson().toString());
    }

    @Test
    public void testToString() throws ParseException {
        final TokenizedUserInput input = new TokenizedUserInput.Builder("a").build();
        final UnrecognizedCommand cmd = new UnrecognizedCommand.Builder(input).build();
        assertEquals("UnrecognizedCommand[userInput=TokenizedUserInput[userInput=a,tokens=[a]]]", cmd.toString());
    }

    @Test
    public void testBuilderFromJson() throws ParseException {
        final TokenizedUserInput input = new TokenizedUserInput.Builder("a").build();
        final UnrecognizedCommand original = new UnrecognizedCommand.Builder(input).build();
        final UnrecognizedCommand copy = new UnrecognizedCommand.Builder().fromJson(mapping, original.toJson()).build();
        assertEquals(original, copy);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoInput() throws ParseException {
        final JsonObject json = new JsonParser().parse("{\"manifest\":\"UnrecognizedCommand\"}").getAsJsonObject();
        new UnrecognizedCommand.Builder().fromJson(mapping, json).build();
    }
}
