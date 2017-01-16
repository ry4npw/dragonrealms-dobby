package pw.ry4n.dr.engine.interpreter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pw.ry4n.dr.engine.model.ProgramImpl;
import pw.ry4n.dr.engine.model.StormFrontCommands;
import pw.ry4n.dr.engine.model.StormFrontLine;

public class GenieInterpreterTest {
	@Test
	public void testReplaceVariablesWithMatch() {
		ProgramImpl program = new ProgramImpl();
		GenieInterpreter interpreter = new GenieInterpreter(program);

		String match = "([A-Z]{3,}\\s?[A-Z]+)";
		interpreter.matchre(new StormFrontLine(StormFrontCommands.MATCHRE, new String[]{"label", match}));

		interpreter.match("You get the impression that if you DANCE HAPPY, you'll have fulfilled your part and be left alone.");

		String result = interpreter.replaceVariables("$1");
		assertEquals("DANCE HAPPY", result);
	}

}
