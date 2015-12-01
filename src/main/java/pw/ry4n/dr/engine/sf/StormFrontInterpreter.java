package pw.ry4n.dr.engine.sf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Line;
import pw.ry4n.dr.engine.sf.model.MatchToken;
import pw.ry4n.dr.engine.sf.model.Program;
import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.CommandSender;
import pw.ry4n.dr.proxy.InterceptingProxy;
import pw.ry4n.dr.proxy.StreamListener;

/**
 * A factory method that takes a StormFront (SF) script file as input and
 * returns an object representing the program.
 * 
 * @author Ryan Powell
 */
public class StormFrontInterpreter implements StreamListener, Runnable {
	private CommandSender commandSender; // send commands to server
	private AbstractProxy sendToServer; // listen to upstream commands from client
	private AbstractProxy sendToClient; // listen to downstream responses from server and send messages to client

	private List<MatchToken> matchList = Collections.synchronizedList(new ArrayList<MatchToken>());
	private boolean isMatching = false;
	private Program program;

	private int currentLineNumber = 0;

	private boolean scriptFinished = false;

	public StormFrontInterpreter(InterceptingProxy sendToServer, AbstractProxy sendToClient,
			Program program) {
		if (program == null) {
			throw new IllegalArgumentException("Program must not be null!");
		}

		this.sendToServer = sendToServer;
		this.commandSender = sendToServer.getCommandSender();
		this.sendToClient = sendToClient;
		this.program = program;
	}

	public void run() {
		sendToServer.subscribe(this);
		sendToClient.subscribe(this);

		currentLineNumber = program.getStart();

		try {
			sendToClient.send(program.getName() + ": START");

			long startTime = System.currentTimeMillis();

			while (!scriptFinished && currentLineNumber < program.getLines().size()) {
				executeLine(program.getLines().get(currentLineNumber));
			}

			exit();
			long endTime = System.currentTimeMillis();

			sendToClient.send(program.getName() + ": END, completed in " + (endTime - startTime) + "ms");
		} catch (IOException e) {
			e.printStackTrace();
		}

		sendToServer.unsubscribe(this);
		sendToClient.unsubscribe(this);
	}

	private void executeLine(Line currentLine) throws IOException {
		currentLineNumber++;

		switch (currentLine.getCommand()) {
		case Commands.ECHO:
			echo(currentLine);
			break;
		case Commands.EXIT:
			exit();
			break;
		case Commands.GOTO:
			goTo(currentLine);
			break;
		case Commands.IF_:
			if_(currentLine);
			break;
		case Commands.PUT:
			put(currentLine);
			break;
		default:
			// LABEL
			break;
		}
	}

	private void put(Line currentLine) throws IOException {
		String sendLine = combineAndReplaceArguments(currentLine.getArguments());
		commandSender.enqueue(sendLine);
		sendToClient.send(program.getName() + ": " + sendLine);
	}

	private void if_(Line currentLine) throws IOException {
		if (currentLine.getN() >= program.getVariables().size()) {
			Line subLine = new Line(currentLine.getSubCommand(), currentLine.getArguments());
			executeLine(subLine);
		}
	}

	private void goTo(Line currentLine) {
		String label = currentLine.getArguments()[0];
		currentLineNumber = program.getLabels().get(label);
	}

	private void exit() {
		scriptFinished = true;
	}

	private void echo(Line currentLine) throws IOException {
		sendToClient.send(program.getName() + ": ECHO " + combineAndReplaceArguments(currentLine.getArguments()));
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

	@Override
	public void notify(String line) {
		if (isMatching) {
			synchronized (matchList) {
				// TODO handle match against line
			}
		}
	}
}
