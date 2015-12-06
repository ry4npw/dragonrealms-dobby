package pw.ry4n.dr.engine.sf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Line;
import pw.ry4n.dr.engine.sf.model.MatchToken;
import pw.ry4n.dr.engine.sf.model.Program;
import pw.ry4n.dr.engine.sf.model.State;
import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.CommandSender;
import pw.ry4n.dr.proxy.InterceptingProxy;
import pw.ry4n.dr.proxy.StreamListener;

/**
 * A factory method that takes a StormFront (SF) script file as input and
 * returns an object representing the program.
 * 
 * @see <a href=https://www.play.net/playdotnet/play/stormfront_scripting.asp>
 *      StormFront Scripting Reference</a>
 * 
 * @author Ryan Powell
 */
public class StormFrontInterpreter implements StreamListener, Runnable {
	private CommandSender commandSender; // send commands to server
	private AbstractProxy sendToServer; // listen to upstream commands from
										// client
	private AbstractProxy sendToClient; // listen to downstream responses from
										// server and send messages to client
	
	private Object monitorObject = new Object(); // thread synchronization

	private Program program;
	private int counter = 0;
	private int currentLineNumber = 0;

	List<MatchToken> matchList = Collections.synchronizedList(new ArrayList<MatchToken>());
	long matchTimeout = 200;
	MatchToken waitForMatchToken = null;

	State state = State.INITIALIZING;
	private long roundTimeOver = 0;

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
		state = State.RUNNING;

		try {
			sendToClient.send(program.getName() + ": START");

			long startTime = System.currentTimeMillis();

			while (state != State.STOPPED && currentLineNumber < program.getLines().size()) {
				synchronized (monitorObject) {
					while (state == State.PAUSED || state == State.WAITING) {
						try {
							monitorObject.wait();
						} catch (InterruptedException e) {
							// do nothing
						}
					}

					while (state == State.MATCHING) {
						try {
							if (matchTimeout > 0) {
								monitorObject.wait(matchTimeout);
							} else {
								monitorObject.wait();
							}
							resumeScript();
							matchList.clear();
						} catch (InterruptedException e) {
							// do nothing
						}
					}
				}

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
			pause(currentLine);
			break;
		case Commands.PUT:
			put(currentLine);
			break;
		case Commands.SAVE:
			save(currentLine);
			break;
		case Commands.SETVARIABLE:
			setVariable(currentLine);
			break;
		case Commands.SHIFT:
			shiftVariables();
			break;
		case Commands.WAIT:
			doWait();
			break;
		case Commands.WAITFOR:
			waitfor(currentLine);
			break;
		case Commands.WAITFORRE:
			waitforre(currentLine);
			break;
		default:
			// assume LABEL
			break;
		}
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
		goTo(label);
	}

