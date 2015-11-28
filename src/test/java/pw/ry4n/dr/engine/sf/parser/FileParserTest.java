package pw.ry4n.dr.engine.sf.parser;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Test;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Program;

public class FileParserTest {
	@Test
	public void testConstructor() throws FileNotFoundException {
		// TODO point "look" to a directory in the repository so unit tests are
		// not dependent on something in the Documents/ directory
		FileParser fileParser = new FileParser("look");
		assertTrue(fileParser.dataCharBuffer.data.length > 0);

		Program program = fileParser.parse();
		assertTrue(program.getLabels().containsKey("look"));
	}

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
		assertTrue(program.getLabels().containsKey("loop"));

		// and that it points to the right line
		assertEquals(Commands.LABEL, program.getLines().get(program.getLabels().get("loop")).getCommand());
		assertEquals("loop", program.getLines().get(program.getLabels().get("loop")).getArguments()[0]);
	}
}
