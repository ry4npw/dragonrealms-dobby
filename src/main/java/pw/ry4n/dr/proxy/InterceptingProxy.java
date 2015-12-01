package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.net.Socket;

import pw.ry4n.dr.engine.sf.model.Program;

public class InterceptingProxy extends AbstractProxy {
	CommandSender commandSender;

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
		System.out.println(line);

		if (line.trim().startsWith(";")) {
			// capture commands that start with a semicolon
			String input = line.substring(1);

			// parse command and do something with it
			System.out.println("Captured input: " + input);

			Thread script = new Thread(new Program(input, this, companion));
			script.start();
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
