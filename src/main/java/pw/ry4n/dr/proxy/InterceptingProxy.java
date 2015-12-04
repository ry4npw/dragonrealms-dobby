package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import pw.ry4n.dr.engine.sf.model.Program;

public class InterceptingProxy extends AbstractProxy {
	CommandSender commandSender;
	private List<Program> scripts = new ArrayList<Program>();

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

			// handle commands
			if (input != null && input.toLowerCase().startsWith("list")) {
				echoScripts();
			} else if (input != null && input.toLowerCase().startsWith("stop")) {
				int spaceAt = input.indexOf(' ');
				if (spaceAt >= 4) {
					stopScript(input.substring(spaceAt + 1));
				} else {
					stopAllScripts();
				}
			} else {
				// else script
				Program script = new Program(input, this, companion);
				scripts.add(script);
				Thread t = new Thread(script);
				t.start();
				script.setThread(t);
			}
		} else {
			// all other input should be passed along to server
			send(line);

			// and to any listeners
			notifyAllListeners(line);
		}
	}

	private void echoScripts() throws IOException {
		if (scripts.isEmpty()) {
			companion.send("No running scripts.");
			return;
		}

		StringBuilder list = new StringBuilder();
		list.append("Running scripts:");

		for (int i = 0; i < scripts.size(); i++) {
			list.append("\n      ");
			list.append(i).append(": ").append(scripts.get(i).getName()).append(".").append(scripts.get(i).getType());
		}

		if (companion != null) {
			companion.send(list.toString());
		}
	}

	private void stopAllScripts() {
		for (Program script : scripts) {
			stopScript(script);
		}
	}

	private void stopScript(String argument) {
		stopScript(Integer.parseInt(argument));
	}

	private void stopScript(int i) {
		if (i >= 0 && i < scripts.size()) {
			stopScript(scripts.get(i));
		}
	}

	private void stopScript(Program script) {
		if (script != null) {
			// signal all scripts to stop executing
			script.stop();
			// interrupt any waiting threads so they can end
			script.getThread().interrupt();
		}
	}

	public CommandSender getCommandSender() {
		return commandSender;
	}

	public void setCommandSender(CommandSender commandSender) {
		this.commandSender = commandSender;
	}
}
