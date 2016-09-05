package pw.ry4n.dr.proxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Test;

public class CommandQueueTest {
	@Test
	public void testParseRoundtime() {
		CommandQueue commandQueue = new CommandQueue();
		assertEquals(10, commandQueue.parseRoundtime("...wait 10 seconds."));
		assertEquals(3, commandQueue.parseRoundtime("Roundtime: 3 sec."));
		assertEquals(6, commandQueue.parseRoundtime("[Roundtime 6 sec.]"));
		assertEquals(4, commandQueue.parseRoundtime("[Praying for 4 sec.]"));
	}

	@Test
	public void testBlocking() throws IOException {
		AbstractProxy sendToClient = mock(AbstractProxy.class);
		CommandQueue commandQueue = new CommandQueue();
		commandQueue.sendProxy = sendToClient;

		assertEquals(CommandQueue.QueueState.CLEAR, commandQueue.state);
		commandQueue.enqueue("test");
		commandQueue.processSendQueue();
		assertEquals(CommandQueue.QueueState.BLOCKING, commandQueue.state);
		commandQueue.notify("GSq" + System.currentTimeMillis());
		assertEquals(CommandQueue.QueueState.CLEAR, commandQueue.state);
	}
}
