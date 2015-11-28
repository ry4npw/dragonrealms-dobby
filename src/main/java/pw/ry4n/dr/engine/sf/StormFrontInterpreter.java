package pw.ry4n.dr.engine.sf;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import pw.ry4n.dr.AbstractProxy;
import pw.ry4n.dr.engine.sf.model.Program;

/**
 * A factory method that takes a StormFront (SF) script file as input and
 * returns an object representing the program.
 * 
 * @author Ryan Powell
 */
public class StormFrontInterpreter implements Runnable {
	private BlockingQueue<String> clientInput; // read-only user input
	private BlockingQueue<String> serverResponse; // read-only server responses
	private AbstractProxy send; // proxy to send commands to server

	private List<MatchToken> matchList;
	private Program program;

	public StormFrontInterpreter(BlockingQueue<String> clientInput, BlockingQueue<String> serverResponse,
			AbstractProxy send, Program program) {
		this.clientInput = clientInput;
		this.serverResponse = serverResponse;
		this.send = send;
		this.program = program;
	}

	public void run() {
		// TODO arguments may start or end with quotation marks, remove them
		// with: argument.replaceAll("\"$|^\"", "");

		// TODO arguments will contain '_' instead of spaces, replac them with:
		// argument.replaceAll("_", "");
	}
}
