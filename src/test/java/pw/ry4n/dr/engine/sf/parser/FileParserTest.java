package pw.ry4n.dr.engine.sf.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.ProgramImpl;

public class FileParserTest {
	@Test
	public void testReadFileData() throws IOException, URISyntaxException {
		Path scriptPath = Paths.get(getClass().getResource("/look.sf").toURI());

		FileParser fileParser = new FileParser();
		fileParser.readFileData(scriptPath);

		assertNotNull(fileParser.dataCharBuffer);

		ProgramImpl program = new ProgramImpl();
		fileParser.parse(program);
		assertTrue(program.getLabels().containsKey("look"));
		assertEquals(8, program.getLines().size());
		assertEquals(Commands.EXIT, program.getLines().get(7).getCommand());
	}

	@Test
	public void testParse() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("move n # a program!\nexit".toCharArray());
		FileParser fileParser = new FileParser();
		ProgramImpl program = new ProgramImpl();
		fileParser.parseFile(program, dataCharBuffer);

		// assert there were parsed lines
		assertNotNull(program.getLines());

		// assert comments were not added to the program
		assertEquals(2, program.getLines().size());

		// assert that our command was correctly parsed
		assertEquals(Commands.MOVE, program.getLines().get(0).getCommand());
		assertEquals("n", program.getLines().get(0).getArguments()[0]);
		assertEquals(Commands.EXIT, program.getLines().get(1).getCommand());
	}

	@Test
	public void testParseWithLabel() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer(
				("# a better program!\n" + "LOOP:\n" + "MOVE N\n" + "GOTO LOOP\n").toCharArray());
		FileParser fileParser = new FileParser();
		ProgramImpl program = new ProgramImpl();
		fileParser.parseFile(program, dataCharBuffer);

		// assert that our LOOP label exists
		assertTrue(program.getLabels().containsKey("loop"));

		// and that it points to the right line
		assertEquals(Commands.LABEL, program.getLines().get(program.getLabels().get("loop")).getCommand());
		assertEquals("loop", program.getLines().get(program.getLabels().get("loop")).getArguments()[0]);
	}

	@Test
	public void testParseLabelStartingWithKeyword() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer(("putpliers:\n" + "put stow pliers\n").toCharArray());
		FileParser fileParser = new FileParser();
		ProgramImpl program = new ProgramImpl();
		fileParser.parseFile(program, dataCharBuffer);

		// assert that our LOOP label exists
		assertTrue(program.getLabels().containsKey("putpliers"));
	}
}
