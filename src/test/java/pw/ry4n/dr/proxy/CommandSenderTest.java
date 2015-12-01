package pw.ry4n.dr.proxy;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommandSenderTest {
	@Test
	public void testParseRoundtime() {
		CommandSender commandSender = new CommandSender();
		assertEquals(10, commandSender.parseRoundtime("...wait 10 seconds."));
		assertEquals(3, commandSender.parseRoundtime("Roundtime: 3 sec."));
	}
}
