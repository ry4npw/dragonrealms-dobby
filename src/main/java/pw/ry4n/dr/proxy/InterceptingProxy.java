package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import pw.ry4n.dr.engine.sf.model.Program;
import pw.ry4n.dr.engine.sf.model.State;

public class InterceptingProxy extends AbstractProxy {
	CommandQueue commandSender;
	private List<Program> scripts = new ArrayList<Program>();

	public InterceptingProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	public void run() {
		if (companion != null) {
			commandSender = new CommandQueue(this, companion);
			Thread messageQueue = new Thread(commandSender);
			messageQueue.start();
		}

		super.run();
	}

	@Override
	protected void filter(byte[] buffer, int count) throws IOException {
		String line = new String(buffer, 0, count, "iso-8859-1");
		System.out.println(">" + line);

		if (line.trim().startsWith(";")) {
			// handle dobby commands (any line starting with a semicolon)
			handleCommand(line.substring(1));
		} else {
			// all other input should be passed along to server
			to.write(buffer, 0, count);

			// and to any listeners
			notifyAllListeners(line);
		}
	}

	private void handleCommand(String input) throws IOException {
		if (input != null && input.toLowerCase().startsWith("list")) {
			list();
		} else if (input != null && input.toLowerCase().startsWith("pause")) {
			int spaceAt = input.indexOf(' ');
			if (spaceAt >= 4) {
				pauseScript(input.substring(spaceAt + 1));
			} else {
				pauseAllScripts();
			}
		} else if (input != null && input.toLowerCase().startsWith("resume")) {
			int spaceAt = input.indexOf(' ');
			if (spaceAt >= 4) {
				resumeScript(input.substring(spaceAt + 1));
			} else {
				resumeAllScripts();
			}
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
	}

	private void list() throws IOException {
		cleanStoppedScriptsList();

		if (scripts.isEmpty()) {
			companion.send("No running scripts.");
			return;
		}

		StringBuilder list = new StringBuilder();
		list.append("Active scripts:");

		for (int i = 0; i < scripts.size(); i++) {
			list.append("\n      ");
			list.append(i).append(": ").append(scripts.get(i).getName()).append(" ")
					.append(scripts.get(i).getState().name());
		}

		if (companion != null) {
			companion.send(list.toString());
		}
	}

	private void cleanStoppedScriptsList() {
		List<Program> stoppedScripts = new ArrayList<Program>();

		for (Program script : scripts) {
			if (State.STOPPED.equals(script.getState())) {
				stoppedScripts.add(script);
			}
		}

		for (Program script : stoppedScripts) {
			scripts.remove(script);
		}
	}

	private void pauseAllScripts() {
		for (Program script : scripts) {
			pauseScript(script);
		}
	}

	private void pauseScript(String argument) {
		pauseScript(Integer.parseInt(argument));
	}

	private void pauseScript(int i) {
		if (i >= 0 && i < scripts.size()) {
			pauseScript(scripts.get(i));
		}
	}

	private void pauseScript(Program script) {
		script.pause();
	}

	private void resumeAllScripts() {
		for (Program script : scripts) {
			resumeScript(script);
		}
	}

	private void resumeScript(String argument) {
		resumeScript(Integer.parseInt(argument));
	}

	private void resumeScript(int i) {
		if (i >= 0 && i < scripts.size()) {
			resumeScript(scripts.get(i));
		}
	}

	private void resumeScript(Program script) {
		if (State.PAUSED.equals(script.getState())) {
			// only resume paused scripts
			script.resume();
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

	public CommandQueue getCommandSender() {
		return commandSender;
	}

	public void setCommandSender(CommandQueue commandSender) {
		this.commandSender = commandSender;
	}
}
