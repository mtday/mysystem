package mysystem.shell.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Perform testing on the {@link ShellConfig} class.
 */
public class ShellConfigTest {
    @Test
    public void testGetKey() {
        assertEquals("mysystem.shell.commands", ShellConfig.SHELL_COMMANDS.getKey());
    }

    @Test
    public void testValues() {
        final List<String> keys =
                Arrays.asList(ShellConfig.values()).stream().map(s -> s.name()).collect(Collectors.toList());
        assertEquals("SHELL_COMMANDS", String.join(" ", new TreeSet<>(keys)));
    }

    @Test
    public void testValueOf() {
        assertEquals(ShellConfig.SHELL_COMMANDS, ShellConfig.valueOf(ShellConfig.SHELL_COMMANDS.name()));
    }
}
