package pw.ry4n.dr.engine.sf.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	public FileParser(String fileName) throws FileNotFoundException, IOException {
		// expect all scripts to be in ~/Documents/dobby/
		String directory = System.getProperty("user.home") + System.getProperty("file.seperator") + "Documents"
				+ System.getProperty("file.seperator") + "dobby" + System.getProperty("file.seperator");
		String filePath = directory + fileName;

		Path directoryPath = Paths.get(directory);
		Path path = Paths.get(filePath);

		// if the directory does not exist, create it
		if (!Files.exists(directoryPath)) {
			// TODO send the following message downstream
			System.out.println("creating script directory: " + directoryPath.toAbsolutePath());
			Files.createDirectory(directoryPath);
		}

		readFileData(path);
	}

	void readFileData(Path path) throws IOException {
		BufferedReader reader = null;

		try {
			reader = Files.newBufferedReader(path, Charset.defaultCharset());

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
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public Program parse(Program program) {
		return parseFile(program, dataCharBuffer);
	}

	Program parseFile(Program program, DataCharBuffer dataCharBuffer) throws ParserException {
		LineParser lineParser = new LineParser(dataCharBuffer);

		while (lineParser.hasMoreChars()) {
			Line line = lineParser.parseLine();

			if (line != null && line.getCommand() != Commands.COMMENT) {
				// add the line to the program
				program.getLines().add(line);

				switch (line.getCommand()) {
				case Commands.COUNTER:
					try {
						// test for an integer
						line.setN(Integer.parseInt(line.getArguments()[0]));
					} catch (NumberFormatException e) {
						throw new ParserException("COUNTER value must be an integer.");
					}
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
