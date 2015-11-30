package pw.ry4n.dr.engine.sf.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import pw.ry4n.dr.AbstractProxy;
import pw.ry4n.dr.engine.sf.StormFrontInterpreter;

public class Program implements Runnable {
	private String name;
	private List<Line> lines = new ArrayList<Line>();
	private Map<String, Integer> labels = new HashMap<String, Integer>();
	private Map<String, String> variables = null;
	private int start = 0;

	private BlockingQueue<String> clientInput; // read-only
	private BlockingQueue<String> serverResponse; // read-only
	private AbstractProxy sendToServer; // send commands to server
	private AbstractProxy sendToClient; // send output to client

	public Program() {
		// empty constructor
	}

	public Program(BlockingQueue<String> clientInput, BlockingQueue<String> serverResponse, AbstractProxy sendToServer,
			AbstractProxy sendToClient) {
		this.clientInput = clientInput;
		this.serverResponse = serverResponse;
		this.sendToServer = sendToServer;
		this.sendToClient = sendToClient;
	}

	/**
	 * Execute the script.
	 */
	public void run() {
		// TODO decide what engine to run
		Thread script = new Thread(
				new StormFrontInterpreter(clientInput, serverResponse, sendToServer, sendToClient, this));
		script.start();
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

	public BlockingQueue<String> getClientInput() {
		return clientInput;
	}

	public void setClientInput(BlockingQueue<String> clientInput) {
		this.clientInput = clientInput;
	}

	public BlockingQueue<String> getServerResponse() {
		return serverResponse;
	}

	public void setServerResponse(BlockingQueue<String> serverResponse) {
		this.serverResponse = serverResponse;
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
}
