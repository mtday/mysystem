package mysystem.common.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;

/**
 * Perform testing of the {@link CommonConfig} enumeration.
 */
public class CommonConfigTest {
    @Test
    public void test() {
        // This is only here for 100% coverage.
        assertEquals(CommonConfig.VERSION, CommonConfig.valueOf("VERSION"));
        assertEquals("[VERSION, ACTOR_SYSTEM_NAME]", Arrays.asList(CommonConfig.values()).toString());
    }

    @Test
    public void testGetKey() {
        assertEquals("mysystem.version", CommonConfig.VERSION.getKey());
        assertEquals("mysystem.actor.system.name", CommonConfig.ACTOR_SYSTEM_NAME.getKey());
    }
}
