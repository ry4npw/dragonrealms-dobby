package pw.ry4n.dr.engine.sf.parser;

import java.io.FileNotFoundException;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.core.ParserException;
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

	FileParser() {
		// empty constructor
	}

	public FileParser(String fileName) throws FileNotFoundException {
		// expect all scripts to be in ~/Documents/dobby/scripts/

		// TODO turn fileName into a DataCharBuffer

		// TODO catch FileNotFoundException and return it to client
	}

	public Program parse() {
		return parseFile(dataCharBuffer);
	}

	Program parseFile(DataCharBuffer dataCharBuffer) {
		LineParser lineParser = new LineParser(dataCharBuffer);

		Program program = new Program();
		try {
			while (lineParser.hasMoreChars()) {
				Line line = lineParser.parseLine();
	
				if (line != null && line.getCommand() != Commands.COMMENT) {
					// add the line to the program
					program.getLines().add(line);
	
					switch (line.getCommand()) {
					case Commands.COMMENT:
						// TODO verify argument is numeric
						break;
					case Commands.LABEL:
						// additional handling for labels
						program.getLabels().put(line.getArguments()[0], program.getLines().size() - 1);
						break;
					}
				}
			}
		} catch (ParserException e) {
			// TODO return ParserException message to client
		}

		return program;
	}
}
