package pw.ry4n.dr.engine.sf;

import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Line;
import pw.ry4n.dr.engine.sf.model.Program;
import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.InterceptingProxy;

public class StormFrontInterpreterTest {
	@Test
	public void testRun() throws IOException, InterruptedException {
		// mocks
		AbstractProxy sendToClient = mock(AbstractProxy.class);
		InterceptingProxy sendToServer = mock(InterceptingProxy.class);

		logProxySend(sendToServer, "sendToServer");
		logProxySend(sendToClient, "sendToClient");

		Program lookProgram = new Program();
		lookProgram.setName("look");
		lookProgram.setType("sf");
		lookProgram.setSendToClient(sendToClient);
		lookProgram.setSendToServer(sendToServer);
		lookProgram.getVariables().put("1", "table");

		// set up program
		lookProgram.getLines().add(new Line(Commands.IF_, 1, Commands.GOTO, new String[] { "look" }));
		lookProgram.getLines().add(new Line(Commands.EXIT, null));
		lookProgram.getLines().add(new Line(Commands.LABEL, new String[] { "look" }));
		lookProgram.getLines().add(new Line(Commands.ECHO, new String[] { "looking", "at", "%1" }));
		lookProgram.getLines().add(new Line(Commands.PUT, new String[] { "look", "in", "%1" }));
		lookProgram.getLines().add(new Line(Commands.PUT, new String[] { "look", "on", "%1" }));
		lookProgram.getLines().add(new Line(Commands.PUT, new String[] { "look", "under", "%1" }));
		lookProgram.getLines().add(new Line(Commands.PUT, new String[] { "look", "behind", "%1" }));
		lookProgram.getLines().add(new Line(Commands.EXIT, null));

		// add the label
		lookProgram.getLabels().put("look", 2);

		// run the script
		lookProgram.run();

		// verify mocks
		verify(sendToServer).enqueue("look in table");
		verify(sendToServer).enqueue("look on table");
		verify(sendToServer).enqueue("look under table");
		verify(sendToServer).enqueue("look behind table");
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
