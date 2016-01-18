package mysystem.shell.util;

/**
 *
 */
public class BadArgumentException extends Exception {
    public BadArgumentException(final String msg, final String input, final int index) {
        super(msg);
    }
}
