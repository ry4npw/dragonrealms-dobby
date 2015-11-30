package pw.ry4n.dr.engine.sf.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pw.ry4n.dr.AbstractProxy;
import pw.ry4n.dr.engine.sf.StormFrontInterpreter;

public class Program implements Runnable {
	private String name;
	private String type;
	private List<Line> lines = new ArrayList<Line>();
	private Map<String, Integer> labels = new HashMap<String, Integer>();
	private Map<String, String> variables = new HashMap<String, String>();
	private int start = 0;

	private AbstractProxy sendToServer; // send commands to server
	private AbstractProxy sendToClient; // send output to client

	Thread runningThread;

	public Program() {
		// empty constructor
	}

	public Program(AbstractProxy sendToServer, AbstractProxy sendToClient) {
		this.sendToServer = sendToServer;
		this.sendToClient = sendToClient;
	}

	/**
	 * Execute the script.
	 */
	public void run() {
		switch (type) {
		case "sf":
			runningThread = new Thread(new StormFrontInterpreter(sendToServer, sendToClient, this));
			break;
		default:
			throw new RuntimeException("'" + type + "' is an unsupported script format. Valid formats are: sf");
		}
		runningThread.start();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Line> getLines() {
		return lines;
	}

	public void setLines(List<Line> lines) {
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

	public void setSendToServer(AbstractProxy sendToServer) {
		this.sendToServer = sendToServer;
	}

	public AbstractProxy getSendToClient() {
		return sendToClient;
	}

	public void setSendToClient(AbstractProxy sendToClient) {
		this.sendToClient = sendToClient;
	}

	public Thread getRunningThread() {
		return runningThread;
	}
}
