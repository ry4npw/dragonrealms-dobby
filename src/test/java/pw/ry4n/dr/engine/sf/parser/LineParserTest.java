package pw.ry4n.dr.engine.sf.parser;

import static org.junit.Assert.*;

import org.junit.Test;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Line;
import pw.ry4n.dr.engine.sf.parser.LineParser;

public class LineParserTest {
	@Test
	public void testParseMatchLineWithArguments() {
		Line line = parseStringToLine("match RT ... wait\n");

		// assert command
		assertEquals(Commands.MATCH, line.getCommand());

		// assert label and match string were both parsed
		assertEquals(2, line.getArguments().length);

		// assert label
		assertEquals("rt", line.getArguments()[0]);

		// assert match string
		assertEquals("... wait", line.getArguments()[1]);
	}

	public void testParseAgumentsWithQuotes() {
		Line line = parseStringToLine("save \"a string with spaces\"");
		assertNotNull(line.getArguments());
		assertEquals(1, line.getArguments().length);
		assertEquals("\"a_string_with_spaces\"", line.getArguments()[0]);
	}

	@Test
	public void testParseNoArguments() {
		LineParser lineParser = createLineParserWithString("\n");
		Line line = new Line();
		lineParser.parseArguments(line);
		assertNull(line.getArguments());
	}

	@Test
	public void testLineCounterIncrementsOnEmptyLine() {
		LineParser lineParser = createLineParserWithString("\n\n\n");
		assertEquals(1, lineParser.lineCounter);
		lineParser.parseLine();
		assertEquals(4, lineParser.lineCounter);
	}

	@Test
	public void testHasMoreCharsReturnsFalse() {
		LineParser lineParser = createLineParserWithString("");
		assertFalse(lineParser.hasMoreChars());
	}

	@Test
	public void testHasMoreCharsReturnsTrue() {
		LineParser lineParser = createLineParserWithString("\n");
		assertTrue(lineParser.hasMoreChars());
	}

	@Test
	public void testParseCommentLine() {
		Line line = parseStringToLine("# this is a comment\n");
		assertNotNull(line);
		assertEquals(Commands.COMMENT, line.getCommand());
		assertNull(line.getArguments());
	}

	@Test
	public void testSkipInLineComment() {
		Line line = parseStringToLine("put n #go north");
		assertEquals(Commands.PUT, line.getCommand());
		assertEquals("n", line.getArguments()[0]);
	}

	@Test
	public void testParseLabel() {
		Line line = parseStringToLine("LOOP:\n");

		// verify label was parsed
		assertNotNull(line);
		assertEquals(Commands.LABEL, line.getCommand());

		// labels should be converted to lowercase
		assertEquals("loop", line.getArguments()[0]);
	}

	@Test
	public void testParseLineWithIf() {
		Line line = parseStringToLine("IF_1 GOTO doit\nEXIT\ndoit:\nPUT attack %1");
		assertEquals(Commands.IF_, line.getCommand());
		assertEquals(Commands.GOTO, line.getSubCommand());
		assertEquals("doit", line.getArguments()[0]);
	}

	@Test
	public void testParseLineWithCounter() {
		Line line = parseStringToLine("counter set 10");
		assertEquals(Commands.COUNTER, line.getCommand());
		assertEquals(Commands.SET, line.getSubCommand());
		assertEquals("10", line.getArguments()[0]);
	}

	private LineParser createLineParserWithString(String string) {
		DataCharBuffer dataCharBuffer = new DataCharBuffer(string.toCharArray());
		return new LineParser(dataCharBuffer);
	}

	private Line parseStringToLine(String string) {
		LineParser lineParser = createLineParserWithString(string);
		Line line = lineParser.parseLine();
		return line;
	}
}
