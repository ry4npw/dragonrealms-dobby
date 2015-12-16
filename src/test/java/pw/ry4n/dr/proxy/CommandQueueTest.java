package pw.ry4n.dr.proxy;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommandQueueTest {
	@Test
	public void testParseRoundtime() {
		CommandQueue commandSender = new CommandQueue();
		assertEquals(10, commandSender.parseRoundtime("...wait 10 seconds."));
		assertEquals(3, commandSender.parseRoundtime("Roundtime: 3 sec."));
	}
}
