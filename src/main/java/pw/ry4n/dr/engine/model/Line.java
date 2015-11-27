package pw.ry4n.dr.engine.model;

import java.util.ArrayList;
import java.util.List;

public class Line {
	private byte command;
	private List<String> arguments;

	public Line() {
		command = -1;
		arguments = new ArrayList<String>();
	}

	public Line(byte command, List<String> arguments) {
		this.command = command;
		this.arguments = arguments;
	}

	public byte getCommand() {
		return command;
	}

	public void setCommand(byte command) {
		this.command = command;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}
}
