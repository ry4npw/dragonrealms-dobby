package pw.ry4n.dr.engine.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pw.ry4n.dr.engine.core.Program;
import pw.ry4n.dr.engine.core.State;
import pw.ry4n.dr.engine.interpreter.StormFrontInterpreter;
import pw.ry4n.dr.engine.parser.StormFrontFileParser;
import pw.ry4n.dr.engine.parser.StormFrontLineParser;
import pw.ry4n.dr.proxy.AbstractProxy;
import pw.ry4n.dr.proxy.InterceptingProxy;

public class ProgramImpl implements Program {
	private String name;
	private String type;
	private List<StormFrontLine> lines = new ArrayList<StormFrontLine>();
	private Map<String, Integer> labels = new HashMap<String, Integer>();
	private Map<String, String> variables = new HashMap<String, String>();
	private int start = 0;
	private Thread thread;

	private InterceptingProxy sendToServer; // listen to and send commands to
											// server
	private AbstractProxy sendToClient; // send output to client

	private StormFrontInterpreter interpreter = null;

	public ProgramImpl() {
		// empty constructor
	}

	public ProgramImpl(InterceptingProxy sendToServer, AbstractProxy sendToClient) {
		this.sendToServer = sendToServer;
		this.sendToClient = sendToClient;
	}

	public ProgramImpl(String fileName, InterceptingProxy clientToServer, AbstractProxy serverToClient) {
		try {
			int firstSpace = fileName.indexOf(' ');
			String scriptName = fileName.substring(0, firstSpace == -1 ? fileName.length() : firstSpace);
			setName(scriptName);
			int firstPeriod = scriptName.indexOf('.');
			setType(scriptName.substring(firstPeriod == -1 || scriptName.length() < firstPeriod ? scriptName.length() : firstPeriod + 1));

			if ("".equals(getType())) {
				// default scripts to .sf extension
				setType("sf");

				if (!getName().endsWith(getType())) {
					setName(getName()+ "." + getType());
				}
			}

			// TODO switch on type
			StormFrontFileParser fileParser = new StormFrontFileParser(scriptName);
			fileParser.parse(this);

			if (firstSpace > 0) {
				setVariables(parseArguments(fileName.substring(firstSpace)));
			}

			// now set up send
			this.sendToServer = clientToServer;
			this.sendToClient = serverToClient;
		} catch (Exception e) {
			if (serverToClient != null) {
				try {
					serverToClient.send("ERROR! " + e.getMessage());
				} catch (IOException e1) {
					System.out.println("ERROR! Unable to write to client stream");
				}
			}
			e.printStackTrace();
		}
	}

	/**
	 * Split arguments based on spaces.
	 * 
	 * TODO combine with {@link StormFrontLineParser#parseArguments(StormFrontLine)}
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

	/**
	 * Execute the script.
	 */
	public void run() {
		switch (type) {
		case "sf":
			interpreter = new StormFrontInterpreter(sendToServer, sendToClient, this);
			interpreter.run();
			break;
		default:
			throw new RuntimeException("'" + type + "' is an unsupported script format. Valid formats are: sf");
		}
	}

	public State getState() {
		if (interpreter != null) {
			return interpreter.getState();
		}

		return State.STOPPED;
	}

	public void pause() {
		if (interpreter != null) {
			interpreter.pauseScript();
		}
	}

	public void resume() {
		if (interpreter != null) {
			interpreter.resumeScript();
		}
	}

	public void stop() {
		if (interpreter != null) {
			interpreter.stopScript();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<StormFrontLine> getLines() {
		return lines;
	}

	public void setLines(List<StormFrontLine> lines) {
		this.lines = lines;
	}

	public Map<String, Integer> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, Integer> labels) {
		this.labels = labels;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public AbstractProxy getSendToServer() {
		return sendToServer;
	}

	public void setSendToServer(InterceptingProxy sendToServer) {
		this.sendToServer = sendToServer;
	}

	public AbstractProxy getSendToClient() {
		return sendToClient;
	}

	public void setSendToClient(AbstractProxy sendToClient) {
		this.sendToClient = sendToClient;
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}
}
