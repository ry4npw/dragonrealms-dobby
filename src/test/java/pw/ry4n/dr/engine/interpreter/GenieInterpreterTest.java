package pw.ry4n.dr.engine.interpreter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Test;

import pw.ry4n.dr.LoggingTest;
import pw.ry4n.dr.engine.core.State;
import pw.ry4n.dr.engine.model.ProgramImpl;
import pw.ry4n.dr.engine.model.StormFrontCommands;
import pw.ry4n.dr.engine.model.StormFrontLine;
import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.CommandQueue;
import pw.ry4n.dr.proxy.InterceptingProxy;

public class GenieInterpreterTest extends LoggingTest {
	@Test
	public void testReplaceVariablesWithMatch() {
		ProgramImpl program = new ProgramImpl();
		GenieInterpreter interpreter = new GenieInterpreter(program);

		String match = "([A-Z]{3,}\\s?[A-Z]+)";
		interpreter.matchre(new StormFrontLine(StormFrontCommands.MATCHRE, new String[]{"label", match}));

		interpreter.match("You get the impression that if you DANCE HAPPY, you'll have fulfilled your part and be left alone.");

		String result = interpreter.replaceVariables("$1");
		assertEquals("DANCE HAPPY", result);
	}

	@Test
	public void testReplaceVariablesWithMatchOnRun() throws IOException, InterruptedException {
		// mocks
		AbstractProxy sendToClient = mock(AbstractProxy.class);
		CommandQueue commandSender = mock(CommandQueue.class);
		InterceptingProxy sendToServer = mock(InterceptingProxy.class);

		when(sendToServer.getCommandSender()).thenReturn(commandSender);
		//doAnswer(log("commandSender")).when(commandSender).enqueue(anyString());
		//doAnswer(log("sendToClient")).when(sendToClient).send(anyString());

		// set up program
		ProgramImpl program = new ProgramImpl();
		program.setName("match_group_test");
		program.setType("ge");
		program.setSendToClient(sendToClient);
		program.setSendToServer(sendToServer);

		program.getLines().add(new StormFrontLine(StormFrontCommands.MATCHRE, new String[] { "doaction", "/([A-Z][ A-Z]*[A-Z]+)/" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.MATCHWAIT, null));
		program.getLabels().put("doaction", 2);
		program.getLines().add(new StormFrontLine(StormFrontCommands.PUT, new String[] { "$1" }));
		program.getLines().add(new StormFrontLine(StormFrontCommands.EXIT, null));

		// run the script
		Thread t = new Thread(program);
		program.setThread(t);
		t.start();

		// wait for program to be running
		while (program.getState() != State.MATCHING);

		program.getInterpreter().notify("You get the impression that if you DANCE HAPPY, you'll have fulfilled your part and be left alone.");

		verify(commandSender).enqueue("DANCE HAPPY");
	}
}
