package pw.ry4n.dr.engine.parser;

public class TokenTypes {
	public static final byte ECHO = 1;
	public static final byte PUT = 2;
	public static final byte MOVE = 3;
	public static final byte WAIT = 4;
	public static final byte NEXTROOM = 5;
	public static final byte WATIFOR = 6;
	public static final byte WAITFORRE = 7;
	public static final byte PAUSE = 8;
	public static final byte GOTO = 9;
	public static final byte EXIT = 10;
	public static final byte MATCH = 11; // MATCH <label> <string>
	public static final byte MATCHRE = 12; // MATCHRE <label> <regex>
	public static final byte MATCHWAIT = 13;
	public static final byte IF_ = 14;
	public static final byte SHIFT = 15;
	public static final byte SAVE = 16;
	public static final byte SETVARIABLE = 17;
	public static final byte COUNTER = 18;
	public static final byte SET = 19; // follows COUNTER
	public static final byte ADD = 20; // follows COUNTER
	public static final byte SUBTRACT = 21; // follows COUNTER
	public static final byte MULTIPLY = 22; // follows COUNTER
	public static final byte DIVIDE = 23; // follows COUNTER
	public static final byte COMMENT = 24; // #
}
