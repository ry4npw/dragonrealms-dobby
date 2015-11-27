package pw.ry4n.dr.engine.parser;

import org.junit.Assert;
import org.junit.Test;

import pw.ry4n.dr.engine.core.DataCharBuffer;

public class LineParserTest {
	@Test
	public void testParseArguments() {
		DataCharBuffer buffer = new DataCharBuffer(" \"a string with spaces\" ... wait\n".toCharArray());
		LineParser parser = new LineParser(buffer, new IndexBuffer(5, false));
		parser.parseArguments();
		Assert.assertFalse(parser.line.getArguments() == null);
		Assert.assertEquals(3, parser.line.getArguments().length);
		Assert.assertEquals("\"a_string_with_spaces\"", parser.line.getArguments()[0]);
		Assert.assertEquals("...", parser.line.getArguments()[1]);
		Assert.assertEquals("wait", parser.line.getArguments()[2]);
	}

	@Test
	public void testParseNoArguments() {
		DataCharBuffer buffer = new DataCharBuffer("\n".toCharArray());
		LineParser parser = new LineParser(buffer, new IndexBuffer(5, false));
		parser.parseArguments();
		Assert.assertNull(parser.line.getArguments());
	}
}
