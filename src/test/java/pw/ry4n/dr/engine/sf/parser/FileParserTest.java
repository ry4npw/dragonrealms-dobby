package pw.ry4n.dr.engine.sf.parser;

import static org.junit.Assert.*;

import org.junit.Test;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Program;

public class FileParserTest {
	@Test
	public void testParse() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("# a program!\nexit".toCharArray());
		FileParser fileParser = new FileParser();
		Program program = fileParser.parseFile(dataCharBuffer);

		// assert there were parsed lines
		assertNotNull(program.getLines());

		// assert comments were not added to the program
		assertEquals(1, program.getLines().size());

		// assert that our command was correctly parsed
		assertEquals(Commands.EXIT, program.getLines().get(0).getCommand());
	}

	@Test
	public void testParseWithLabel() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer(
				("# a better program!\n" + "LOOP:\n" + "MOVE N\n" + "GOTO LOOP\n").toCharArray());
		FileParser fileParser = new FileParser();
		Program program = fileParser.parseFile(dataCharBuffer);

		// assert that our LOOP label exists
		assertTrue(program.getLabels().containsKey("LOOP"));

		// and that it points to the right line
		assertEquals(Commands.LABEL, program.getLines().get(program.getLabels().get("LOOP")).getCommand());
		assertEquals("LOOP", program.getLines().get(program.getLabels().get("LOOP")).getArguments()[0]);
	}
}
