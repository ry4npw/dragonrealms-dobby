package pw.ry4n.dr.engine.interpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import pw.ry4n.dr.engine.core.State;
import pw.ry4n.dr.engine.model.MatchToken;
import pw.ry4n.dr.engine.model.ProgramImpl;
import pw.ry4n.dr.engine.model.StormFrontCommands;
import pw.ry4n.dr.engine.model.StormFrontLine;
import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.CommandQueue;
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
	private CommandQueue commandSender; // send commands to server
	private AbstractProxy sendToServer; // listen to upstream commands from
										// client
	private AbstractProxy sendToClient; // listen to downstream responses from
										// server and send messages to client

	private Object monitorObject = new Object(); // thread synchronization

	private ProgramImpl program;
	private int counter = 0;
	private int currentLineNumber = 0;

	List<MatchToken> matchList = Collections.synchronizedList(new ArrayList<MatchToken>());
	long matchTimeout = 200;
	MatchToken waitForMatchToken = null;

	State state = State.INITIALIZING;
	private State lastState = null;

	StormFrontInterpreter(ProgramImpl program) {
		this.program = program;
	}

	public StormFrontInterpreter(InterceptingProxy sendToServer, AbstractProxy sendToClient, ProgramImpl program) {
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
			sendMessageToClient("START");

			long startTime = System.currentTimeMillis();

			while (!State.STOPPED.equals(state) && currentLineNumber < program.getLines().size()) {
				synchronized (monitorObject) {
					while (State.PAUSED.equals(state) || State.WAITING.equals(state)) {
						try {
							monitorObject.wait();
							waitForRoundtime();
						} catch (InterruptedException e) {
							// do nothing
						}
					}

					while (State.MATCHING.equals(state)) {
						try {
							if (matchTimeout > 0) {
								monitorObject.wait(matchTimeout);
							} else {
								monitorObject.wait();
							}
							// it is possible that the script was manually
							// paused while waiting, so do not just resume.
							if (!State.PAUSED.equals(state)) {
								resumeScript();
								matchList.clear();
							}
						} catch (InterruptedException e) {
							// do nothing
						}
					}
				}

				if (!State.STOPPED.equals(state)) {
					executeLine(program.getLines().get(currentLineNumber));
				}
			}

			exit();
			long endTime = System.currentTimeMillis();

			sendMessageToClient("END, completed in " + (endTime - startTime) + "ms");
		} catch (IOException e) {
			e.printStackTrace();
		}

		sendToServer.unsubscribe(this);
		sendToClient.unsubscribe(this);
	}

	void executeLine(StormFrontLine currentLine) throws IOException {
		currentLineNumber++;

		switch (currentLine.getCommand()) {
		case StormFrontCommands.COUNTER:
			counter(currentLine);
			break;
		case StormFrontCommands.DELETEVARIABLE:
			deleteVariable(currentLine);
			break;
		case StormFrontCommands.ECHO:
			echo(currentLine);
			break;
		case StormFrontCommands.EXIT:
			exit();
			break;
		case StormFrontCommands.GOTO:
			goTo(currentLine);
			break;
		case StormFrontCommands.IF_:
			if_(currentLine);
			break;
		case StormFrontCommands.MATCH:
			match(currentLine);
			break;
		case StormFrontCommands.MATCHRE:
			matchre(currentLine);
			break;
		case StormFrontCommands.MATCHWAIT:
			matchwait(currentLine);
			break;
		case StormFrontCommands.MOVE:
			put(currentLine);
			nextroom();
			break;
		case StormFrontCommands.NEXTROOM:
			nextroom();
			break;
		case StormFrontCommands.PAUSE:
			pause(currentLine);
			break;
		case StormFrontCommands.PUT:
			put(currentLine);
			break;
		case StormFrontCommands.SAVE:
			save(currentLine);
			break;
		case StormFrontCommands.SETVARIABLE:
			setVariable(currentLine);
			break;
		case StormFrontCommands.SHIFT:
			shiftVariables();
			break;
		case StormFrontCommands.WAIT:
			doWait();
			break;
		case StormFrontCommands.WAITRT:
			doWaitRt();
			break;
		case StormFrontCommands.WAITFOR:
			waitfor(currentLine);
			break;
		case StormFrontCommands.WAITFORRE:
			waitforre(currentLine);
			break;
		default:
			// assume LABEL
			break;
		}
	}

	void counter(StormFrontLine currentLine) {
		switch (currentLine.getSubCommand()) {
		case StormFrontCommands.ADD:
			counter += Integer.parseInt(replaceVariables(currentLine.getArguments()[0]));
			break;
		case StormFrontCommands.DIVIDE:
			counter /= Integer.parseInt(replaceVariables(currentLine.getArguments()[0]));
			break;
		case StormFrontCommands.MULTIPLY:
			counter *= Integer.parseInt(replaceVariables(currentLine.getArguments()[0]));
			break;
		case StormFrontCommands.SET:
			counter = Integer.parseInt(replaceVariables(currentLine.getArguments()[0]));
			break;
		case StormFrontCommands.SUBTRACT:
			counter -= Integer.parseInt(replaceVariables(currentLine.getArguments()[0]));
			break;
		default:
			break;
		}
	}

	void deleteVariable(StormFrontLine currentLine) {
		String key = currentLine.getArguments()[0];
		program.getVariables().remove(key);
	}

	void echo(StormFrontLine currentLine) throws IOException {
		sendMessageToClient("ECHO " + combineAndReplaceArguments(currentLine.getArguments()));
	}

	void goTo(StormFrontLine currentLine) {
		String label = replaceVariables(currentLine.getArguments()[0], false);
		goTo(label);
	}

	void goTo(String label) {
		if (label != null) {
			label = label.toLowerCase();
		}

		if (program.getLabels().containsKey(label)) {
			currentLineNumber = program.getLabels().get(label);
		} else {
			try {
				state = State.STOPPED;
				sendMessageToClient("ERROR! Label " + label + " does not exist.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void exit() {
		state = State.STOPPED;
	}

	void if_(StormFrontLine currentLine) throws IOException {
		if (program.getVariables().containsKey(String.valueOf(currentLine.getN()))) {
			StormFrontLine subLine = new StormFrontLine(currentLine.getSubCommand(), currentLine.getArguments());
			executeLine(subLine);
		}
	}

	void match(StormFrontLine currentLine) {
		matchList.add(new MatchToken(MatchToken.STRING, currentLine.getArguments()[0], replaceVariables(currentLine.getArguments()[1])));
	}

	void matchre(StormFrontLine currentLine) {
		matchList.add(new MatchToken(MatchToken.REGEX, currentLine.getArguments()[0], replaceVariables(currentLine.getArguments()[1])));
	}

	void matchwait(StormFrontLine currentLine) {
		state = State.MATCHING;
		matchTimeout = currentLine == null || currentLine.getArguments() == null ? 0
				: new Float(1000 * Float.valueOf(currentLine.getArguments()[0])).longValue();
	}

	void pause(StormFrontLine currentLine) {
		try {
			int duration = 1000;

			if (currentLine != null && currentLine.getArguments() != null && currentLine.getArguments().length > 0) {
				duration = new Float(1000 * Float.parseFloat(currentLine.getArguments()[0])).intValue();
			}

			long roundTimeLeft = commandSender.getRoundTimeOver() - System.currentTimeMillis();

			if (duration > roundTimeLeft) {
				Thread.sleep(duration);
			} else if (roundTimeLeft > 0) {
				Thread.sleep(roundTimeLeft);
			}

			// sometimes pause is triggered before RT is parsed always check for
			// more RT before continuing.
			waitForRoundtime();
		} catch (InterruptedException e) {
			// do not worry about interrupts
		}
	}

	void nextroom() {
		// wait for room change
		waitForMatchToken = new MatchToken(MatchToken.STRING, "GSo");
		state = State.WAITING;
	}

	void put(StormFrontLine currentLine) throws IOException {
		String sendLine = combineAndReplaceArguments(currentLine.getArguments());
		commandSender.enqueue(sendLine);
		sendMessageToClient(sendLine);
	}

	void sendMessageToClient(String message) throws IOException {
		sendToClient.send(program.getName() + ": " + message);
	}

	void save(StormFrontLine currentLine) {
		program.getVariables().put("s", combineAndReplaceArguments(currentLine.getArguments()));
	}

	void setVariable(StormFrontLine currentLine) {
		String key = currentLine.getArguments()[0];
		String value = combineAndReplaceArguments(
				Arrays.copyOfRange(currentLine.getArguments(), 1, currentLine.getArguments().length));
		program.getVariables().put(key, value);
	}

	void shiftVariables() {
		for (int i = 1; i < 10; i++) {
			String value = program.getVariables().remove(String.valueOf(i));

			if (value == null) {
				break;
			} else if (i > 1) {
				program.getVariables().put(String.valueOf(i - 1), value);
			}
		}
	}

	void doWait() {
		waitForMatchToken = null;
		state = State.WAITING;
	}

	void doWaitRt() {
		waitForMatchToken = null;
		state = State.WAITRT;
	}

	void waitfor(StormFrontLine currentLine) {
		waitForMatchToken = new MatchToken(MatchToken.STRING, currentLine.getArguments()[0]);
		state = State.WAITING;
	}

	void waitforre(StormFrontLine currentLine) {
		waitForMatchToken = new MatchToken(MatchToken.REGEX, currentLine.getArguments()[0]);
		state = State.WAITING;
	}

	String combineAndReplaceArguments(String[] arguments) {
		if (arguments == null) {
			return "";
		}

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

	String replaceVariables(String argument) {
		return replaceVariables(argument, true);
	}

	String replaceVariables(String argument, boolean convertUnderscoresToSpaces) {
		StringBuilder sb = new StringBuilder();
		replaceVariables(sb, argument);
		return sb.toString();
	}

	void replaceVariables(StringBuilder result, String argument) {
		replaceVariables(result, argument, true);
	}

	void replaceVariables(StringBuilder result, String argument, boolean convertUnderscoresToSpaces) {
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
							if (convertUnderscoresToSpaces) {
								result.append(formatArgument(value));
							} else {
								result.append(value);
							}
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

	String getVariable0() {
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
			monitorObject.notify();
		}
	}

	public void pauseScript() {
		synchronized (monitorObject) {
			if (!State.STOPPED.equals(state) && !State.PAUSED.equals(state)) {
				// remember lastState for pausing
				lastState = state;
				state = State.PAUSED;
				monitorObject.notify();

				try {
					sendMessageToClient("PAUSED");
				} catch (IOException e) {
					// ignore problems sending message to client
				}
			}
		}
	}

	public void resumeScript() {
		if (State.STOPPED.equals(state)) {
			return;
		}

		synchronized (monitorObject) {
			try {
				if (State.PAUSED.equals(state)) {
					// when paused, revert state to lastState
					state = lastState;
					sendMessageToClient("RESUMING");
				} else {
					state = State.RUNNING;
				}
				monitorObject.notify();
			} catch (IOException e) {
				// ignore problems sending message to client
			}
		}
	}

	void waitForRoundtime() throws InterruptedException {
		long roundTimeLeft = commandSender.getRoundTimeOver() - System.currentTimeMillis();
		if (roundTimeLeft > 0) {
			Thread.sleep(roundTimeLeft);
		}
	}

	@Override
	public void notify(String line) {
		synchronized (monitorObject) {
			if ((line.startsWith("GSq") || line.startsWith("GSQ")) && State.WAITRT.equals(state)) {
				// WAITRT should end
				resumeScript();
				return;
			} else if (line.startsWith("GS") && !line.startsWith("GSo")) {
				// ignore SIMU-PROTOCOL, except for GSo which we use for nextroom()
				return;
			}

			switch (state) {
			case MATCHING:
				MatchToken token = match(line);
				if (token != null) {
					goTo(replaceVariables(token.getLabel()));
					monitorObject.notify();
				}
				break;
			case WAITING:
				if (waitForMatchToken != null) {
					if (waitForMatchToken.match(line)) {
						waitForMatchToken = null;
						resumeScript();
					}
				} else {
					resumeScript();
				}
				break;
			default:
				break;
			}
		}
	}

	MatchToken match(String line) {
		for (MatchToken token : matchList) {
			if (token.match(line)) {
				state = State.RUNNING;
				return token;
			}
		}

		return null;
	}
}
