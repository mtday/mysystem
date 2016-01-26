package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import mysystem.common.serialization.ManifestMapping;

/**
 * Perform testing of the {@link Option} class and builder.
 */
public class OptionTest {
    private final ManifestMapping mapping = new ManifestMapping();

    @Test
    public void testCompareTo() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(1, a.compareTo(b));
        assertEquals(-1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertTrue(b.equals(b));
    }

    @Test
    public void testHashCode() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();

        assertEquals(582621274, a.hashCode());
        assertEquals(-1781598390, b.hashCode());
    }

    @Test
    public void testAsOption() {
        final Option ours = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final org.apache.commons.cli.Option theirs = ours.asOption();
        assertEquals(ours.getDescription(), theirs.getDescription());
        assertEquals(ours.getShortOption(), theirs.getOpt());
        assertEquals(ours.getLongOption().get(), theirs.getLongOpt());
        assertEquals(ours.getArgName().get(), theirs.getArgName());
        assertEquals(ours.getArguments(), theirs.getArgs());
        assertEquals(ours.isRequired(), theirs.isRequired());
        assertEquals(ours.hasOptionalArg(), theirs.hasOptionalArg());
    }

    @Test
    public void testToJson() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();

        assertEquals("{\"description\":\"description\",\"shortOption\":\"s\",\"longOption\":\"long\","
                + "\"argName\":\"argName\",\"arguments\":1,\"required\":true,\"optionalArg\":true,"
                + "\"manifest\":\"Option\"}", a.toJson().toString());
        assertEquals("{\"description\":\"description\",\"shortOption\":\"s\",\"longOption\":\"long\",\"arguments\":0,"
                + "\"required\":false,\"optionalArg\":false,\"manifest\":\"Option\"}", b.toJson().toString());
    }

    @Test
    public void testToString() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();

        assertEquals("Option[description=description,shortOption=s,longOption=Optional[long],argName=Optional[argName],"
                + "arguments=1,required=true,optionalArg=true]", a.toString());
        assertEquals("Option[description=description,shortOption=s,longOption=Optional[long],argName=Optional.empty,"
                + "arguments=0,required=false,optionalArg=false]", b.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderSetArgumentsNegative() {
        new Option.Builder().setArguments(-1);
    }

    @Test
    public void testBuilderCopy() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b = new Option.Builder(a).build();
        assertEquals(a, b);
    }

    @Test
    public void testBuilderCopyNoOptionals() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setArguments(1).build();
        final Option b = new Option.Builder(a).build();
        assertEquals(a, b);
    }

    @Test
    public void testBuilderFromJson() {
        final Option original =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                        .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option copy = new Option.Builder().fromJson(mapping, original.toJson()).build();
        assertEquals(original, copy);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoDescription() {
        final String jsonStr = "{\"shortOption\":\"s\",\"longOption\":\"long\","
                + "\"argName\":\"argName\",\"arguments\":1,\"required\":true,\"optionalArg\":true,"
                + "\"manifest\":\"Option\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Option.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoShortOption() {
        final String jsonStr = "{\"description\":\"description\",\"longOption\":\"long\","
                + "\"argName\":\"argName\",\"arguments\":1,\"required\":true,\"optionalArg\":true,"
                + "\"manifest\":\"Option\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Option.Builder().fromJson(mapping, json).build();
    }

    @Test
    public void testBuilderFromJsonNoArguments() {
        final String jsonStr = "{\"description\":\"description\",\"shortOption\":\"s\",\"longOption\":\"long\","
                + "\"argName\":\"argName\",\"required\":true,\"optionalArg\":true," + "\"manifest\":\"Option\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        final Option option = new Option.Builder().fromJson(mapping, json).build();
        assertEquals(0, option.getArguments());
    }

    @Test
    public void testBuilderFromJsonNoRequired() {
        final String jsonStr = "{\"description\":\"description\",\"shortOption\":\"s\",\"longOption\":\"long\","
                + "\"argName\":\"argName\",\"arguments\":1,\"optionalArg\":true," + "\"manifest\":\"Option\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        final Option option = new Option.Builder().fromJson(mapping, json).build();
        assertFalse(option.isRequired());
    }

    @Test
    public void testBuilderFromJsonNoOptionalArg() {
        final String jsonStr = "{\"description\":\"description\",\"shortOption\":\"s\",\"longOption\":\"long\","
                + "\"argName\":\"argName\",\"arguments\":1,\"required\":true," + "\"manifest\":\"Option\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        final Option option = new Option.Builder().fromJson(mapping, json).build();
        assertFalse(option.hasOptionalArg());
    }
}
