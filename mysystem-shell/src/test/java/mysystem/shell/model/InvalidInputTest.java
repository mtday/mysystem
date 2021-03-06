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
 * Perform testing of the {@link InvalidInput} class and builder.
 */
public class InvalidInputTest {
    private final ManifestMapping mapping = new ManifestMapping();

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
    public void testToJson() {
        final ParseException ex = new ParseException("Message", 10);
        final InvalidInput a = new InvalidInput.Builder(new UserInput.Builder("a").build(), ex).build();
        assertEquals("{\"userInput\":{\"input\":\"a\",\"manifest\":\"UserInput\"},\"error\":\"Message\","
                + "\"location\":10,\"manifest\":\"InvalidInput\"}", a.toJson().toString());
    }

    @Test
    public void testToString() {
        final ParseException ex = new ParseException("Message", 10);
        final InvalidInput a = new InvalidInput.Builder(new UserInput.Builder("a").build(), ex).build();
        assertEquals("InvalidInput[userInput=a,error=Message,location=Optional[10]]", a.toString());
    }

    @Test
    public void testBuilderCopy() {
        final ParseException ex = new ParseException("Message", 10);
        final InvalidInput a = new InvalidInput.Builder(new UserInput.Builder("a").build(), ex).build();
        final InvalidInput b = new InvalidInput.Builder(a).build();

        assertEquals(a, b);
    }

    @Test
    public void testBuilderTokenizedInput() throws ParseException {
        final org.apache.commons.cli.ParseException ex = new org.apache.commons.cli.ParseException("Message");
        final InvalidInput a = new InvalidInput.Builder(new TokenizedUserInput.Builder("a").build(), ex).build();
        final InvalidInput b = new InvalidInput.Builder(a).build();

        assertEquals(a, b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNegativeErrorOffset() {
        new InvalidInput.Builder().setLocation(-1).build();
    }

    @Test
    public void testBuilderWithNegativeErrorInParseException() {
        final ParseException ex = new ParseException("Message", -1);
        final InvalidInput a = new InvalidInput.Builder(new UserInput.Builder("a").build(), ex).build();

        assertEquals("Message", a.getError());
        assertEquals("a", a.getUserInput().getInput());
        assertFalse(a.getLocation().isPresent());
    }

    @Test
    public void testBuilderFromJson() throws ParseException {
        final org.apache.commons.cli.ParseException ex = new org.apache.commons.cli.ParseException("Message");
        final InvalidInput original = new InvalidInput.Builder(new TokenizedUserInput.Builder("a").build(), ex).build();
        final InvalidInput copy = new InvalidInput.Builder().fromJson(mapping, original.toJson()).build();
        assertEquals(original, copy);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoUserInput() throws ParseException {
        new InvalidInput.Builder().setError("error").build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoError() throws ParseException {
        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("a").build();
        new InvalidInput.Builder().setUserInput(userInput).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoUserInput() {
        final String jsonStr = "{\"error\":\"Message\",\"location\":10,\"manifest\":\"InvalidInput\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new InvalidInput.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoError() {
        final String jsonStr = "{\"userInput\":{\"input\":\"a\",\"manifest\":\"UserInput\"},"
                + "\"location\":10,\"manifest\":\"InvalidInput\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new InvalidInput.Builder().fromJson(mapping, json).build();
    }
}
