package pw.ry4n.dr.engine.sf;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

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
	private BlockingQueue<String> clientInput; // read-only user input
	private BlockingQueue<String> serverResponse; // read-only server responses
	private AbstractProxy sendToServer; // proxy to send commands to server
	private AbstractProxy sendToClient; // proxy to send debug to client

	private List<MatchToken> matchList;
	private Program program;

	private int currentLineNumber = 0;

	public StormFrontInterpreter(BlockingQueue<String> clientInput, BlockingQueue<String> serverResponse,
			AbstractProxy sendToServer, AbstractProxy sendToClient, Program program) {
		if (program == null) {
			throw new IllegalArgumentException("Program must not be null!");
		}

		this.clientInput = clientInput;
		this.serverResponse = serverResponse;
		this.sendToServer = sendToServer;
		this.sendToClient = sendToClient;
		this.program = program;
	}

	public void run() {
		boolean run = true;
		currentLineNumber = program.getStart();

		try {
			sendToClient.send("dobby [BeginScript: " + program.getName() + "]");

			while (run && currentLineNumber < program.getLines().size()) {
				run = executeLine(program.getLines().get(currentLineNumber));
			}

			sendToClient.send("dobby [EndScript: " + program.getName() + "]");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean executeLine(Line currentLine) throws IOException {
		boolean run = true;
		currentLineNumber++;

		switch (currentLine.getCommand()) {
		case Commands.ECHO:
			sendToClient.send("dobby [" + program.getName() + ": " + combineAndReplaceArguments(currentLine.getArguments()) + "]");
			break;
		case Commands.EXIT:
			run = false;
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
		}

		return run;
	}

	private String combineAndReplaceArguments(String[] arguments) {
		StringBuilder result = new StringBuilder();

		for (int index = 0; index < arguments.length; index++) {
			if (arguments[index].startsWith("%")) {
				result.append(program.getVariables().get(arguments[index].substring(1)));
			} else {
				result.append(replaceUnderscoreWithSpaces(arguments[index]));
			}

			if (index < arguments.length + 1) {
				result.append(' ');
			}
		}

		return result.toString();
	}

	private String replaceUnderscoreWithSpaces(String string) {
		if (string == null) {
			return null;
		}

		// arguments may start or end with quotation marks, remove them
		// with: argument.replaceAll("\"$|^\"", "");

		// arguments will contain '_' instead of spaces, replace them:
		// argument.replaceAll("_", " ");
		return string.replaceAll("\"$|^\"", "").replace('_', ' ');
	}
}
