package pw.ry4n.dr.engine.interpreter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pw.ry4n.dr.engine.core.State;
import pw.ry4n.dr.engine.model.MatchToken;
import pw.ry4n.dr.engine.model.ProgramImpl;
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

	boolean matchre(String string, String regex) {
		return Pattern.compile(regex).matcher(string).find();
	}

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
