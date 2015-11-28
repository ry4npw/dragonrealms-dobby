package pw.ry4n.dr.engine.sf.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import pw.ry4n.dr.AbstractProxy;
import pw.ry4n.dr.engine.sf.StormFrontInterpreter;

public class Program implements Runnable {
	private List<Line> lines = new ArrayList<Line>();
	private Map<String, Integer> labels = new HashMap<String, Integer>();
	private int start;

	private BlockingQueue<String> clientInput; // read-only
	private BlockingQueue<String> serverResponse; // read-only
	private AbstractProxy send; // send commands to server

	public Program() {
		// empty constructor
	}

	public Program(BlockingQueue<String> clientInput, BlockingQueue<String> serverResponse, AbstractProxy send) {
		this.clientInput = clientInput;
		this.serverResponse = serverResponse;
		this.send = send;
	}

	/**
	 * Loads the script identified by the fileName.
	 * 
	 * @param fileName
	 */
	public void load(String fileName) {

	}

	/**
	 * Execute the script.
	 */
	public void run() {
		// TODO decide what engine to run
		Thread script = new Thread(new StormFrontInterpreter(clientInput, serverResponse, send, this));
		script.start();
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

	public AbstractProxy getSend() {
		return send;
	}

	public void setSend(AbstractProxy send) {
		this.send = send;
	}
}
