package pw.ry4n.dr.engine.sf.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class MatchTokenTest {
	@Test
	public void testMatch() {
		MatchToken token = new MatchToken(MatchToken.STRING, "mind lock");
		assertTrue(token.match(" Outdoorsmanship:     18 41.93% mind lock     (34/34)"));
	}

	@Test
	public void testMatchre() {
		MatchToken token = new MatchToken(MatchToken.REGEX, "(nugget|tear)");
		assertTrue(
				token.match("When the dust clears, both a soapstone pebble and a huge platinum nugget are visible on the ground."));
	}
}
