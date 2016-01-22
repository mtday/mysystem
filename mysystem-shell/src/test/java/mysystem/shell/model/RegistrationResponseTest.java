package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.ActorRef;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing of the {@link RegistrationResponse} class and builder.
 */
public class RegistrationResponseTest {
    @Test
    public void testCompareTo() {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration rA = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final Registration rB = new Registration.Builder(ref, new CommandPath.Builder("b", "c").build()).build();
        final Registration rC = new Registration.Builder(ref, new CommandPath.Builder("a", "b", "c").build()).build();

        final RegistrationResponse a = new RegistrationResponse.Builder(rA).build();
        final RegistrationResponse b = new RegistrationResponse.Builder(rB).build();
        final RegistrationResponse c = new RegistrationResponse.Builder(rC).build();
        final RegistrationResponse d = new RegistrationResponse.Builder(rA, rB).build();
        final RegistrationResponse e = new RegistrationResponse.Builder(rA, rC).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-1, a.compareTo(d));
        assertEquals(-1, a.compareTo(e));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(1, b.compareTo(c));
        assertEquals(1, b.compareTo(d));
        assertEquals(1, b.compareTo(e));
        assertEquals(1, c.compareTo(a));
        assertEquals(-1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(1, c.compareTo(d));
        assertEquals(1, c.compareTo(e));
        assertEquals(1, d.compareTo(a));
        assertEquals(-1, d.compareTo(b));
        assertEquals(-1, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
        assertEquals(1, d.compareTo(e));
        assertEquals(1, e.compareTo(a));
        assertEquals(-1, e.compareTo(b));
        assertEquals(-1, e.compareTo(c));
        assertEquals(-1, e.compareTo(d));
        assertEquals(0, e.compareTo(e));
    }

    @Test
    public void testEquals() {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration rA = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final Registration rB = new Registration.Builder(ref, new CommandPath.Builder("b", "c").build()).build();
        final Registration rC = new Registration.Builder(ref, new CommandPath.Builder("a", "b", "c").build()).build();

        final RegistrationResponse a = new RegistrationResponse.Builder(rA).build();
        final RegistrationResponse b = new RegistrationResponse.Builder(rB).build();
        final RegistrationResponse c = new RegistrationResponse.Builder(rC).build();
        final RegistrationResponse d = new RegistrationResponse.Builder(rA, rB).build();
        final RegistrationResponse e = new RegistrationResponse.Builder(rA, rC).build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals(d));
        assertFalse(a.equals(e));
        assertFalse(b.equals(a));
        assertTrue(b.equals(b));
        assertFalse(b.equals(c));
        assertFalse(b.equals(d));
        assertFalse(b.equals(e));
        assertFalse(c.equals(a));
        assertFalse(c.equals(b));
        assertTrue(c.equals(c));
        assertFalse(c.equals(d));
        assertFalse(c.equals(e));
        assertFalse(d.equals(a));
        assertFalse(d.equals(b));
        assertFalse(d.equals(c));
        assertTrue(d.equals(d));
        assertFalse(d.equals(e));
        assertFalse(e.equals(a));
        assertFalse(e.equals(b));
        assertFalse(e.equals(c));
        assertFalse(e.equals(d));
        assertTrue(e.equals(e));
    }

    @Test
    public void testHashCode() {
        assertEquals(2040755605, new RegistrationResponse.Builder().build().hashCode());
    }

    @Test
    public void testToString() {
        assertEquals(
                "RegistrationResponse[registrations=[],userInput=Optional.absent()]",
                new RegistrationResponse.Builder().build().toString());
    }

    @Test
    public void testBuilderCopy() {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration rA = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final Registration rB = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();

        final RegistrationResponse a = new RegistrationResponse.Builder(Arrays.asList(rA, rB)).build();
        final RegistrationResponse b = new RegistrationResponse.Builder(a).build();

        assertEquals(a, b);
    }

    @Test
    public void testBuilderWithUserInput() throws ParseException {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final TokenizedUserInput userInput = new TokenizedUserInput.Builder("input").build();
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration registration = new Registration.Builder(ref, commandPath).build();
        final RegistrationResponse a = new RegistrationResponse.Builder(registration).setUserInput(userInput).build();

        assertNotNull(a.getUserInput());
        assertTrue(a.getUserInput().isPresent());
        assertEquals(userInput, a.getUserInput().get());
    }

    @Test
    public void testBuilderWithLookup() throws ParseException {
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration regA = new Registration.Builder(ref, new CommandPath.Builder("a").build()).build();
        final Registration regB = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();

        final Map<CommandPath, Registration> map = new HashMap<>();
        map.put(regA.getPath(), regA);
        map.put(regB.getPath(), regB);

        final TokenizedUserInput inputA = new TokenizedUserInput.Builder("a").build();
        final TokenizedUserInput inputB = new TokenizedUserInput.Builder("a b").build();
        final TokenizedUserInput inputC = new TokenizedUserInput.Builder("a b c").build();
        final TokenizedUserInput inputD = new TokenizedUserInput.Builder("a b -i 1").build();

        final RegistrationLookup lookupA = new RegistrationLookup.Builder(inputA).build();
        final RegistrationLookup lookupB = new RegistrationLookup.Builder(inputB).build();
        final RegistrationLookup lookupC = new RegistrationLookup.Builder(inputC).build();
        final RegistrationLookup lookupD = new RegistrationLookup.Builder(inputD).build();

        final RegistrationResponse responseA = new RegistrationResponse.Builder(lookupA, map).build();
        final RegistrationResponse responseB = new RegistrationResponse.Builder(lookupB, map).build();
        final RegistrationResponse responseC = new RegistrationResponse.Builder(lookupC, map).build();
        final RegistrationResponse responseD = new RegistrationResponse.Builder(lookupD, map).build();

        assertEquals(2, responseA.getRegistrations().size());
        assertTrue(responseA.getRegistrations().contains(regA));
        assertTrue(responseA.getRegistrations().contains(regB));

        assertEquals(1, responseB.getRegistrations().size());
        assertTrue(responseB.getRegistrations().contains(regB));

        assertEquals(1, responseC.getRegistrations().size());
        assertTrue(responseC.getRegistrations().contains(regB));

        assertEquals(1, responseD.getRegistrations().size());
        assertTrue(responseD.getRegistrations().contains(regB));
    }
}
