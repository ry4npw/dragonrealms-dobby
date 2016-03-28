package pw.ry4n.dr.engine.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import pw.ry4n.dr.engine.core.State;
import pw.ry4n.dr.engine.interpreter.StormFrontInterpreter;
import pw.ry4n.dr.engine.model.MatchToken;
import pw.ry4n.dr.engine.model.ProgramImpl;
import pw.ry4n.dr.engine.model.StormFrontCommands;
import pw.ry4n.dr.engine.model.StormFrontLine;
import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.CommandQueue;
import pw.ry4n.dr.proxy.InterceptingProxy;

public class StormFrontInterpreterTest {
	@Test
	public void testFormatArgument() {
		ProgramImpl program = new ProgramImpl();
		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		assertEquals("dust bunn", interpreter.formatArgument("dust_bunn"));
		assertEquals("dust bunn", interpreter.formatArgument("\"dust_bunn\""));
	}

	@Test
	public void testMatchre() {
		ProgramImpl program = new ProgramImpl();
		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		String metals = "(animite|coal|copper|covellite|damite|darkstone|electrum|glaes|gold|haralun|iron|kertig|lead|lumium|muracite|nickel|niniam|oravir|platinum|silver|tin|zinc)";
		interpreter.matchre(new StormFrontLine(StormFrontCommands.MATCHRE, new String[]{"tear", metals + "\\stear"}));
		interpreter.matchre(new StormFrontLine(StormFrontCommands.MATCHRE, new String[]{"fragment", metals + "\\sfragment"}));
		interpreter.matchre(new StormFrontLine(StormFrontCommands.MATCHRE, new String[]{"lump", metals + "\\slump"}));
		interpreter.matchre(new StormFrontLine(StormFrontCommands.MATCHRE, new String[]{"shard", metals + "\\sshard"}));
		interpreter.matchre(new StormFrontLine(StormFrontCommands.MATCHRE, new String[]{"nugget", metals + "\\snugget"}));
		interpreter.matchre(new StormFrontLine(StormFrontCommands.MATCHRE, new String[]{"no_nugget", "(You see|You look)"}));

		MatchToken token = interpreter.match("You take a moment to look for all the items in the area and see  that wolf spiders that are caught in a cage of swirling darkness, a quartzite pebble and a small silver nugget.");
		assertEquals("nugget", token.getLabel());
	}

	@Test
	public void testGetVariable0() {
		ProgramImpl program = new ProgramImpl();
		program.getVariables().put("1", "one");
		program.getVariables().put("2", "two");

		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		String output = interpreter.getVariable0();

		assertEquals("one two", output);

		
	}

	@Test
	public void testMove() {
		ProgramImpl program = new ProgramImpl();
		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		interpreter.nextroom();
		interpreter.notify("GSo");

		assertEquals(State.RUNNING, interpreter.state);
	}

	@Test
	public void testNextroom() throws IOException {
		ProgramImpl program = new ProgramImpl();
		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		interpreter.nextroom();

		assertTrue(interpreter.state == State.WAITING);
		assertEquals(MatchToken.STRING, interpreter.waitForMatchToken.getType());
	}

	@Test
	public void testSetVariable() {
		ProgramImpl program = new ProgramImpl();

		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		StormFrontLine line = new StormFrontLine();
		line.setArguments(new String[] {"name", "value"});

		interpreter.setVariable(line);
		assertEquals(1, program.getVariables().size());
		assertEquals("value", program.getVariables().get("name"));
	}

	@Test
	public void testDeleteVariable() {
		ProgramImpl program = new ProgramImpl();
		program.getVariables().put("name", "value");

		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		StormFrontLine line = new StormFrontLine();
		line.setArguments(new String[] {"name"});

		interpreter.deleteVariable(line);
		assertTrue(program.getVariables().isEmpty());
	}

	@Test
	public void testReplaceVariables() {
		ProgramImpl program = new ProgramImpl();
		program.getVariables().put("1", "one");
		program.getVariables().put("2", "two");

		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		// ECHO %1%-%2 (should return %1 and %2 hyphenated)
		StringBuilder sb = new StringBuilder();
		interpreter.replaceVariables(sb, "%1%-%2");
		assertEquals("one-two", sb.toString());
	}

	@Test
	public void testReplaceVariablesWithString() {
		ProgramImpl program = new ProgramImpl();
		program.getVariables().put("variable1", "one");
		program.getVariables().put("variable2", "two");

		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		String result = interpreter.replaceVariables("label-%variable2");
		assertEquals("label-two", result);

		result = interpreter.replaceVariables("label%variable1%%variable2");
		assertEquals("labelonetwo", result);
	}

