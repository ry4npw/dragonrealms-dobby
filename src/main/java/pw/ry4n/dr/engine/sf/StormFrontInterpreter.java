package pw.ry4n.dr.engine.sf;

import java.io.IOException;
import java.util.List;

import pw.ry4n.dr.AbstractProxy;
import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Line;
import pw.ry4n.dr.engine.sf.model.MatchToken;
import pw.ry4n.dr.engine.sf.model.Program;

/**
 * A factory method that takes a StormFront (SF) script file as input and
 * returns an object representing the program.
 * 
 * @author Ryan Powell
 */
public class StormFrontInterpreter implements Runnable {
	// TODO monitor read-only user input (implement upstream MATCH)
	// TODO monitor read-only server responses (implement downstream MATCH)
	private AbstractProxy sendToServer; // proxy to send commands to server
	private AbstractProxy sendToClient; // proxy to send debug to client

	private List<MatchToken> matchList;
	private Program program;

	private int currentLineNumber = 0;

	private boolean scriptFinished = false;

	public StormFrontInterpreter(AbstractProxy sendToServer, AbstractProxy sendToClient, Program program) {
		if (program == null) {
			throw new IllegalArgumentException("Program must not be null!");
		}

		this.sendToServer = sendToServer;
		this.sendToClient = sendToClient;
		this.program = program;
	}

	public void run() {
		currentLineNumber = program.getStart();

		try {
			sendToClient.send("dobby [" + program.getName() + ": START]");

			long startTime = System.currentTimeMillis();

			while (!scriptFinished && currentLineNumber < program.getLines().size()) {
				executeLine(program.getLines().get(currentLineNumber));
			}

			scriptFinished = true;
			long endTime = System.currentTimeMillis();

			sendToClient.send("dobby [" + program.getName() + ": END, completed in " + (endTime - startTime) + "ms]");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void executeLine(Line currentLine) throws IOException {
		currentLineNumber++;

		switch (currentLine.getCommand()) {
		case Commands.ECHO:
			sendToClient.send("dobby [" + program.getName() + ": "
					+ combineAndReplaceArguments(currentLine.getArguments()) + "]");
			break;
		case Commands.EXIT:
			scriptFinished = true;
			break;
		case Commands.GOTO:
			String label = currentLine.getArguments()[0];
			currentLineNumber = program.getLabels().get(label);
			break;
		case Commands.IF_:
			if (currentLine.getN() >= program.getVariables().size()) {
				Line subLine = new Line(currentLine.getSubCommand(), currentLine.getArguments());
				executeLine(subLine);
			}
			break;
		case Commands.PUT:
			sendToServer.send(combineAndReplaceArguments(currentLine.getArguments()));
			break;
		default:
			// LABEL
			break;
		}
	}

	private String combineAndReplaceArguments(String[] arguments) {
		StringBuilder result = new StringBuilder();

		for (int index = 0; index < arguments.length; index++) {
			if (arguments[index].startsWith("%")) {
				result.append(program.getVariables().get(arguments[index].substring(1)));
			} else {
				result.append(formatArguments(arguments[index]));
			}

			if (index < arguments.length - 1) {
				result.append(' ');
			}
		}

		return result.toString();
	}

	/**
	 * Arguments may contain multiple words that are combined with quotations or
	 * underscores to be one argument. This method formats those by doing the
	 * following:
	 * 
	 * <ul>
	 * <li>remove quotation marks at the start or end of the string</li>
	 * <li>replace all underscores '_' with spaces ' '</li>
	 * </ul>
	 * 
	 * @param string
	 * @return
	 */
	private String formatArguments(String string) {
		if (string == null) {
			return null;
		}

		return string.replaceAll("\"$|^\"", "").replace('_', ' ');
	}

	public boolean isScriptFinished() {
		return scriptFinished;
	}
}
