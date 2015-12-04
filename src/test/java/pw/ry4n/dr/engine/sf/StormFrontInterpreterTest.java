package pw.ry4n.dr.engine.sf;

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

import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Line;
import pw.ry4n.dr.engine.sf.model.MatchToken;
import pw.ry4n.dr.engine.sf.model.Program;
import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.CommandSender;
import pw.ry4n.dr.proxy.InterceptingProxy;

public class StormFrontInterpreterTest {
	@Test
	public void testReplaceVariables() {
		Program program = new Program();
		program.getVariables().put("1", "one");
		program.getVariables().put("2", "two");

		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		// ECHO %1%-%2 (should return %1 and %2 hyphenated)
		StringBuilder sb = new StringBuilder();
		interpreter.replaceVariables(sb, "%1%-%2");
		assertEquals("one-two", sb.toString());
	}

	@Test
	public void testRunWithIf() throws IOException, InterruptedException {
		// mocks
		AbstractProxy sendToClient = mock(AbstractProxy.class);
		CommandSender commandSender = mock(CommandSender.class);
		InterceptingProxy sendToServer = mock(InterceptingProxy.class);

		when(sendToServer.getCommandSender()).thenReturn(commandSender);
		doAnswer(log("commandSender")).when(commandSender).enqueue(anyString());
		doAnswer(log("sendToClient")).when(sendToClient).send(anyString());

		// set up program
		Program program = new Program();
		program.setName("if_test");
		program.setType("sf");
		program.setSendToClient(sendToClient);
		program.setSendToServer(sendToServer);

		program.getVariables().put("1", "table");
		program.getLines().add(new Line(Commands.IF_, 1, Commands.GOTO, new String[] { "look" }));
		program.getLines().add(new Line(Commands.EXIT, null));
		program.getLines().add(new Line(Commands.LABEL, new String[] { "look" }));
		program.getLabels().put("look", 2);
		program.getLines().add(new Line(Commands.ECHO, new String[] { "looking", "at", "%1" }));
		program.getLines().add(new Line(Commands.PUT, new String[] { "look", "in", "%1" }));
		program.getLines().add(new Line(Commands.PUT, new String[] { "look", "on", "%1" }));
		program.getLines().add(new Line(Commands.PUT, new String[] { "look", "under", "%1" }));
		program.getLines().add(new Line(Commands.PUT, new String[] { "look", "behind", "%1" }));
		program.getLines().add(new Line(Commands.EXIT, null));

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
		CommandSender commandSender = mock(CommandSender.class);
		InterceptingProxy sendToServer = mock(InterceptingProxy.class);

		when(sendToServer.getCommandSender()).thenReturn(commandSender);
		doAnswer(log("commandSender")).when(commandSender).enqueue(anyString());
		doAnswer(log("sendToClient")).when(sendToClient).send(anyString());

		// set up program
		Program program = new Program();
		program.setName("counterTest");
		program.setType("sf");
		program.setSendToClient(sendToClient);
		program.setSendToServer(sendToServer);

		program.getLines().add(new Line(Commands.COUNTER, 2, Commands.SET, new String[] { "2" }));
		program.getLines().add(new Line(Commands.GOTO, new String[] { "my%c" }));
		program.getLines().add(new Line(Commands.LABEL, new String[] { "my1" }));
		program.getLabels().put("my1", 2);
		program.getLines().add(new Line(Commands.PUT, new String[] { "It", "was", "1" }));
		program.getLines().add(new Line(Commands.EXIT, null));
		program.getLines().add(new Line(Commands.LABEL, new String[] { "my2" }));
		program.getLabels().put("my2", 5);
		program.getLines().add(new Line(Commands.PUT, new String[] { "It", "was", "2" }));
		program.getLines().add(new Line(Commands.EXIT, null));
		program.getLines().add(new Line(Commands.LABEL, new String[] { "my3" }));
		program.getLabels().put("my3", 8);
		program.getLines().add(new Line(Commands.PUT, new String[] { "It", "was", "3" }));
		program.getLines().add(new Line(Commands.EXIT, null));

		// run the script
		program.run();

		// verify mocks
		verify(commandSender).enqueue("It was 2");
	}

	@Test
	public void testNextroom() throws IOException {
		Program program = new Program();
		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		interpreter.nextroom();

		assertTrue(interpreter.isWaiting);
		assertEquals(MatchToken.REGEX, interpreter.waitForMatchToken.getType());
	}

	public void testFormatArgument() {
		Program program = new Program();
		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		assertEquals("dust bunn", interpreter.formatArgument("dust_bunn"));
		assertEquals("dust bunn", interpreter.formatArgument("\"dust_bunn\""));
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