	@Test
	public void testRunWithIf() throws IOException, InterruptedException {
		// mocks
		AbstractProxy sendToClient = mock(AbstractProxy.class);
		CommandQueue commandSender = mock(CommandQueue.class);
		InterceptingProxy sendToServer = mock(InterceptingProxy.class);

		when(sendToServer.getCommandSender()).thenReturn(commandSender);
		doAnswer(log("commandSender")).when(commandSender).enqueue(anyString());
		doAnswer(log("sendToClient")).when(sendToClient).send(anyString());

		// set up program
		ProgramImpl program = new ProgramImpl();
		program.setName("if_test");
		program.setType("sf");
		program.setSendToClient(sendToClient);
		program.setSendToServer(sendToServer);

		program.getVariables().put("1", "table");
		program.getLines().add(new StormFrontLine(StormFrontCommands.IF_, 1, StormFrontCommands.GOTO, new String[] { "look" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.EXIT, null));
		program.getLines().add(new StormFrontLine(StormFrontCommands.LABEL, new String[] { "look" }));
		program.getLabels().put("look", 2);
		program.getLines().add(new StormFrontLine(StormFrontCommands.ECHO, new String[] { "looking", "at", "%1" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.PUT, new String[] { "look", "in", "%1" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.PUT, new String[] { "look", "on", "%1" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.PUT, new String[] { "look", "under", "%1" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.PUT, new String[] { "look", "behind", "%1" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.EXIT, null));

		// run the script
		program.run();

		// verify mocks
		verify(commandSender).enqueue("look in table");
		verify(commandSender).enqueue("look on table");
		verify(commandSender).enqueue("look under table");
		verify(commandSender).enqueue("look behind table");
	}

	@Test
	public void testRunWithCounter() throws IOException {
		// mocks
		AbstractProxy sendToClient = mock(AbstractProxy.class);
		CommandQueue commandSender = mock(CommandQueue.class);
		InterceptingProxy sendToServer = mock(InterceptingProxy.class);

		when(sendToServer.getCommandSender()).thenReturn(commandSender);
		doAnswer(log("commandSender")).when(commandSender).enqueue(anyString());
		doAnswer(log("sendToClient")).when(sendToClient).send(anyString());

		// set up program
		ProgramImpl program = new ProgramImpl();
		program.setName("counterTest");
		program.setType("sf");
		program.setSendToClient(sendToClient);
		program.setSendToServer(sendToServer);

		program.getLines().add(new StormFrontLine(StormFrontCommands.COUNTER, 2, StormFrontCommands.SET, new String[] { "2" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.GOTO, new String[] { "my%c" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.LABEL, new String[] { "my1" }));
		program.getLabels().put("my1", 2);
		program.getLines().add(new StormFrontLine(StormFrontCommands.PUT, new String[] { "It", "was", "1" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.EXIT, null));
		program.getLines().add(new StormFrontLine(StormFrontCommands.LABEL, new String[] { "my2" }));
		program.getLabels().put("my2", 5);
		program.getLines().add(new StormFrontLine(StormFrontCommands.PUT, new String[] { "It", "was", "2" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.EXIT, null));
		program.getLines().add(new StormFrontLine(StormFrontCommands.LABEL, new String[] { "my3" }));
		program.getLabels().put("my3", 8);
		program.getLines().add(new StormFrontLine(StormFrontCommands.PUT, new String[] { "It", "was", "3" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.EXIT, null));

		// run the script
		program.run();

		// verify mocks
		verify(commandSender).enqueue("It was 2");
	}

	@Test
	public void testShift() {
		ProgramImpl program = new ProgramImpl();
		program.getVariables().put("1", "one");
		program.getVariables().put("2", "two");

		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		assertEquals(2, program.getVariables().size());
		assertEquals("one", program.getVariables().get("1"));

		interpreter.shiftVariables();

		assertEquals(1, program.getVariables().size());
		assertEquals("two", program.getVariables().get("1"));

		interpreter.shiftVariables();

		assertTrue(program.getVariables().isEmpty());
	}

	private Answer<Object> log(final String proxyName) {
		return new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				for (Object arg : args) {
					System.out.println(proxyName + "." + invocation.getMethod().getName() + "(" + arg + ")");
				}
				return null;
			}
		};
	}
}
