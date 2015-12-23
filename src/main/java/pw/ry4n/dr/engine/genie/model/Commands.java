package pw.ry4n.dr.engine.genie.model;

public class Commands extends pw.ry4n.dr.engine.sf.model.Commands {
	// Genie commands
	public static final byte ACTION = 27; // action <command> when <pattern> (several commands supported with ';')
	public static final byte DEBUG = 28; // debug <level>
	public static final byte DELAY = 29; // similar to PAUSE, but do not wait for RT
	public static final byte EVAL = 30; // eval <variable> <expression>
	public static final byte EVALMATH = 31; // evalmath <variable> <expression>
	public static final byte GOSUB = 32; // gosub <label> [argument argument argument ...] -OR- gosub clear
	public static final byte IF = 33; // if <expression> then <command>
	public static final byte ELSE = 34;
	public static final byte INCLUDE = 35; // include <file name>
	public static final byte MATH = 36; // math <variable> add/substract/set/multiply/divide/modulus <number>
	public static final byte RANDOM = 37;
	public static final byte RETURN = 38;
	public static final byte SEND = 39; // send <command> -OR- send [delay] <command> (adds to command queue)
	public static final byte TIMER = 40; // timer start/stop/setstart/clear
	public static final byte VAR = 41;
	public static final byte UNVAR = 42;
	public static final byte SCRIPT_BLOCK = 43; // {}
}
