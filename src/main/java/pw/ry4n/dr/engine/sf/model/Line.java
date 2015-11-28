package pw.ry4n.dr.engine.sf.model;

public class Line {
	private byte command = -1;
	private int n = -1;
	private byte subCommand = -1;
	private String[] arguments = null;

	public Line() {
		// empty constructor
	}

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

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public byte getSubCommand() {
		return subCommand;
	}

	public void setSubCommand(byte subCommand) {
		this.subCommand = subCommand;
	}

	public String[] getArguments() {
		return arguments;
	}

	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}
}
