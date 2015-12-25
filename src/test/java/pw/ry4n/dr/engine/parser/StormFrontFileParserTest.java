package pw.ry4n.dr.engine.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.model.ProgramImpl;
import pw.ry4n.dr.engine.model.StormFrontCommands;
import pw.ry4n.dr.engine.parser.StormFrontFileParser;

public class StormFrontFileParserTest {
	@Test
	public void testReadFileData() throws IOException, URISyntaxException {
		Path scriptPath = Paths.get(getClass().getResource("/look.sf").toURI());

		StormFrontFileParser fileParser = new StormFrontFileParser();
		fileParser.readFileData(scriptPath);

		assertNotNull(fileParser.dataCharBuffer);

		ProgramImpl program = new ProgramImpl();
		fileParser.parse(program);
		assertTrue(program.getLabels().containsKey("look"));
		assertEquals(8, program.getLines().size());
		assertEquals(StormFrontCommands.EXIT, program.getLines().get(7).getCommand());
	}

	@Test
	public void testParse() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer("move n # a program!\nexit".toCharArray());
		StormFrontFileParser fileParser = new StormFrontFileParser();
		ProgramImpl program = new ProgramImpl();
		fileParser.parseFile(program, dataCharBuffer);

		// assert there were parsed lines
		assertNotNull(program.getLines());

		// assert comments were not added to the program
		assertEquals(2, program.getLines().size());

		// assert that our command was correctly parsed
		assertEquals(StormFrontCommands.MOVE, program.getLines().get(0).getCommand());
		assertEquals("n", program.getLines().get(0).getArguments()[0]);
		assertEquals(StormFrontCommands.EXIT, program.getLines().get(1).getCommand());
	}

	@Test
	public void testParseWithLabel() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer(
				("# a better program!\n" + "LOOP:\n" + "MOVE N\n" + "GOTO LOOP\n").toCharArray());
		StormFrontFileParser fileParser = new StormFrontFileParser();
		ProgramImpl program = new ProgramImpl();
		fileParser.parseFile(program, dataCharBuffer);

		// assert that our LOOP label exists
		assertTrue(program.getLabels().containsKey("loop"));

		// and that it points to the right line
		assertEquals(StormFrontCommands.LABEL, program.getLines().get(program.getLabels().get("loop")).getCommand());
	}

	@Test
	public void testParseLabelStartingWithKeyword() {
		DataCharBuffer dataCharBuffer = new DataCharBuffer(("putpliers:\n" + "put stow pliers\n").toCharArray());
		StormFrontFileParser fileParser = new StormFrontFileParser();
		ProgramImpl program = new ProgramImpl();
		fileParser.parseFile(program, dataCharBuffer);

		// assert that our LOOP label exists
		assertTrue(program.getLabels().containsKey("putpliers"));
	}
}
