package pw.ry4n.dr.engine.parser;

import static org.junit.Assert.*;

import org.junit.Test;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.model.Line;

public class LineParserTest {
	@Test
	public void testParseLineWithArguments() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("match ... wait\n".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		Line line = lineParser.parseLine();
		assertEquals(Commands.MATCH, line.getCommand());
		assertNotNull(line.getArguments());
		assertEquals(2, line.getArguments().length);
		assertEquals("...", line.getArguments()[0]);
		assertEquals("wait", line.getArguments()[1]);
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
	public void testHasMoreLinesReturnsFalse() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		assertFalse(lineParser.hasMoreLines());
	}

	@Test
	public void testHasMoreLinesReturnsTrue() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("\n".toCharArray());
		LineParser lineParser = new LineParser(dataCharBuffer);
		assertTrue(lineParser.hasMoreLines());
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
}
