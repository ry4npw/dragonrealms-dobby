package pw.ry4n.dr.engine.interpreter;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pw.ry4n.dr.engine.core.State;
import pw.ry4n.dr.engine.model.MatchToken;
import pw.ry4n.dr.engine.model.ProgramImpl;
import pw.ry4n.dr.engine.model.StormFrontCommands;
import pw.ry4n.dr.engine.model.StormFrontLine;
import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.InterceptingProxy;

public class GenieInterpreter extends StormFrontInterpreter {
	MatchToken lastMatch = null;

	GenieInterpreter(ProgramImpl program) {
		super(program);
	}

	public GenieInterpreter(InterceptingProxy sendToServer, AbstractProxy sendToClient, ProgramImpl program) {
		super(sendToServer, sendToClient, program);
	}

	@Override
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

	@Override
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

	@Override
	void echo(StormFrontLine currentLine) throws IOException {
		sendMessageToClient("ECHO " + combineAndReplaceArguments(currentLine.getArguments()));
	}

	@Override
	void goTo(StormFrontLine currentLine) {
		String label = replaceVariables(currentLine.getArguments()[0], false);
		goTo(label);
	}

	@Override
	void match(StormFrontLine currentLine) {
		matchList.add(new MatchToken(MatchToken.STRING, currentLine.getArguments()[0],
				replaceVariables(currentLine.getArguments()[1])));
	}

	@Override
	void matchre(StormFrontLine currentLine) {
		matchList.add(new MatchToken(MatchToken.REGEX, currentLine.getArguments()[0],
				replaceVariables(currentLine.getArguments()[1])));
	}

	@Override
	void save(StormFrontLine currentLine) {
		program.getVariables().put("s", combineAndReplaceArguments(currentLine.getArguments()));
	}

	@Override
	void setVariable(StormFrontLine currentLine) {
		String key = currentLine.getArguments()[0];
		String value = combineAndReplaceArguments(
				Arrays.copyOfRange(currentLine.getArguments(), 1, currentLine.getArguments().length));
		program.getVariables().put(key, value);
	}

	@Override
	String combineAndReplaceArguments(String[] arguments) {
		if (arguments == null) {
			return "";
		}

		StringBuilder result = new StringBuilder();

		// loop through arguments
		for (int index = 0; index < arguments.length; index++) {
			String argument = arguments[index];

			if (argument.contains("%") || argument.contains("$")) {
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

	@Override
	String replaceVariables(String argument) {
		return replaceVariables(argument, true);
	}

	@Override
	String replaceVariables(String argument, boolean convertUnderscoresToSpaces) {
		StringBuilder sb = new StringBuilder();
		replaceVariables(sb, argument);
		return sb.toString();
	}

	@Override
	void replaceVariables(StringBuilder result, String argument) {
		replaceVariables(result, argument, true);
	}

	@Override
	void replaceVariables(StringBuilder result, String argument, boolean convertUnderscoresToSpaces) {
		Matcher matcher = Pattern.compile("\\$(\\d+)").matcher(argument);
		if (matcher.find() && lastMatch != null) {
			// 1. find/replace regex matches
			String value = lastMatch.getGroup(Integer.parseInt(matcher.group(1))-1);
			argument = argument.replace("$"+matcher.group(1), value);

			// 2. TODO find/replace global variables
		}

		super.replaceVariables(result, argument, convertUnderscoresToSpaces);
	}

	@Override
	public void notify(String line) {
		synchronized (monitorObject) {
			if ((line.startsWith("GSq") || line.startsWith("GSQ")) && State.WAITRT.equals(state)) {
				// WAITRT should end
				resumeScript();
				return;
			} else if (line.startsWith("GS") && !line.startsWith("GSo")) {
				// ignore SIMU-PROTOCOL, except for GSo which we use for
				// nextroom()
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
						lastMatch = waitForMatchToken;
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

	@Override
	MatchToken match(String line) {
		for (MatchToken token : matchList) {
			if (token.match(line)) {
				state = State.RUNNING;
				lastMatch = token;
				return token;
			}
		}

		return null;
	}
}
