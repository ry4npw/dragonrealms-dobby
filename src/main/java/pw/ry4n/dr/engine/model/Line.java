package pw.ry4n.dr.engine.model;

public class Line {
	private byte command;
	private String[] arguments;

	public Line(byte command, String[] arguments) {
		this.command = command;
		this.arguments = arguments;
	}

	public byte getCommand() {
		return command;
	}

	public void setCommand(byte command) {
		this.command = command;
	}

	public String[] getArguments() {
		return arguments;
	}

	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}
}
