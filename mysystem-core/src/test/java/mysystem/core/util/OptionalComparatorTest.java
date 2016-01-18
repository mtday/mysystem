package mysystem.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * Perform testing of the {@link OptionalComparatorTest} class.
 */
public class OptionalComparatorTest {
    @Test
    public void testCompareBothEmpty() {
        final Optional<Integer> a = Optional.empty();
        final Optional<Integer> b = Optional.empty();

        Assert.assertEquals(0, new OptionalComparator().compare(a, b));
    }

    @Test
    public void testCompareFirstEmpty() {
        final Optional<Integer> a = Optional.empty();
        final Optional<Integer> b = Optional.of(1);

        Assert.assertEquals(-1, new OptionalComparator().compare(a, b));
    }

    @Test
    public void testCompareSecondEmpty() {
        final Optional<Integer> a = Optional.of(1);
        final Optional<Integer> b = Optional.empty();

        Assert.assertEquals(1, new OptionalComparator().compare(a, b));
    }

    @Test
    public void testCompareBothWithValues() {
        final Optional<Integer> a = Optional.of(1);
        final Optional<Integer> b = Optional.of(1);

        Assert.assertEquals(0, new OptionalComparator().compare(a, b));
    }
}
