package pw.ry4n.dr.engine.sf.parser;

import java.io.FileNotFoundException;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Line;
import pw.ry4n.dr.engine.sf.model.Program;

/**
 * The purpose of this class is to take a File and parse it into a Program.
 * 
 * @author Ryan Powell
 */
public class FileParser {
	DataCharBuffer dataCharBuffer = null;
	LineParser lineParser = null;

	public FileParser(String fileName) throws FileNotFoundException {
		// TODO turn fileName into a DataCharBuffer
		// TODO catch FileNotFoundException and return it to client
		this.lineParser = new LineParser(dataCharBuffer);
	}

	public Program parse() {
		Program program = new Program();

		while (lineParser.hasMoreLines()) {
			Line line = lineParser.parseLine();

			// add the line to the program
			program.lines.add(line);

			// additional handling for the program
			switch (line.getCommand()) {
			case Commands.LABEL:
				program.labels.put(line.getArguments()[0], program.lines.size() - 1);
				break;
			}
		}

		// TODO catch ParserException and return it to client

		return program;
	}
}
