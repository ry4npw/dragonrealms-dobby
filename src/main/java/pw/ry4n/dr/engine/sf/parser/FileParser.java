package pw.ry4n.dr.engine.sf.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
		String directory = System.getProperty("user.home")+File.separator+"Documents"+File.separator+"dobby"+File.separator+"scripts"+File.separator;
		String path = directory + fileName + ".sf";
		FileReader reader = null;

		File directoryFile = new File(directory);

		// if the directory does not exist, create it
		if (!directoryFile.exists()) {
			// TODO send message downstream
			System.out.println("creating directory: " + directoryFile.getAbsolutePath());
			directoryFile.mkdir();
		}

		try {
			File file = new File(path);
			reader = new FileReader(file);

			char[] buf = new char[1024];
			int numRead = 0;
			StringBuilder fileData = new StringBuilder();

			while ((numRead = reader.read(buf)) != -1) {
				fileData.append(buf, 0, numRead);
			}

			// read file into dataCharBuffer
			dataCharBuffer = new DataCharBuffer(new char[fileData.length()]);
			fileData.getChars(0, fileData.length() - 1, dataCharBuffer.data, 0);

			reader.close();
		} catch (FileNotFoundException e) {
			// TODO catch FileNotFoundException and return it to client
			System.out.println("Cannot find file: " + path);
		} catch (IOException e) {
			System.out.println("Something went wrong reading the script.");
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public Program parse() {
		return parseFile(dataCharBuffer);
	}

	Program parseFile(DataCharBuffer dataCharBuffer) throws ParserException {
		LineParser lineParser = new LineParser(dataCharBuffer);

		Program program = new Program();

		while (lineParser.hasMoreChars()) {
			Line line = lineParser.parseLine();

			if (line != null && line.getCommand() != Commands.COMMENT) {
				// add the line to the program
				program.getLines().add(line);

				switch (line.getCommand()) {
				case Commands.COUNTER:
					// TODO verify argument is numeric
					break;
				case Commands.LABEL:
					// additional handling for labels
					String label = line.getArguments()[0].toLowerCase();

					if (label == "start") {
						// special "start" label
						program.setStart(program.getLines().size() - 1);
					}

					// add label to label map
					program.getLabels().put(label, program.getLines().size() - 1);
					break;
				}
			}
		}

		return program;
	}
}
