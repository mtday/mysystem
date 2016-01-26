package mysystem.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.cli.CommandLine;
import org.junit.Test;
import org.mockito.Mockito;

import mysystem.common.serialization.ManifestMapping;

import java.text.ParseException;
import java.util.Optional;

/**
 * Perform testing of the {@link Command} class and builder.
 */
public class CommandTest {
    private final ManifestMapping mapping = new ManifestMapping();

    @Test
    public void testCompareTo() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput uA = new TokenizedUserInput.Builder("a").build();
        final TokenizedUserInput uB = new TokenizedUserInput.Builder("b").build();
        final TokenizedUserInput uC = new TokenizedUserInput.Builder("a b c").build();

        final Registration rA =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build();
        final Registration rB =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("b", "c").build())
                        .build();
        final Registration rC =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b", "c").build())
                        .build();
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

        final Registration rA =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build();
        final Registration rB =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("b", "c").build())
                        .build();
        final Registration rC =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b", "c").build())
                        .build();
        final RegistrationResponse rrA = new RegistrationResponse.Builder(rA).setUserInput(uA).build();
        final RegistrationResponse rrB = new RegistrationResponse.Builder(rB).setUserInput(uB).build();
        final RegistrationResponse rrC = new RegistrationResponse.Builder(rC).setUserInput(uC).build();

        final Command a = new Command.Builder(rrA).build();
        final Command b = new Command.Builder(rrB).build();
        final Command c = new Command.Builder(rrC).build();

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(c, c);
    }

    @Test
    public void testHashCode() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput u = new TokenizedUserInput.Builder("a").build();
        final Registration r =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build();
        final RegistrationResponse rr = new RegistrationResponse.Builder(r).setUserInput(u).build();
        final Command command = new Command.Builder(rr).build();

        assertEquals(4066, command.hashCode());
    }

    @Test
    public void testToString() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput u = new TokenizedUserInput.Builder("a").build();
        final Registration r =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build();
        final RegistrationResponse rr = new RegistrationResponse.Builder(r).setUserInput(u).build();
        final Command command = new Command.Builder(rr).build();

        assertEquals(
                "Command[commandPath=a,registration=Registration[actorPath=path,path=a b,options=Optional"
                        + ".empty,description=Optional.empty],userInput=TokenizedUserInput[userInput=a,tokens=[a]]]",
                command.toString());
    }

    @Test
    public void testBuilderCopy() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput u = new TokenizedUserInput.Builder("a").build();
        final Registration r =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build();
        final RegistrationResponse rr = new RegistrationResponse.Builder(r).setUserInput(u).build();

        final Command a = new Command.Builder(rr).build();
        final Command b = new Command.Builder(a).build();

        assertEquals(a, b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderTooManyRegistrations() throws ParseException, org.apache.commons.cli.ParseException {
        final TokenizedUserInput u = new TokenizedUserInput.Builder("a").build();
        final Registration rA =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b").build())
                        .build();
        final Registration rB =
                new Registration.Builder().setActorPath("path").setPath(new CommandPath.Builder("a", "b", "c").build())
                        .build();
        final RegistrationResponse rr = new RegistrationResponse.Builder(rA, rB).setUserInput(u).build();
        new Command.Builder(rr).build();
    }

    @Test
    public void testBuilderWithOptions() throws ParseException, org.apache.commons.cli.ParseException {
        final Option option =
                new Option.Builder().setDescription("description").setShortOption("i").setArguments(1).build();
        final Options options = new Options.Builder(option).build();

        final TokenizedUserInput input = new TokenizedUserInput.Builder("a b -i 1").build();
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration reg =
                new Registration.Builder().setActorPath("path").setPath(commandPath).setOptions(options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(input).build();
        final Command command = new Command.Builder(response).build();

        assertTrue(command.getCommandLine().isPresent());

        final CommandLine commandLine = command.getCommandLine().get();
        assertEquals("1", commandLine.getOptionValue('i'));
    }

    @Test(expected = org.apache.commons.cli.ParseException.class)
    public void testBuilderWithOptionsAndInvalidInput() throws ParseException, org.apache.commons.cli.ParseException {
        final Option option =
                new Option.Builder().setDescription("description").setShortOption("i").setArguments(1).setRequired(true)
                        .build();
        final Options options = new Options.Builder(option).build();

        final TokenizedUserInput input = new TokenizedUserInput.Builder("a b -i").build();
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration reg =
                new Registration.Builder().setActorPath("path").setPath(commandPath).setOptions(options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(input).build();
        final Command command = new Command.Builder(response).build();
        command.validateCommandLine();
    }

    @Test
    public void testGetCommandLineParseException() throws ParseException, org.apache.commons.cli.ParseException {
        final Option option =
                new Option.Builder().setDescription("description").setShortOption("i").setArguments(1).build();
        final Options options = new Options.Builder(option).build();

        final TokenizedUserInput input = new TokenizedUserInput.Builder("a b --i").build();
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration reg =
                new Registration.Builder().setActorPath("path").setPath(commandPath).setOptions(options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(input).build();

        final Command command = Mockito.mock(Command.class);
        Mockito.when(command.getRegistration()).thenReturn(reg);
        Mockito.when(command.getUserInput()).thenReturn(input);
        Mockito.when(command.getCommandLine()).thenCallRealMethod();
        final Optional<CommandLine> commandLine = command.getCommandLine();
        assertFalse(commandLine.isPresent());
    }

    @Test
    public void testBuilderFromJson() throws ParseException, org.apache.commons.cli.ParseException {
        final Option option =
                new Option.Builder().setDescription("description").setShortOption("i").setArguments(1).build();
        final Options options = new Options.Builder(option).build();

        final TokenizedUserInput input = new TokenizedUserInput.Builder("a b -i 1").build();
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration reg =
                new Registration.Builder().setActorPath("path").setPath(commandPath).setOptions(options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(input).build();
        final Command original = new Command.Builder(response).build();
        final Command copy = new Command.Builder().fromJson(mapping, original.toJson()).build();

        assertEquals(original, copy);
    }

    @Test
    public void testToJson() throws ParseException, org.apache.commons.cli.ParseException {
        final Option option =
                new Option.Builder().setDescription("description").setShortOption("i").setArguments(1).build();
        final Options options = new Options.Builder(option).build();

        final TokenizedUserInput input = new TokenizedUserInput.Builder("a b -i 1").build();
        final CommandPath commandPath = new CommandPath.Builder("a", "b").build();
        final Registration reg =
                new Registration.Builder().setActorPath("path").setPath(commandPath).setOptions(options).build();
        final RegistrationResponse response = new RegistrationResponse.Builder(reg).setUserInput(input).build();
        final Command command = new Command.Builder(response).build();

        assertEquals(
                "{\"commandPath\":{\"path\":[\"a\",\"b\"],\"manifest\":\"CommandPath\"},"
                        + "\"registration\":{\"actorPath\":\"path\",\"path\":{\"path\":[\"a\",\"b\"],"
                        + "\"manifest\":\"CommandPath\"},\"options\":{\"options\":[{\"description\":\"description\","
                        + "\"shortOption\":\"i\",\"arguments\":1,\"required\":false,\"optionalArg\":false,"
                        + "\"manifest\":\"Option\"}],\"manifest\":\"Options\"},\"manifest\":\"Registration\"},"
                        + "\"userInput\":{\"userInput\":{\"input\":\"a b -i 1\",\"manifest\":\"UserInput\"},"
                        + "\"tokens\":[\"a\",\"b\",\"-i\",\"1\"],\"manifest\":\"TokenizedUserInput\"},"
                        + "\"manifest\":\"Command\"}",
                command.toJson().toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoCommandPath() throws ParseException, org.apache.commons.cli.ParseException {
        final String jsonStr = "{\"registration\":{\"actorPath\":\"path\",\"path\":{\"path\":[\"a\",\"b\"],"
                + "\"manifest\":\"CommandPath\"},\"options\":{\"options\":[{\"description\":\"description\","
                + "\"shortOption\":\"i\",\"arguments\":1,\"required\":false,\"optionalArg\":false,"
                + "\"manifest\":\"Option\"}],\"manifest\":\"Options\"},\"manifest\":\"Registration\"},"
                + "\"userInput\":{\"userInput\":{\"input\":\"a b -i 1\",\"manifest\":\"UserInput\"},"
                + "\"tokens\":[\"a\",\"b\",\"-i\",\"1\"],\"manifest\":\"TokenizedUserInput\"},"
                + "\"manifest\":\"Command\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Command.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoRegistration() throws ParseException, org.apache.commons.cli.ParseException {
        final String jsonStr = "{\"commandPath\":{\"path\":[\"a\",\"b\"],\"manifest\":\"CommandPath\"},"
                + "\"userInput\":{\"userInput\":{\"input\":\"a b -i 1\",\"manifest\":\"UserInput\"},"
                + "\"tokens\":[\"a\",\"b\",\"-i\",\"1\"],\"manifest\":\"TokenizedUserInput\"},"
                + "\"manifest\":\"Command\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Command.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoUserInput() throws ParseException, org.apache.commons.cli.ParseException {
        final String jsonStr = "{\"commandPath\":{\"path\":[\"a\",\"b\"],\"manifest\":\"CommandPath\"},"
                + "\"registration\":{\"actorPath\":\"path\",\"path\":{\"path\":[\"a\",\"b\"],"
                + "\"manifest\":\"CommandPath\"},\"options\":{\"options\":[{\"description\":\"description\","
                + "\"shortOption\":\"i\",\"arguments\":1,\"required\":false,\"optionalArg\":false,"
                + "\"manifest\":\"Option\"}],\"manifest\":\"Options\"},\"manifest\":\"Registration\"},"
                + "\"manifest\":\"Command\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Command.Builder().fromJson(mapping, json).build();
    }
}
