package pw.ry4n.dr.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import pw.ry4n.dr.engine.core.Program;
import pw.ry4n.dr.engine.core.State;
import pw.ry4n.dr.engine.model.ProgramImpl;
import pw.ry4n.dr.util.FixedSizeArrayDeque;

public class InterceptingProxy extends AbstractProxy {
	CommandQueue commandsToSend;
	private List<Program> scripts = new ArrayList<Program>();
	FixedSizeArrayDeque<String> sentCommands = new FixedSizeArrayDeque<>(10);

	/**
	 * Package private constructor for unit testing.
	 * 
	 * @param sendProxy
	 * @param to
	 */
	InterceptingProxy(AbstractProxy sendProxy, OutputStream to) {
		this.to = to;
		commandsToSend = new CommandQueue(sendProxy, null);
		companion = sendProxy;
	}

	public InterceptingProxy(Socket local, Socket remote) {
		super(local, remote);
	}

	@Override
	public void run() {
		if (companion != null) {
			commandsToSend = new CommandQueue(this, companion);
			Thread messageQueue = new Thread(commandsToSend);
			messageQueue.start();
		}

		super.run();
	}

	@Override
	protected void filter(byte[] buffer, int count) throws IOException {
		String line = new String(buffer, 0, count, "iso-8859-1").replace("\n", "");
		System.out.println(">" + line);

		if (line.trim().startsWith(";")) {
			// handle dobby commands (any line starting with a semicolon)
			handleCommand(line.substring(1));
		} else {
			// all other input should be passed along to server (including blank lines)
			to.write(buffer, 0, count);

			// remember this in our command queue
			if (!"".equals(line.trim())) {
				sentCommands.push(line);

				// and to any listeners
				notifyAllListeners(line);
			}
		}
	}

	private void handleCommand(String input) throws IOException {
		if (input != null && input.toLowerCase().startsWith("every ")) {
			int spaceAt = input.indexOf(' ');
			if (spaceAt >= 5) {
				every(input.substring(spaceAt + 1));
			}
		} else if (input != null && input.toLowerCase().equals("list")) {
			list();
		} else if (input != null && (input.toLowerCase().equals("pause") || input.toLowerCase().startsWith("pause "))) {
			int spaceAt = input.indexOf(' ');
			if (spaceAt >= 5) {
				pauseScript(input.substring(spaceAt + 1));
			} else {
				pauseAllScripts();
			}
		} else if (input != null && input.toLowerCase().startsWith("repeat ")) {
			int spaceAt = input.indexOf(' ');
			if (spaceAt >= 6) {
				repeat(input.substring(spaceAt + 1));
			}
		} else if (input != null && (input.toLowerCase().equals("resume") || input.toLowerCase().startsWith("resume "))) {
			int spaceAt = input.indexOf(' ');
			if (spaceAt >= 6) {
				resumeScript(input.substring(spaceAt + 1));
			} else {
				resumeAllScripts();
			}
		} else if (input != null && (input.toLowerCase().equals("stop") || input.toLowerCase().startsWith("stop"))) {
			int spaceAt = input.indexOf(' ');
			if (spaceAt >= 4) {
				stopScript(input.substring(spaceAt + 1));
			} else {
				stopAllScripts();
			}
		} else {
			runScript(input);
		}
	}

	private void every(String substring) throws IOException {
		try {
			TimedThread tt = parseEvery(substring);
			scripts.add(tt);
			Thread t = new Thread(tt);
			tt.setThread(t);
			t.start();
		} catch (Exception e) {
			sendUserMessage(e.getMessage());
			sendUserMessage("Please try something like:  ;every 91 seconds PREDICT WEATHER");
		}
	}

	TimedThread parseEvery(String substring) {
		Scanner scanner = new Scanner(substring.toLowerCase());
		String time = scanner.next();

		// parse duration and timeUnit
		long duration = -1L;
		TimeUnit timeUnit = null;

		if (time.endsWith("s")) {
			timeUnit = TimeUnit.SECONDS;
			time = time.substring(0, time.length() - 1);
		} else if (time.endsWith("m")) {
			timeUnit = TimeUnit.MINUTES;
			time = time.substring(0, time.length() - 1);
		} else if (time.endsWith("h")) {
			timeUnit = TimeUnit.HOURS;
			time = time.substring(0, time.length() - 1);
		}

		try {
			duration = Long.parseLong(time);
		} catch (NumberFormatException e) {
			scanner.close();
			throw new NumberFormatException(time + " is not an integer duration.");
		}

		if (timeUnit == null) {
			String timeUnitString = scanner.next().toLowerCase();
			switch (timeUnitString) {
			case "s":
			case "sec":
			case "second":
			case "seconds":
				timeUnit = TimeUnit.SECONDS;
				break;
			case "m":
			case "min":
			case "minute":
			case "minutes":
				timeUnit = TimeUnit.MINUTES;
				break;
			case "h":
			case "hour":
			case "hours":
				timeUnit = TimeUnit.HOURS;
				break;
			default:
				scanner.close();
				throw new IllegalArgumentException(timeUnitString + " is not one of (seconds|minutes|hours).");
			}
		}

		// use rest of string as command
		scanner.useDelimiter("\\z");

		if (!scanner.hasNext()) {
			scanner.close();
			throw new IllegalArgumentException("You must specify a command to send.");
		}

		String command = scanner.next().trim();
		scanner.close();

		if ("".equals(command)) {
			throw new IllegalArgumentException("You must specify a command to send.");
		}

		return new TimedThread(this, commandsToSend, duration, timeUnit, command);
	}

