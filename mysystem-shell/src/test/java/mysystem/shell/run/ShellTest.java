package mysystem.shell.run;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Perform testing of the {@link Shell} class.
 */
public class ShellTest {
    @Test
    public void test() {
        final Shell shell = new Shell();
        try {
            assertNotNull(shell.getActorSystem());
        } finally {
            shell.terminate();
        }
    }
}
