package pw.ry4n.dr.engine.model;

import static org.junit.Assert.*;

import org.junit.Test;

import pw.ry4n.dr.engine.model.MatchToken;

public class MatchTokenTest {
	@Test
	public void testMatch() {
		MatchToken token = new MatchToken(MatchToken.STRING, "mind lock");
		assertTrue(token.match(" Outdoorsmanship:     18 41.93% mind lock     (34/34)"));
	}

	@Test
	public void testMatchre() {
		MatchToken token = new MatchToken(MatchToken.REGEX, "(nugget|tear)");
		assertTrue(token.match(
				"When the dust clears, both a soapstone pebble and a huge platinum nugget are visible on the ground."));
		assertFalse(
				token.match("You take a moment to look for all the items in the area and see a small soapstone rock."));
	}

	@Test
	public void testMatchreIgnoreCase() {
		MatchToken token = new MatchToken(MatchToken.REGEX, "/out of reach|remove|What were you|can't seem|Wield what\\?/i");
		assertTrue(token.match("Wield what?"));
	}

	@Test
	public void testGetGroup() {
		MatchToken token = new MatchToken(MatchToken.REGEX, "(nugget|tear)");
		assertTrue(token.match(
				"When the dust clears, both a soapstone pebble and a huge platinum nugget are visible on the ground."));
		assertEquals("nugget", token.getGroup());
	}
}
