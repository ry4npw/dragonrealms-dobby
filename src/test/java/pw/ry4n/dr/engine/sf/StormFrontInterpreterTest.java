package pw.ry4n.dr.engine.sf;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

		logProxySend(sendToServer, "sendToServer");
		logProxySend(sendToClient, "sendToClient");

		Program program = new Program();
		program.setName("script");
		program.setType("sf");
		program.setSendToClient(sendToClient);
		program.setSendToServer(sendToServer);

		// set up program
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

		logProxySend(sendToServer, "sendToServer");
		logProxySend(sendToClient, "sendToClient");

		Program program = new Program();
		program.setName("script");
		program.setType("sf");
		program.setSendToClient(sendToClient);
		program.setSendToServer(sendToServer);

		// set up program
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

		verify(commandSender).enqueue("It was 2");
	}

	@Test
	public void testNextroom() throws IOException {
		Program program = new Program();
		StormFrontInterpreter interpreter = new StormFrontInterpreter(program);

		interpreter.nextroom();

		assertTrue(interpreter.isMatching);
		assertEquals(0, interpreter.matchTimeout);
		assertEquals(1, interpreter.matchList.size());
		assertEquals(MatchToken.REGEX, interpreter.matchList.get(0).getType());
	}

	private void logProxySend(AbstractProxy proxy, final String proxyName) throws IOException {
		// log calls to System.out
		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				for (Object arg : args) {
					System.out.println(proxyName + ".send(" + arg + ")");
				}
				return null;
			}
		}).when(proxy).send(anyString());
	}
}
