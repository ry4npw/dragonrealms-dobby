package pw.ry4n.dr.engine.model;

import java.util.Arrays;

public class StormFrontLine {
	private byte command = -1;
	private int n = -1;
	private byte subCommand = -1;
	private String[] arguments = null;

	public StormFrontLine() {
		// empty constructor
	}

	public StormFrontLine(byte command, String[] arguments) {
		this.command = command;
		this.arguments = arguments;
	}

	public StormFrontLine(byte command, int n, byte subCommand, String[] arguments) {
		this.command = command;
		this.n = n;
		this.subCommand = subCommand;
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

	@Override
	public String toString() {
		return "Line [command=" + command + ", n=" + n + ", subCommand=" + subCommand + ", arguments="
				+ Arrays.toString(arguments) + "]";
	}
}
