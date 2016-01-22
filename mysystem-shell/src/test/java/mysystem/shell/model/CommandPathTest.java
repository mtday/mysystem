package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;

import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;

/**
 * Perform testing of the {@link CommandPath} class and builder.
 */
public class CommandPathTest {
    @Test
    public void testCompareTo() {
        final CommandPath a = new CommandPath.Builder("a", "b").build();
        final CommandPath b = new CommandPath.Builder("a", "b", "c").build();
        final CommandPath c = new CommandPath.Builder("b", "c").build();

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
        final CommandPath a = new CommandPath.Builder("a", "b").build();
        final CommandPath b = new CommandPath.Builder("a", "b", "c").build();
        final CommandPath c = new CommandPath.Builder("b", "c").build();

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
        final CommandPath a = new CommandPath.Builder("a", "b").build();
        final CommandPath b = new CommandPath.Builder("a", "b", "c").build();
        final CommandPath c = new CommandPath.Builder("b", "c").build();

        assertEquals(4066, a.hashCode());
        assertEquals(126145, b.hashCode());
        assertEquals(4098, c.hashCode());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoPaths() {
        new CommandPath.Builder().build();
    }

    @Test
    public void testBuilderWithPathList() {
        assertEquals("a b", new CommandPath.Builder(Arrays.asList("a", "b")).build().toString());
    }

    @Test
    public void testBuilderWithPathVarargs() {
        assertEquals("a b", new CommandPath.Builder("a", "b").build().toString());
    }

    @Test
    public void testBuilderWithAddList() {
        assertEquals("a b", new CommandPath.Builder().add(Arrays.asList("a", "b")).build().toString());
    }

    @Test
    public void testBuilderWithAddVarargs() {
        assertEquals("a b", new CommandPath.Builder().add("a", "b").build().toString());
    }

    @Test
    public void testBuilderWithMultipleAdds() {
        assertEquals                                                                                      ("a b c d e",
                new CommandPath.Builder("a", "b").add("c").add(Arrays.asList("d", "e")).build().toString());
    }

    @Test
    public void testBuilderWithCommandPath() {
        final CommandPath path = new CommandPath.Builder("a", "b").build();
        assertEquals("a b c", new CommandPath.Builder(path).add("c").build().toString());
    }

    @Test
    public void testBuilderWithUserInput() throws ParseException {
        final TokenizedUserInput uA = new TokenizedUserInput.Builder("  a   bc\td  ").build();
        final TokenizedUserInput uB = new TokenizedUserInput.Builder("  a   bc\td  -e  ").build();

        final CommandPath a = new CommandPath.Builder(uA).build();
        final CommandPath b = new CommandPath.Builder(uB).build();

        assertEquals("a bc d", a.toString());
        assertEquals("a bc d", b.toString());
    }

    @Test
    public void testBuilderWithEmptyString() throws ParseException {
        assertEquals("a", new CommandPath.Builder(new TokenizedUserInput.Builder("a  ''").build()).build().toString());
    }

    @Test
    public void testBuilderWithString() throws ParseException {
        assertEquals(
                "a b", new CommandPath.Builder(new TokenizedUserInput.Builder("a 'b'").build()).build().toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderWithUserInputWhitespace() throws ParseException {
        new CommandPath.Builder(new TokenizedUserInput.Builder("  ").build()).build();
    }

    @Test
    public void testIsPrefix() {
        final CommandPath a = new CommandPath.Builder("a", "b", "c").build();
        final CommandPath b = new CommandPath.Builder("a", "b").build();
        final CommandPath c = new CommandPath.Builder("a", "c").build();
        final CommandPath d = new CommandPath.Builder("one", "two", "three").build();
        final CommandPath e = new CommandPath.Builder("one", "tw").build();

        assertTrue(a.isPrefix(a));
        assertTrue(a.isPrefix(b));
        assertFalse(a.isPrefix(c));
        assertFalse(a.isPrefix(d));
        assertFalse(a.isPrefix(e));

        assertFalse(b.isPrefix(a));
        assertTrue(b.isPrefix(b));
        assertFalse(b.isPrefix(c));
        assertFalse(b.isPrefix(d));
        assertFalse(b.isPrefix(e));

        assertFalse(c.isPrefix(a));
        assertFalse(c.isPrefix(b));
        assertTrue(c.isPrefix(c));
        assertFalse(c.isPrefix(d));
        assertFalse(c.isPrefix(e));

        assertFalse(d.isPrefix(a));
        assertFalse(d.isPrefix(b));
        assertFalse(d.isPrefix(c));
        assertTrue(d.isPrefix(d));
        assertTrue(d.isPrefix(e));

        assertFalse(e.isPrefix(a));
        assertFalse(e.isPrefix(b));
        assertFalse(e.isPrefix(c));
        assertFalse(e.isPrefix(d));
        assertTrue(e.isPrefix(e));
    }

    @Test
    public void testGetParent() {
        final CommandPath a = new CommandPath.Builder("a", "b", "c").build();

        final Optional<CommandPath> b = a.getParent();
        assertTrue(b.isPresent());
        assertEquals("a b", b.get().toString());

        final Optional<CommandPath> c = b.get().getParent();
        assertTrue(c.isPresent());
        assertEquals("a", c.get().toString());

        final Optional<CommandPath> d = c.get().getParent();
        assertFalse(d.isPresent());
    }

    @Test
    public void testGetChild() {
        final CommandPath a = new CommandPath.Builder("a", "b", "c").build();

        final Optional<CommandPath> b = a.getChild();
        assertTrue(b.isPresent());
        assertEquals("b c", b.get().toString());

        final Optional<CommandPath> c = b.get().getChild();
        assertTrue(c.isPresent());
        assertEquals("c", c.get().toString());

        final Optional<CommandPath> d = c.get().getChild();
        assertFalse(d.isPresent());
    }

    @Test
    public void testGetSize() {
        final CommandPath a = new CommandPath.Builder("a", "b").build();
        final CommandPath b = new CommandPath.Builder("a", "b", "c").build();

        assertEquals(2, a.getSize());
        assertEquals(3, b.getSize());
    }
}
