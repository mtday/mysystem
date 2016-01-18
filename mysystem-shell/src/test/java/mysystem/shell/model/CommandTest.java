package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.ActorRef;

import java.text.ParseException;

/**
 * Perform testing of the {@link Command} class and builder.
 */
public class CommandTest {
    @Test
    public void testCompareTo() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput uA = new TokenizedUserInput.Builder("a").build();
        final TokenizedUserInput uB = new TokenizedUserInput.Builder("b").build();
        final TokenizedUserInput uC = new TokenizedUserInput.Builder("a b c").build();

        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration rA = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final Registration rB = new Registration.Builder(ref, new CommandPath.Builder("b", "c").build()).build();
        final Registration rC = new Registration.Builder(ref, new CommandPath.Builder("a", "b", "c").build()).build();
        final RegistrationResponse rrA = new RegistrationResponse.Builder(rA).setUserInput(uA).build();
        final RegistrationResponse rrB = new RegistrationResponse.Builder(rB).setUserInput(uB).build();
        final RegistrationResponse rrC = new RegistrationResponse.Builder(rC).setUserInput(uC).build();

        final Command a = new Command.Builder(rrA).build();
        final Command b = new Command.Builder(rrB).build();
        final Command c = new Command.Builder(rrC).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(1, b.compareTo(c));
        assertEquals(1, c.compareTo(a));
        assertEquals(-1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput uA = new TokenizedUserInput.Builder("a").build();
        final TokenizedUserInput uB = new TokenizedUserInput.Builder("b").build();
        final TokenizedUserInput uC = new TokenizedUserInput.Builder("a b c").build();

        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration rA = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final Registration rB = new Registration.Builder(ref, new CommandPath.Builder("b", "c").build()).build();
        final Registration rC = new Registration.Builder(ref, new CommandPath.Builder("a", "b", "c").build()).build();
        final RegistrationResponse rrA = new RegistrationResponse.Builder(rA).setUserInput(uA).build();
        final RegistrationResponse rrB = new RegistrationResponse.Builder(rB).setUserInput(uB).build();
        final RegistrationResponse rrC = new RegistrationResponse.Builder(rC).setUserInput(uC).build();

        final Command a = new Command.Builder(rrA).build();
        final Command b = new Command.Builder(rrB).build();
        final Command c = new Command.Builder(rrC).build();

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
    public void testHashCode() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput u = new TokenizedUserInput.Builder("a").build();
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration r = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final RegistrationResponse rr = new RegistrationResponse.Builder(r).setUserInput(u).build();
        final Command command = new Command.Builder(rr).build();

        assertEquals(4066, command.hashCode());
    }

    @Test
    public void testToString() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput u = new TokenizedUserInput.Builder("a").build();
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration r = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final RegistrationResponse rr = new RegistrationResponse.Builder(r).setUserInput(u).build();
        final Command command = new Command.Builder(rr).build();

        assertEquals              (
                "Command[commandPath=a,registration=Registration[path=a b,description=Optional.empty],userInput=a]",
                command.toString());
    }

    @Test
    public void testBuilderCopy() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput u = new TokenizedUserInput.Builder("a").build();
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration r = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final RegistrationResponse rr = new RegistrationResponse.Builder(r).setUserInput(u).build();

        final Command a = new Command.Builder(rr).build();
        final Command b = new Command.Builder(a).build();

        assertEquals(a, b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderTooManyRegistrations() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput u = new TokenizedUserInput.Builder("a").build();
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration rA = new Registration.Builder(ref, new CommandPath.Builder("a", "b").build()).build();
        final Registration rB = new Registration.Builder(ref, new CommandPath.Builder("a", "b", "c").build()).build();
        final RegistrationResponse rr = new RegistrationResponse.Builder(rA, rB).setUserInput(u).build();
        new Command.Builder(rr).build();
    }

    @Test
    public void testBuilderWithOptions() throws ParseException, org.apache.commons.cli.ParseException {
        final Options options = new Options();
        options.addOption(new Option("i", true, "description"));

        final TokenizedUserInput input = new TokenizedUserInput.Builder("a b -i 1").build();
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration reg = new Registration.Builder(ref, commandPath, options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(input).build();
        final Command command = new Command.Builder(response).build();

        assertTrue(command.getCommandLine().isPresent());

        final CommandLine commandLine = command.getCommandLine().get();
        assertEquals("1", commandLine.getOptionValue('i'));
    }

    @Test(expected = org.apache.commons.cli.MissingArgumentException.class)
    public void testBuilderWithOptionsAndInvalidInput() throws ParseException, org.apache.commons.cli.ParseException {
        final Option option = new Option("i", true, "description");
        option.setRequired(true);
        final Options options = new Options();
        options.addOption(option);

        final TokenizedUserInput input = new TokenizedUserInput.Builder("a b -i").build();
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final ActorRef ref = Mockito.mock(ActorRef.class);
        final Registration reg = new Registration.Builder(ref, commandPath, options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(input).build();
        new Command.Builder(response).build();
    }
}
