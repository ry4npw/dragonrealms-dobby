package pw.ry4n.dr;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import pw.ry4n.dr.engine.sf.model.Line;
import pw.ry4n.dr.engine.sf.model.Program;
import pw.ry4n.dr.engine.sf.parser.FileParser;
import pw.ry4n.dr.engine.sf.parser.LineParser;

public class InterceptingProxy extends AbstractProxy {
	public InterceptingProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	protected void filter(String line) throws IOException {
		// TODO do something with this for upstream MATCHing
		System.out.println(line);

		if (line.trim().startsWith(";")) {
			// capture commands that start with a semicolon
			String input = line.substring(1);

			// parse command and do something with it
			System.out.println("Captured input: " + input);
			try {
				int firstSpace = input.indexOf(' ');
				String scriptName = input.substring(0, firstSpace == -1 ? input.length() : firstSpace);
				FileParser fileParser = new FileParser(scriptName);
				Program program = fileParser.parse();
				program.setName(scriptName);

				if (firstSpace > 0) {
					program.setVariables(parseArguments(input.substring(firstSpace)));
				}

				program.setSendToServer(this);
				program.setSendToClient(companion);

				program.run();
			} catch (Exception e) {
				if (companion != null) {
					companion.send("dobby [ERROR! " + e.getMessage() + "]");
				}
				e.printStackTrace();
			}
		} else {
			// all other input should be passed along
			send(line);
		}
	}

	/**
	 * Split arguments based on spaces.
	 * 
	 * TODO combine with {@link LineParser#parseArguments(Line)}
	 * 
	 * @param arguments
	 */
	Map<String, String> parseArguments(String arguments) {
		if (arguments == null) {
			return null;
		}

		char[] argumentChars = arguments.toCharArray(); 

		int index = 0;

		if (argumentChars[index] == ' ') {
			index++;
		}

		int start = index;
		boolean inString = false;

		while (argumentChars.length > index) {
			if (argumentChars[index] == '"') {
				inString = !inString;
			}

			if (inString && argumentChars[index] == ' ') {
				argumentChars[index] = '_';
			}

			index++;
		}

		Map<String, String> variables = new HashMap<String, String>();
		if (start < index) {
			int counter = 0;
			for (String argument : new String(argumentChars, start, index - start).split(" ")) {
				counter++;
				variables.put(String.valueOf(counter), argument);
			}
		}

		return variables;
	}
}
