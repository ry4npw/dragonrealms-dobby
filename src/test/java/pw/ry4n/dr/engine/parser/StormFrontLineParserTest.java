package pw.ry4n.dr.engine.parser;

import static org.junit.Assert.*;

import org.junit.Test;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.core.ParserException;
import pw.ry4n.dr.engine.model.StormFrontCommands;
import pw.ry4n.dr.engine.model.StormFrontLine;
import pw.ry4n.dr.engine.parser.StormFrontLineParser;

public class StormFrontLineParserTest {
	@Test
	public void testParseMatchLineWithArguments() {
		StormFrontLine line = parseStringToLine("match RT ... wait\n");

		// assert command
		assertEquals(StormFrontCommands.MATCH, line.getCommand());

		// assert label and match string were both parsed
		assertEquals(2, line.getArguments().length);

		// assert label
		assertEquals("rt", line.getArguments()[0]);

		// assert match string
		assertEquals("... wait", line.getArguments()[1]);
	}

	@Test
	public void testParseAgumentsWithQuotes() {
		StormFrontLine line = parseStringToLine("save \"a string with spaces\"");
		assertNotNull(line.getArguments());
		assertEquals(1, line.getArguments().length);
		assertEquals("\"a_string_with_spaces\"", line.getArguments()[0]);
	}

	@Test
	public void testParseNoArguments() {
		StormFrontLineParser lineParser = createLineParserWithString("\n");
		StormFrontLine line = new StormFrontLine();
		lineParser.parseArguments(line);
		assertNull(line.getArguments());
	}

	@Test
	public void testLineCounterIncrementsOnEmptyLine() {
		StormFrontLineParser lineParser = createLineParserWithString("\n\n\n");
		assertEquals(1, lineParser.lineCounter);
		lineParser.parseLine();
		assertEquals(4, lineParser.lineCounter);
	}

	@Test
	public void testHasMoreCharsReturnsFalse() {
		StormFrontLineParser lineParser = createLineParserWithString("");
		assertFalse(lineParser.hasMoreChars());
	}

	@Test
	public void testHasMoreCharsReturnsTrue() {
		StormFrontLineParser lineParser = createLineParserWithString("\n");
		assertTrue(lineParser.hasMoreChars());
	}

	@Test
	public void testParseCommentLine() {
		StormFrontLine line = parseStringToLine("# this is a comment\n");
		assertNotNull(line);
		assertEquals(StormFrontCommands.COMMENT, line.getCommand());
		assertNull(line.getArguments());
	}

	@Test
	public void testSkipInLineComment() {
		StormFrontLine line = parseStringToLine("put n #go north");
		assertEquals(StormFrontCommands.PUT, line.getCommand());
		assertEquals("n", line.getArguments()[0]);
	}

	@Test
	public void testParseLabel() {
		StormFrontLine line = parseStringToLine("LOOP:\n");

		// verify label was parsed
		assertNotNull(line);
		assertEquals(StormFrontCommands.LABEL, line.getCommand());

		// labels should be converted to lowercase
		assertEquals("LOOP", line.getArguments()[0]);
	}

	@Test
	public void testParseLineWithIf() {
		StormFrontLine line = parseStringToLine("IF_1 GOTO doit\nEXIT\ndoit:\nPUT attack %1");
		assertEquals(StormFrontCommands.IF_, line.getCommand());
		assertEquals(StormFrontCommands.GOTO, line.getSubCommand());
		assertEquals("doit", line.getArguments()[0]);
	}

	@Test
	public void testParseLineWithCounter() {
		StormFrontLine line = parseStringToLine("counter set 10");
		assertEquals(StormFrontCommands.COUNTER, line.getCommand());
		assertEquals(StormFrontCommands.SET, line.getSubCommand());
		assertEquals("10", line.getArguments()[0]);
	}

	@Test(expected = ParserException.class)
	public void testThrowsParserException() {
		parseStringToLine("say Hello!");
	}

	private StormFrontLineParser createLineParserWithString(String string) {
		DataCharBuffer dataCharBuffer = new DataCharBuffer(string.toCharArray());
		return new StormFrontLineParser(dataCharBuffer);
	}

	private StormFrontLine parseStringToLine(String string) {
		StormFrontLineParser lineParser = createLineParserWithString(string);
		StormFrontLine line = lineParser.parseLine();
		return line;
	}
}
