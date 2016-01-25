package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import akka.actor.UntypedActor;
import mysystem.shell.command.ExitCommand;
import mysystem.shell.command.HelpCommand;

import java.util.Objects;

/**
 * Perform testing of the {@link CommandConfig} class and builder.
 */
public class CommandConfigTest {
    private Config getConfig() {
        return ConfigFactory.parseString("{ }");
    }

    private Config getConfig(final String className) {
        Objects.requireNonNull(className);
        return ConfigFactory.parseString(String.format("{ class = \"%s\" }", className));
    }

    private Config getConfig(final Class<? extends UntypedActor> actorClass) {
        Objects.requireNonNull(actorClass);
        return ConfigFactory.parseString(String.format("{ class = \"%s\" }", actorClass.getName()));
    }

    @Test
    public void testCompareTo() {
        final CommandConfig a = new CommandConfig.Builder("a", getConfig(ExitCommand.class)).build();
        final CommandConfig b = new CommandConfig.Builder("a", getConfig(HelpCommand.class)).build();
        final CommandConfig c = new CommandConfig.Builder("b", getConfig(HelpCommand.class)).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-3, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(3, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final CommandConfig a = new CommandConfig.Builder("a", getConfig(ExitCommand.class)).build();
        final CommandConfig b = new CommandConfig.Builder("a", getConfig(HelpCommand.class)).build();
        final CommandConfig c = new CommandConfig.Builder("b", getConfig(HelpCommand.class)).build();

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
        final CommandConfig a = new CommandConfig.Builder("a", getConfig(ExitCommand.class)).build();
        final CommandConfig b = new CommandConfig.Builder("a", getConfig(HelpCommand.class)).build();
        final CommandConfig c = new CommandConfig.Builder("b", getConfig(HelpCommand.class)).build();

        assertEquals(-32232217, a.hashCode());
        assertEquals(-1198032540, b.hashCode());
        assertEquals(-1198032503, c.hashCode());
    }

    @Test
    public void testToJson() {
        assertEquals(
                "{\"commandName\":\"a\",\"commandClass\":\"mysystem.shell.command.ExitCommand\",\"manifest"
                        + "\":\"CommandConfig\"}",
                new CommandConfig.Builder("a", getConfig(ExitCommand.class)).build().toJson().toString());
    }

    @Test
    public void testToString() {
        assertEquals(
                "CommandConfig[commandName=a,commandClass=mysystem.shell.command.ExitCommand]",
                new CommandConfig.Builder("a", getConfig(ExitCommand.class)).build().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderNoClass() {
        new CommandConfig.Builder("a", getConfig()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderMissingClass() {
        new CommandConfig.Builder("a", getConfig("missing")).build();
    }

    @Test
    public void testBuilderCopy() {
        final CommandConfig a = new CommandConfig.Builder("a", getConfig(ExitCommand.class)).build();
        final CommandConfig b = new CommandConfig.Builder(a).build();

        assertEquals(a, b);
    }
}