	private void list() throws IOException {
		cleanStoppedScriptsList();

		if (scripts.isEmpty()) {
			sendUserMessage("No running scripts.");
			return;
		}

		StringBuilder list = new StringBuilder();
		list.append("Active scripts:");

		int padding = (int) Math.log10(scripts.size());
		for (int i = 0; i < scripts.size(); i++) {
			list.append("\n       ");
			int numSpaces = padding - (int) Math.log10(i);
			while (numSpaces > 0) {
				list.append(' ');
				numSpaces--;
			}
			list.append(i).append(": ").append(scripts.get(i).getName()).append(" ")
					.append(scripts.get(i).getState().name());
		}

		sendUserMessage(list.toString());
	}

	private void cleanStoppedScriptsList() {
		List<Program> stoppedScripts = new ArrayList<Program>();

		for (Program script : scripts) {
			if (State.STOPPED.equals(script.getState())) {
				stoppedScripts.add(script);
			}
		}

		scripts.removeAll(stoppedScripts);
	}

	private void pauseAllScripts() {
		for (Program script : scripts) {
			pauseScript(script);
		}
	}

	private void pauseScript(String argument) throws IOException {
		try {
			pauseScript(Integer.parseInt(argument));
		} catch (NumberFormatException e) {
			sendUserMessage("'" + argument + "'" +" is not a numeric script identifier from ;list");
		}
	}

	private void pauseScript(int i) {
		if (i >= 0 && i < scripts.size()) {
			pauseScript(scripts.get(i));
		}
	}

	private void pauseScript(Program script) {
		// only pause running scripts.
		if (!State.STOPPED.equals(script.getState())) {
			script.pause();
		}
	}

	void repeat(String argument) throws IOException {
		repeat(Integer.parseInt(argument));
	}

	private void repeat(int i) throws IOException {
		if (i > 10) {
			sendUserMessage("You cannot repeat more than the last 10 commands.");
			return;
		}

		if (i < 1) {
			sendUserMessage("You must repeat at least 1 command.");
			return;
		}

		if (sentCommands.size() < i) {
			sendUserMessage("You have not yet sent " + i + " commands.");
			return;
		}

		// get to the last X commands
		Iterator<String> commands = sentCommands.descendingIterator();
		int commandsToSkip = sentCommands.size() - i;
		while (commandsToSkip > 0) {
			commands.next();
			commandsToSkip--;
		}

		// repeat last X commands sent to server
		while (commands.hasNext()) {
			String command = commands.next();
			System.out.println("repeat> " + command);
			sendUserMessage("repeat: " + command);
			commandsToSend.enqueue(command);
		}
	}

	private void resumeAllScripts() {
		for (Program script : scripts) {
			resumeScript(script);
		}
	}

	private void resumeScript(String argument) throws IOException {
		try {
			resumeScript(Integer.parseInt(argument));
		} catch (NumberFormatException e) {
			sendUserMessage("'" + argument + "'" +" is not a numeric script identifier from ;list");
		}
	}

	private void resumeScript(int i) {
		if (i >= 0 && i < scripts.size()) {
			resumeScript(scripts.get(i));
		}
	}

	private void resumeScript(Program script) {
		// only resume paused scripts
		if (State.PAUSED.equals(script.getState())) {
			script.resume();
		}
	}

	void runScript(String input) {
		Program script = new ProgramImpl(input, this, companion);
		scripts.add(script);
		Thread t = new Thread(script);
		script.setThread(t);
		t.start();
	}

	void sendUserMessage(String message) throws IOException {
		companion.send(message);
	}

	private void stopAllScripts() {
		for (Program script : scripts) {
			stopScript(script);
		}
	}

	private void stopScript(String argument) throws IOException {
		try {
			stopScript(Integer.parseInt(argument));
		} catch (NumberFormatException e) {
			sendUserMessage("'" + argument + "'" +" is not a numeric script identifier from ;list");
		}
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
		return commandsToSend;
	}

	public void setCommandSender(CommandQueue commandSender) {
		this.commandsToSend = commandSender;
	}
}
