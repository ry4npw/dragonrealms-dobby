package pw.ry4n.dr.engine.sf.parser;

import static org.junit.Assert.*;

import org.junit.Test;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Line;
import pw.ry4n.dr.engine.sf.parser.LineParser;

public class LineParserTest {
	@Test
	public void testParseLineWithArguments() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("match RT ... wait\n".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		Line line = lineParser.parseLine();
		assertEquals(Commands.MATCH, line.getCommand());
		assertNotNull(line.getArguments());
		assertEquals(3, line.getArguments().length);
		assertEquals("RT", line.getArguments()[0]);
		assertEquals("...", line.getArguments()[1]);
		assertEquals("wait", line.getArguments()[2]);
	}

	public void testParseAgumentsWithQuotes() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("save \"a string with spaces\"".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		Line line = lineParser.parseLine();
		assertNotNull(line.getArguments());
		assertEquals(1, line.getArguments().length);
		assertEquals("\"a_string_with_spaces\"", line.getArguments()[0]);
	}

	@Test
	public void testParseNoArguments() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("\n".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		Line line = new Line();
		lineParser.parseArguments(line);
		assertNull(line.getArguments());
	}

	@Test
	public void testLineCounterIncrementsOnEmptyLine() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("\n\n\n".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		assertEquals(1, lineParser.lineCounter);
		lineParser.parseLine();
		assertEquals(4, lineParser.lineCounter);
	}

	@Test
	public void testHasMoreCharsReturnsFalse() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		assertFalse(lineParser.hasMoreChars());
	}

	@Test
	public void testHasMoreCharsReturnsTrue() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("\n".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		assertTrue(lineParser.hasMoreChars());
	}

	@Test
	public void testParseLineWithComment() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("# this is a comment\n".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		Line line = lineParser.parseLine();
		assertNotNull(line);
		assertEquals(Commands.COMMENT, line.getCommand());
		assertNull(line.getArguments());
	}

	@Test
	public void testParseLabel() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("LOOP:\n".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		Line line = lineParser.parseLine();
		assertNotNull(line);
		assertEquals(Commands.LABEL, line.getCommand());
		assertEquals("LOOP", line.getArguments()[0]);
	}

	@Test
	public void testParseLineWithIf() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("IF_1 GOTO doit\nEXIT\ndoit:\nPUT attack %1".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		Line line = lineParser.parseLine();
		assertEquals(Commands.IF_, line.getCommand());
		assertEquals(Commands.GOTO, line.getSubCommand());
		assertEquals("doit", line.getArguments()[0]);
	}

	@Test
	public void testParseLineWithCounter() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("counter set 10".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		Line line = lineParser.parseLine();
		assertEquals(Commands.COUNTER, line.getCommand());
		assertEquals(Commands.SET, line.getSubCommand());
		assertEquals("10", line.getArguments()[0]);
	}
}