	void goTo(String label) {
		if (program.getLabels().containsKey(label)) {
			currentLineNumber = program.getLabels().get(label);
		} else {
			try {
				state = State.STOPPED;
				sendToClient.send(program.getName() + ": ERROR! Label" + label + " does not exist.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void exit() {
		state = State.STOPPED;
	}

	void if_(Line currentLine) throws IOException {
		if (currentLine.getN() >= program.getVariables().size()) {
			Line subLine = new Line(currentLine.getSubCommand(), currentLine.getArguments());
			executeLine(subLine);
		}
	}

	void match(Line currentLine) {
		synchronized (matchList) {
			matchList.add(
					new MatchToken(MatchToken.STRING, currentLine.getArguments()[0], currentLine.getArguments()[1]));
		}
	}

	void matchre(Line currentLine) {
		synchronized (matchList) {
			matchList.add(
					new MatchToken(MatchToken.REGEX, currentLine.getArguments()[0], currentLine.getArguments()[1]));
		}
	}

	void matchwait(Line currentLine) {
		state = State.MATCHING;
		matchTimeout = currentLine == null || currentLine.getArguments() == null ? 0
				: new Float(1000 * Float.valueOf(currentLine.getArguments()[0])).longValue();
	}

	void pause(Line currentLine) {
		int duration = 1000;
		if (currentLine != null && currentLine.getArguments() != null && currentLine.getArguments().length > 0) {
			duration = new Float(1000 * Float.parseFloat(currentLine.getArguments()[0])).intValue();
		}
		try {
			long roundTimeLeft = roundTimeOver - System.currentTimeMillis();
			if (duration > roundTimeLeft) {
				Thread.sleep(duration);
			} else if (roundTimeLeft > 0) {
				Thread.sleep(roundTimeLeft);
			}
		} catch (InterruptedException e) {
			// do not worry about interrupts
		}
	}

	void nextroom() {
		waitForMatchToken = new MatchToken(MatchToken.REGEX, "^GSo");
		state = State.WAITING;
	}

	void put(Line currentLine) throws IOException {
		String sendLine = combineAndReplaceArguments(currentLine.getArguments());
		commandSender.enqueue(sendLine);
		sendToClient.send(program.getName() + ": " + sendLine);
	}

	void save(Line currentLine) {
		program.getVariables().put("s", combineAndReplaceArguments(currentLine.getArguments()));
	}

	void setVariable(Line currentLine) {
		String key = currentLine.getArguments()[0];
		String value = combineAndReplaceArguments(
				Arrays.copyOfRange(currentLine.getArguments(), 1, currentLine.getArguments().length));
		program.getVariables().put(key, value);
	}

	void shiftVariables() {
		for (int i = 1; i < 10; i++) {
			String value = program.getVariables().get(i);
			if (value != null) {
				program.getVariables().remove(i);
				program.getVariables().put(String.valueOf(i - 1), value);
			}
		}
	}

	void doWait() {
		waitForMatchToken = null;
		state = State.WAITING;
	}

	void waitfor(Line currentLine) {
		waitForMatchToken = new MatchToken(MatchToken.STRING, currentLine.getArguments()[0]);
		state = State.WAITING;
	}

	void waitforre(Line currentLine) {
		waitForMatchToken = new MatchToken(MatchToken.REGEX, currentLine.getArguments()[0]);
		state = State.WAITING;
	}

	private String combineAndReplaceArguments(String[] arguments) {
		StringBuilder result = new StringBuilder();

		// loop through arguments
		for (int index = 0; index < arguments.length; index++) {
			String argument = arguments[index];

			if (argument.contains("%")) {
				replaceVariables(result, formatArgument(argument));
			} else {
				result.append(formatArgument(argument));
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
					} else {
						// other variables (including the save %s variable), we
						// look up in the program variable list
						String value = program.getVariables().get(variable);
						if (value != null) {
							result.append(formatArgument(value));
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
				result.append(formatArgument(value));
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
	String formatArgument(String string) {
		if (string == null) {
			return null;
		}

		return string.replaceAll("\"$|^\"", "").replace('_', ' ');
	}

	public State getState() {
		return state;
	}

	public void stopScript() {
		synchronized (monitorObject) {
			state = State.STOPPED;
		}
	}

	public void pauseScript() {
		synchronized (monitorObject) {
			state = State.PAUSED;
		}
	}

	public void resumeScript() {
		synchronized (monitorObject) {
			state = State.RUNNING;
			monitorObject.notify();
		}
	}

	@Override
	public void notify(String line) {
		if (line.startsWith("Roundtime: ")) {
			updateRoundtime(line);
		}

		synchronized (monitorObject) {
			switch (state) {
			case MATCHING:
				MatchToken token = match(line);
				if (token != null) {
					goTo(token.getLabel());
					monitorObject.notify();
				}
				break;
			case WAITING:
				if (waitForMatchToken != null) {
					if (waitForMatchToken.match(line)) {
						resumeScript();
						waitForMatchToken = null;
						monitorObject.notify();
					}
				} else {
					// wait for RT
					if (!inRoundtime()) {
						resumeScript();
						monitorObject.notify();
					}
				}
				break;
			default:
				break;
			}
		}
	}

	private synchronized MatchToken match(String line) {
		for (MatchToken token : matchList) {
			if (token.match(line)) {
				resumeScript();
				return token;
			}
		}
		return null;
	}

	private boolean inRoundtime() {
		return System.currentTimeMillis() < roundTimeOver;
	}

	void updateRoundtime(String line) {
		roundTimeOver = System.currentTimeMillis() + parseRoundtime(line) * 1000;
	}

	int parseRoundtime(String line) {
		Scanner s = new Scanner(line);
		try {
			while (s.hasNext() && !s.hasNextInt()) {
				s.next();
			}

			if (s.hasNextInt()) {
				return s.nextInt();
			}

			return 0;
		} finally {
			s.close();
		}
	}
}
