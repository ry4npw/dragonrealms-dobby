package pw.ry4n.dr.engine.sf.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import pw.ry4n.dr.AbstractProxy;
import pw.ry4n.dr.engine.sf.StormFrontInterpreter;

public class Program implements Runnable {
	public List<Line> lines;
	public Map<String, Integer> labels;
	public int start;

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
		// decide what engine to run
		Thread script = new Thread(new StormFrontInterpreter(clientInput, serverResponse, send, this));
		script.start();
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
