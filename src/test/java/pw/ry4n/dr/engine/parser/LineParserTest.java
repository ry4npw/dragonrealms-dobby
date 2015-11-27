package pw.ry4n.dr.engine.parser;

import org.junit.Assert;
import org.junit.Test;

import pw.ry4n.dr.engine.core.DataCharBuffer;

public class LineParserTest {
	@Test
	public void testParseArguments() {
		DataCharBuffer buffer = new DataCharBuffer(" ... wait\n".toCharArray());
		LineParser parser = new LineParser(buffer, new IndexBuffer(5, false));
		parser.parseArguments();
		Assert.assertEquals(2, parser.line.getArguments().size());
		Assert.assertEquals("...", parser.line.getArguments().get(0));
		Assert.assertEquals("wait", parser.line.getArguments().get(1));
	}

	@Test
	public void testParseNoArguments() {
		DataCharBuffer buffer = new DataCharBuffer(" \n".toCharArray());
		LineParser parser = new LineParser(buffer, new IndexBuffer(5, false));
		parser.parseArguments();
		Assert.assertTrue(parser.line.getArguments().isEmpty());;
	}
}
