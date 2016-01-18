package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli.Options;
import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.ActorRef;

/**
 * Perform testing of the {@link Registration} class and builder.
 */
public class RegistrationTest {
    @Test
    public void testCompareTo() {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration a = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final Registration b = new Registration.Builder(ref, new CommandPath.Builder("a", "b", "c").build()).build();
        final Registration c = new Registration.Builder(ref, new CommandPath.Builder("b", "c").build()).build();

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
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration a = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final Registration b = new Registration.Builder(ref, new CommandPath.Builder("a", "b", "c").build()).build();
        final Registration c = new Registration.Builder(ref, new CommandPath.Builder("b", "c").build()).build();

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
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration a = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final Registration b = new Registration.Builder(ref, new CommandPath.Builder("a", "b", "c").build()).build();
        final Registration c = new Registration.Builder(ref, new CommandPath.Builder("b", "c").build()).build();

        assertEquals(4066, a.hashCode());
        assertEquals(126145, b.hashCode());
        assertEquals(4098, c.hashCode());
    }

    @Test
    public void testBuilderWithPath() {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        assertEquals                                                                                       (
                "Registration[path=a b,description=Optional.empty]",
                new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build().toString());
    }

    @Test
    public void testBuilderCopy() {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration cmd = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        assertEquals                                                                                                 (
                "Registration[path=a b,description=Optional.empty]", new Registration.Builder(cmd).build().toString());
    }

    @Test
    public void testBuilderCopyWithOptions() {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration cmd = new Registration.Builder(ref, commandPath, new Options()).build();
        assertEquals                                                                                                 (
                "Registration[path=a b,description=Optional.empty]", new Registration.Builder(cmd).build().toString());
    }

    @Test
    public void testBuilderWithOptions() {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration cmd = new Registration.Builder(ref, commandPath, new Options()).build();
        assertEquals("Registration[path=a b,description=Optional.empty]", cmd.toString());
        assertTrue(cmd.getOptions().isPresent());
    }

    @Test
    public void testBuilderWithDescription() {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration cmd = new Registration.Builder(ref, commandPath, new Options(), "description").build();
        assertEquals("Registration[path=a b,description=Optional[description]]", cmd.toString());
    }
}
