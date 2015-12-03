package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import pw.ry4n.dr.engine.sf.model.Program;

public class InterceptingProxy extends AbstractProxy {
	CommandSender commandSender;
	private List<Program> scripts = new ArrayList<Program>();
	private List<Thread> threads = new ArrayList<Thread>();

	public InterceptingProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	public void run() {
		if (companion != null) {
			commandSender = new CommandSender(this, companion);
			Thread messageQueue = new Thread(commandSender);
			messageQueue.start();
		}

		super.run();
	}

	@Override
	protected void filter(String line) throws IOException {
		System.out.println(">" + line);

		if (line.trim().startsWith(";")) {
			// capture commands that start with a semicolon
			String input = line.substring(1);

			// parse command and do something with it
			System.out.println("input: " + input);
			if ("stop".equalsIgnoreCase(input)) {
				for (Program script : scripts) {
					// signal all scripts to stop executing
					script.stop();
				}
				for (Thread t : threads) {
					// interrupt any waiting threads so they can end
					t.interrupt();
				}
			} else {
				// else script
				Program script = new Program(input, this, companion);
				scripts.add(script);
				Thread t = new Thread(script);
				t.start();
				threads.add(t);
			}
		} else {
			// all other input should be passed along to server
			send(line);

			// and to any listeners
			notifyAllListeners(line);
		}
	}

	public CommandSender getCommandSender() {
		return commandSender;
	}

	public void setCommandSender(CommandSender commandSender) {
		this.commandSender = commandSender;
	}
}
