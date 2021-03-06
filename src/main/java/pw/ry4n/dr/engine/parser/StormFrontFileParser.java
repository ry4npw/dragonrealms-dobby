package pw.ry4n.dr.engine.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.core.ParserException;
import pw.ry4n.dr.engine.model.ProgramImpl;
import pw.ry4n.dr.engine.model.StormFrontCommands;
import pw.ry4n.dr.engine.model.StormFrontLine;

/**
 * The purpose of this class is to take a File and parse it into a Program.
 * 
 * @author Ryan Powell
 */
public class StormFrontFileParser {
	DataCharBuffer dataCharBuffer = null;

	StormFrontFileParser() {
		// empty constructor
	}

	public StormFrontFileParser(String fileName) throws FileNotFoundException, IOException {
		// directory for all scripts: ~/Documents/dobby/
		StringBuilder directory = new StringBuilder();
		directory.append(System.getProperty("user.home")).append(System.getProperty("file.separator"));
		directory.append("Documents").append(System.getProperty("file.separator"));
		directory.append("dobby").append(System.getProperty("file.separator"));

		StringBuilder filePath = new StringBuilder();
		filePath.append(directory).append(fileName);

		// default script extension of ".sf"
		if (fileName.indexOf('.') < 0) {
			filePath.append(".sf");
		}

		Path directoryPath = Paths.get(directory.toString());
		Path path = Paths.get(filePath.toString());

		// if the directory does not exist, create it
		if (!Files.exists(directoryPath)) {
			// TODO send the following message downstream to client
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

	public ProgramImpl parse(ProgramImpl program) {
		return parseFile(program, dataCharBuffer);
	}

	ProgramImpl parseFile(ProgramImpl program, DataCharBuffer dataCharBuffer) throws ParserException {
		StormFrontLineParser lineParser = new StormFrontLineParser(dataCharBuffer);

		while (lineParser.hasMoreChars()) {
			StormFrontLine line = lineParser.parseLine();

			if (line != null && line.getCommand() != StormFrontCommands.COMMENT) {
				// add the line to the program
				program.getLines().add(line);

				switch (line.getCommand()) {
				case StormFrontCommands.COUNTER:
					if (line.getArguments() == null || line.getArguments()[0] == null) {
						throw new ParserException(lineParser.lineCounter + ": invalid COUNTER format. Usage: COUNTER <variable> <value>");
					}
					break;
				case StormFrontCommands.LABEL:
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
