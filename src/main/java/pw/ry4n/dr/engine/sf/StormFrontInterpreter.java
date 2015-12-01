package pw.ry4n.dr.engine.sf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

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
	private AbstractProxy sendToServer; // listen to upstream commands from
										// client
	private AbstractProxy sendToClient; // listen to downstream responses from
										// server and send messages to client

	List<MatchToken> matchList = Collections.synchronizedList(new ArrayList<MatchToken>());
	long matchTimeout = 200;
	private Object monitorObject = new Object(); // thread synchronization
	boolean isMatching = false;
	boolean isWaiting = false;
	private Program program;

	private int counter = 0;
	private int currentLineNumber = 0;

	private boolean scriptFinished = false;

	StormFrontInterpreter(Program program) {
		this.program = program;
	}

	public StormFrontInterpreter(InterceptingProxy sendToServer, AbstractProxy sendToClient, Program program) {
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
		synchronized (monitorObject) {
			while (isMatching) {
				System.out.println("waiting for match...");
				try {
					if (matchTimeout > 0) {
						monitorObject.wait(matchTimeout);
						clearMatchList();
					} else {
						monitorObject.wait();
					}
				} catch (InterruptedException e) {
					// do nothing
				}
			}
			while (isWaiting) {
				try {
					monitorObject.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}

		currentLineNumber++;

		switch (currentLine.getCommand()) {
		case Commands.COUNTER:
			counter(currentLine);
			break;
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
		case Commands.MATCH:
			match(currentLine);
			break;
		case Commands.MATCHRE:
			matchre(currentLine);
			break;
		case Commands.MATCHWAIT:
			matchwait(currentLine);
			break;
		case Commands.MOVE:
			put(currentLine);
			nextroom();
			break;
		case Commands.NEXTROOM:
			nextroom();
			break;
		case Commands.PAUSE:
			// TODO
			break;
		case Commands.PUT:
			put(currentLine);
			break;
		case Commands.SAVE:
			// TODO
			break;
		case Commands.SETVARIABLE:
			// TODO
			break;
		case Commands.SHIFT:
			// TODO
			break;
		case Commands.WAIT:
			doWait();
			break;
		case Commands.WAITFOR:
			match(currentLine);
			matchwait(null);
			break;
		case Commands.WAITFORRE:
			matchre(currentLine);
			matchwait(null);
			break;
		default:
			// assume LABEL
			break;
		}
	}

	private void clearMatchList() {
		matchList.clear();
		isMatching = false;
	}

	void counter(Line currentLine) {
		switch (currentLine.getSubCommand()) {
		case Commands.ADD:
			counter += currentLine.getN();
			break;
		case Commands.DIVIDE:
			counter /= currentLine.getN();
			break;
		case Commands.MULTIPLY:
			counter *= currentLine.getN();
			break;
		case Commands.SET:
			counter = currentLine.getN();
			break;
		case Commands.SUBTRACT:
			counter -= currentLine.getN();
			break;
		default:
			break;
		}
	}

	void echo(Line currentLine) throws IOException {
		sendToClient.send(program.getName() + ": ECHO " + combineAndReplaceArguments(currentLine.getArguments()));
	}

	void goTo(Line currentLine) {
		String label = combineAndReplaceArguments(currentLine.getArguments());
		currentLineNumber = program.getLabels().get(label);
	}

	void exit() {
		scriptFinished = true;
	}

	void if_(Line currentLine) throws IOException {
		if (currentLine.getN() >= program.getVariables().size()) {
			Line subLine = new Line(currentLine.getSubCommand(), currentLine.getArguments());
			executeLine(subLine);
		}
	}

	void match(Line currentLine) {
		matchList.clear();
		matchList.add(new MatchToken(MatchToken.STRING, currentLine.getArguments()[0]));
	}

	void matchre(Line currentLine) {
		isMatching = true;
		matchTimeout = 0;
		matchList.clear();
		matchList.add(new MatchToken(MatchToken.REGEX, currentLine.getArguments()[0]));
	}

	void matchwait(Line currentLine) {
		isMatching = true;
		matchTimeout = currentLine == null || currentLine.getArguments() == null ? 0
				: new Float(1000 * Float.valueOf(currentLine.getArguments()[0])).longValue();
	}

	void nextroom() {
		isMatching = true; // wait for match
		matchTimeout = 0; // do not timeout
		matchList.clear();
		matchList.add(new MatchToken(MatchToken.REGEX, "^GSo"));
	}

	void put(Line currentLine) throws IOException {
		String sendLine = combineAndReplaceArguments(currentLine.getArguments());
		commandSender.enqueue(sendLine);
		sendToClient.send(program.getName() + ": " + sendLine);
	}

	void doWait() {
		isWaiting = true;
	}

	private String combineAndReplaceArguments(String[] arguments) {
		StringBuilder result = new StringBuilder();

		// loop through arguments
		for (int index = 0; index < arguments.length; index++) {
			String argument = arguments[index];

			if (argument.contains("%")) {
				replaceVariables(result, argument);
			} else {
				result.append(formatArguments(argument));
			}

			// combine with spaces
			if (index < arguments.length - 1) {
				result.append(' ');
			}
		}

		return result.toString();
	}

	void replaceVariables(StringBuilder result, String argument) {
		Scanner s = new Scanner(argument);
		try {
			s.useDelimiter("%");
			if (!argument.startsWith("%")) {
				result.append(s.next());
			}
			boolean isVariable = false;
			while (s.hasNext()) {
				isVariable = !isVariable;
				if (isVariable) {
					String variable = s.next();
					if ("0".equals(variable)) {
						// 0 is a special case (all numeric variables)
						result.append(getVariable0());
					} else if ("c".equals(variable)) {
						// c is a special case (counter)
						result.append(counter);
					} else if ("s".equals(variable)) {
						// TODO s is a special case (save variable)
					} else {
						// other variables we look up in the program variable list
						String value = program.getVariables().get(variable);
						if (value != null) {
							result.append(value);
						}
					}
				} else {
					result.append(s.next());
				}
			}
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	private String getVariable0() {
		StringBuffer result = new StringBuffer();
		boolean first = true;

		for (int i = 1; i < 10; i++) {
			String value = program.getVariables().get(String.valueOf(i));
			if (value != null) {
				if (first) {
					first = false;
				} else {
					result.append(' ');
				}
				result.append(value);
			}
		}

		return result.toString();
	}

	/**
	 * Arguments may contain multiple words that are combined with quotations or
	 * underscores to be one argument. This method formats those by doing the
	 * following:
	 * 
	 * <ol>
	 * <li>remove quotation marks at the start or end of the string</li>
	 * <li>replace all underscores '_' with spaces ' '</li>
	 * </ol>
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
		synchronized (monitorObject) {
			if (isMatching) {
				if (match(line)) {
					monitorObject.notify();
					clearMatchList();
				} else if (isWaiting) {
					monitorObject.notify();
					isWaiting = false;
				}
			}
		}
	}

	private boolean match(String line) {
		for (MatchToken token : matchList) {
			if (token.match(line)) {
				return true;
			}
		}
		return false;
	}
}
