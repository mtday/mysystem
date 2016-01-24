package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link Registration} class and builder.
 */
public class RegistrationTest {
    @Test
    public void testCompareTo() {
        final Registration a =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build();
        final Registration b =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b", "c").build())
                        .build();
        final Registration c =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("b", "c").build())
                        .build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final Registration a =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build();
        final Registration b =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b", "c").build())
                        .build();
        final Registration c =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("b", "c").build())
                        .build();

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
        final Registration a =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build();
        final Registration b =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b", "c").build())
                        .build();
        final Registration c =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("b", "c").build())
                        .build();

        assertEquals(4066, a.hashCode());
        assertEquals(126145, b.hashCode());
        assertEquals(4098, c.hashCode());
    }

    @Test
    public void testToJson() {
        assertEquals("{\"actorPath\":\"path\",\"path\":{\"path\":[\"a\",\"b\"]}}",
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build().toJson().toString());
    }

    @Test
    public void testBuilderWithPath() {
        assertEquals("Registration[actorPath=path,path=a b,options=Optional.empty,description=Optional.empty]",
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build().toString());
    }

    @Test
    public void testBuilderCopy() {
        final Registration cmd =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build();
        assertEquals("Registration[actorPath=path,path=a b,options=Optional.empty,description=Optional.empty]",
                new Registration.Builder(cmd).build().toString());
    }

    @Test
    public void testBuilderCopyWithOptions() {
        final Option option = new Option.Builder().setDescription("description").setShortOption("s").build();
        final Options options = new Options.Builder(option).build();

        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration cmd =
                new Registration.Builder().setActorPath("path").setPath(commandPath).setOptions(options).build();
        assertEquals(
                "Registration[actorPath=path,path=a b,options=Optional[Options[options=[Option[description=description,"
                        + "shortOption=s,longOption=Optional.empty,argName=Optional.empty,arguments=0,required=false,"
                        + "optionalArg=false]]]],description=Optional.empty]",
                new Registration.Builder(cmd).build().toString());
    }

    @Test
    public void testBuilderWithOptions() {
        final Option option = new Option.Builder().setDescription("description").setShortOption("s").build();
        final Options options = new Options.Builder(option).build();

        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration cmd =
                new Registration.Builder().setActorPath("path").setPath(commandPath).setOptions(options).build();
        assertEquals("Registration[actorPath=path,path=a b,"
                + "options=Optional[Options[options=[Option[description=description,shortOption=s,"
                + "longOption=Optional.empty,argName=Optional.empty,arguments=0,required=false,"
                + "optionalArg=false]]]],description=Optional.empty]", cmd.toString());
        assertTrue(cmd.getOptions().isPresent());
    }

    @Test
    public void testBuilderWithDescription() {
        final Option option = new Option.Builder().setDescription("description").setShortOption("s").build();
        final Options options = new Options.Builder(option).build();

        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration cmd =
                new Registration.Builder().setActorPath("path").setPath(commandPath).setOptions(options)
                        .setDescription("description").build();
        assertEquals(
                "Registration[actorPath=path,path=a b,"
                        + "options=Optional[Options[options=[Option[description=description,shortOption=s,"
                        + "longOption=Optional.empty,argName=Optional.empty,arguments=0,required=false,"
                        + "optionalArg=false]]]],description=Optional[description]]",
                cmd.toString());
    }
}
