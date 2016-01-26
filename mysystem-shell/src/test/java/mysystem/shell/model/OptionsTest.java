package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import mysystem.common.serialization.ManifestMapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Perform testing of the {@link Options} class and builder.
 */
public class OptionsTest {
    private final ManifestMapping mapping = new ManifestMapping();

    @Test
    public void testCompareTo() {
        final Option oa = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option ob =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();

        final Options a = new Options.Builder(oa).build();
        final Options b = new Options.Builder(ob).build();
        final Options c = new Options.Builder(oa, ob).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(1, a.compareTo(b));
        assertEquals(1, a.compareTo(c));
        assertEquals(-1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(-1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final Option oa = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option ob =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();

        final Options a = new Options.Builder(oa).build();
        final Options b = new Options.Builder(ob).build();
        final Options c = new Options.Builder(oa, ob).build();

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
        final Option oa = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option ob =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();

        final Options a = new Options.Builder(oa).build();
        final Options b = new Options.Builder(ob).build();
        final Options c = new Options.Builder(oa, ob).build();

        assertEquals(582621903, a.hashCode());
        assertEquals(-1781597761, b.hashCode());
        assertEquals(-1198976487, c.hashCode());
    }

    @Test
    public void testAsOptions() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("a").setLongOption("longA")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("b").setLongOption("longB").build();
        final Options ours = new Options.Builder(a, b).build();
        final org.apache.commons.cli.Options theirs = ours.asOptions();

        assertEquals(2, ours.getOptions().size());
        final Collection<org.apache.commons.cli.Option> options = theirs.getOptions();
        assertEquals(2, options.size());
        final Iterator<org.apache.commons.cli.Option> iter = options.iterator();

        final org.apache.commons.cli.Option A = iter.next();
        assertEquals(a.getDescription(), A.getDescription());
        assertEquals(a.getShortOption(), A.getOpt());
        assertEquals(a.getLongOption().get(), A.getLongOpt());
        assertEquals(a.getArgName().get(), A.getArgName());
        assertEquals(a.getArguments(), A.getArgs());
        assertEquals(a.isRequired(), A.isRequired());
        assertEquals(a.hasOptionalArg(), A.hasOptionalArg());

        final org.apache.commons.cli.Option B = iter.next();
        assertEquals(b.getDescription(), B.getDescription());
        assertEquals(b.getShortOption(), B.getOpt());
        assertEquals(b.getLongOption().get(), B.getLongOpt());
        assertNull(B.getArgName());
        assertEquals(b.getArguments(), B.getArgs());
        assertEquals(b.isRequired(), B.isRequired());
        assertEquals(b.hasOptionalArg(), B.hasOptionalArg());

        assertFalse(iter.hasNext());
    }

    @Test
    public void testToJson() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();
        final Options options = new Options.Builder(a, b).build();

        assertEquals("{\"options\":[{\"description\":\"description\",\"shortOption\":\"s\",\"longOption\":\"long\","
                + "\"arguments\":0,\"required\":false,\"optionalArg\":false,\"manifest\":\"Option\"},"
                + "{\"description\":\"description\",\"shortOption\":\"s\",\"longOption\":\"long\","
                + "\"argName\":\"argName\",\"arguments\":1,\"required\":true,\"optionalArg\":true,"
                + "\"manifest\":\"Option\"}],\"manifest\":\"Options\"}", options.toJson().toString());
    }

    @Test
    public void testToString() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();
        final Options options = new Options.Builder(a, b).build();

        assertEquals(
                "Options[options=[Option[description=description,shortOption=s,longOption=Optional[long],"
                        + "argName=Optional.empty,arguments=0,required=false,optionalArg=false], "
                        + "Option[description=description,shortOption=s,longOption=Optional[long],"
                        + "argName=Optional[argName],arguments=1,required=true,optionalArg=true]]]",
                options.toString());
    }

    @Test
    public void testBuilderCopy() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();
        final Options original = new Options.Builder(a, b).build();
        final Options copy = new Options.Builder(original).build();
        assertEquals(original, copy);
    }

    @Test
    public void testBuilderWithCollection() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();
        final Options options = new Options.Builder(Arrays.asList(a, b)).build();
        assertTrue(options.getOptions().contains(a));
        assertTrue(options.getOptions().contains(b));
    }

    @Test
    public void testBuilderFromJson() {
        final Option a = new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long")
                .setArgName("argName").setArguments(1).setRequired(true).setOptionalArg(true).build();
        final Option b =
                new Option.Builder().setDescription("description").setShortOption("s").setLongOption("long").build();
        final Options original = new Options.Builder(a, b).build();
        final Options copy = new Options.Builder().fromJson(mapping, original.toJson()).build();
        assertEquals(original, copy);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromJsonNoOptions() {
        final String jsonStr = "{\"manifest\":\"Options\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Options.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoOptions() {
        new Options.Builder().build();
    }
}
