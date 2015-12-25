package pw.ry4n.dr.engine.interpreter;

import java.util.regex.Pattern;

import pw.ry4n.dr.engine.model.ProgramImpl;
import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.InterceptingProxy;

public class GenieInterpreter extends StormFrontInterpreter {
	public GenieInterpreter(InterceptingProxy sendToServer, AbstractProxy sendToClient, ProgramImpl program) {
		super(sendToServer, sendToClient, program);
	}

	boolean matchre(String string, String regex) {
		return Pattern.compile(regex).matcher(string).find();
	}
}
