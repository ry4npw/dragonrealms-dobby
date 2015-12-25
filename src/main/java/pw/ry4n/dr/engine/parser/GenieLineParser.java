package pw.ry4n.dr.engine.parser;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.core.ParserException;
import pw.ry4n.dr.engine.model.GenieCommands;
import pw.ry4n.dr.engine.model.StormFrontLine;

/**
 * <p>
 * The line parser will parse each line in the format: <command> (<argument1> ...)
 * </p>
 *
 * @author Ryan Powell
 */
public class GenieLineParser extends pw.ry4n.dr.engine.parser.StormFrontLineParser{
	public GenieLineParser(DataCharBuffer dataBuffer) {
		super(dataBuffer);
	}

	@Override
	protected void parseCommand(StormFrontLine line) {
		// check for Genie commands
		switch (this.dataBuffer.data[this.dataPosition]) {
		case '{':
			// SCRIPT_BLOCK
			break;
		case 'a':
		case 'A':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'c' || this.dataBuffer.data[this.dataPosition + 1] == 'C')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'i' || this.dataBuffer.data[this.dataPosition + 3] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'o' || this.dataBuffer.data[this.dataPosition + 4] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'n' || this.dataBuffer.data[this.dataPosition + 5] == 'N')
					&& this.dataBuffer.data[this.dataPosition + 6] == ' ') {
				// ACTION
				line.setCommand(GenieCommands.ACTION);
				this.dataPosition += 6;
				parseAction(line);
			}
			break;
		case 'd':
		case 'D':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'e' || this.dataBuffer.data[this.dataPosition + 1] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'b' || this.dataBuffer.data[this.dataPosition + 2] == 'B')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'u' || this.dataBuffer.data[this.dataPosition + 3] == 'U')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'g' || this.dataBuffer.data[this.dataPosition + 4] == 'G')
					&& this.dataBuffer.data[this.dataPosition + 5] == ' ') {
				// DEBUG
				line.setCommand(GenieCommands.DEBUG);
				this.dataPosition += 5;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'e' || this.dataBuffer.data[this.dataPosition + 1] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'l' || this.dataBuffer.data[this.dataPosition + 2] == 'L')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'a' || this.dataBuffer.data[this.dataPosition + 3] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'y' || this.dataBuffer.data[this.dataPosition + 4] == 'Y')
					&& this.dataBuffer.data[this.dataPosition + 5] == ' ') {
				// DELAY
				line.setCommand(GenieCommands.DELAY);
				this.dataPosition += 5;
			}
			break;
		case 'e':
		case 'E':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'l' || this.dataBuffer.data[this.dataPosition + 1] == 'L')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 's' || this.dataBuffer.data[this.dataPosition + 2] == 'S')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'e' || this.dataBuffer.data[this.dataPosition + 3] == 'E')
					&& this.dataBuffer.data[this.dataPosition + 4] == ' ') {
				// ELSE
				line.setCommand(GenieCommands.ELSE);
				this.dataPosition += 4;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'v' || this.dataBuffer.data[this.dataPosition + 1] == 'V')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'a' || this.dataBuffer.data[this.dataPosition + 2] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'l' || this.dataBuffer.data[this.dataPosition + 3] == 'L')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'm' || this.dataBuffer.data[this.dataPosition + 4] == 'M')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'a' || this.dataBuffer.data[this.dataPosition + 5] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 't' || this.dataBuffer.data[this.dataPosition + 6] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 7] == 'h' || this.dataBuffer.data[this.dataPosition + 7] == 'M')
					&& this.dataBuffer.data[this.dataPosition + 8] == ' ') {
				// EVALMATH
				line.setCommand(GenieCommands.EVALMATH);
				this.dataPosition += 8;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'v' || this.dataBuffer.data[this.dataPosition + 1] == 'X')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'a' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'l' || this.dataBuffer.data[this.dataPosition + 3] == 'T')
					&& this.dataBuffer.data[this.dataPosition + 4] == ' ') {
				// EVAL
				line.setCommand(GenieCommands.EVAL);
				this.dataPosition += 4;
			}
			break;
		case 'g':
		case 'G':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'o' || this.dataBuffer.data[this.dataPosition + 1] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 's' || this.dataBuffer.data[this.dataPosition + 2] == 'S')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'u' || this.dataBuffer.data[this.dataPosition + 3] == 'U')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'b' || this.dataBuffer.data[this.dataPosition + 4] == 'B')
					&& this.dataBuffer.data[this.dataPosition + 5] == ' ') {
				// GOSUB
				line.setCommand(GenieCommands.GOSUB);
				this.dataPosition += 5;
			}
			break;
		case 'i':
		case 'I':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'f' || this.dataBuffer.data[this.dataPosition + 1] == 'F')
					&& this.dataBuffer.data[this.dataPosition + 2] == ' ') {
				// IF
				line.setCommand(GenieCommands.IF);
				this.dataPosition += 2;
				parseConditionalIf(line);
			}
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'n' || this.dataBuffer.data[this.dataPosition + 1] == 'N')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'c' || this.dataBuffer.data[this.dataPosition + 2] == 'C')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'l' || this.dataBuffer.data[this.dataPosition + 3] == 'L')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'u' || this.dataBuffer.data[this.dataPosition + 4] == 'U')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'd' || this.dataBuffer.data[this.dataPosition + 5] == 'D')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'e' || this.dataBuffer.data[this.dataPosition + 6] == 'E')
					&& this.dataBuffer.data[this.dataPosition + 7] == ' ') {
				line.setCommand(GenieCommands.INCLUDE);
				this.dataPosition += 7;
			}
			break;
		// MATH
		case 'm':
		case 'M':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'h' || this.dataBuffer.data[this.dataPosition + 3] == 'H')
					&& this.dataBuffer.data[this.dataPosition + 4] == ' ') {
				// MATH
				line.setCommand(GenieCommands.MATH);
				parseMath(line);
			}
			break;
		// RANDOM
		// RETURN
		case 's':
		case 'S':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'e' || this.dataBuffer.data[this.dataPosition + 1] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'n' || this.dataBuffer.data[this.dataPosition + 2] == 'N')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'd' || this.dataBuffer.data[this.dataPosition + 3] == 'D')
					&& this.dataBuffer.data[this.dataPosition + 4] == ' ') {
				// SEND
				line.setCommand(GenieCommands.SEND);
				this.dataPosition += 4;
			}
			break;
		// TIMER
		case 'u':
		case 'U':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'n' || this.dataBuffer.data[this.dataPosition + 1] == 'N')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'v' || this.dataBuffer.data[this.dataPosition + 2] == 'V')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'a' || this.dataBuffer.data[this.dataPosition + 3] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'r' || this.dataBuffer.data[this.dataPosition + 4] == 'R')
					&& this.dataBuffer.data[this.dataPosition + 5] == ' ') {
				// UNVAR
				line.setCommand(GenieCommands.UNVAR);
				this.dataPosition += 5;
			}
			break;
		case 'v':
		case 'V':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'r' || this.dataBuffer.data[this.dataPosition + 2] == 'R')
					&& this.dataBuffer.data[this.dataPosition + 3] == ' ') {
				// VAR
				line.setCommand(GenieCommands.VAR);
				this.dataPosition += 3;
			}
			break;
		}

		if (GenieCommands.NONE == line.getCommand()) {
			// not a Genie command, try to parse StormFront command
			super.parseCommand(line);
		}
	}

	private void parseAction(StormFrontLine line) {
		// action (class) <command> when <pattern>
		// action (class) on
		// action (class) off
	}

	private void parseConditionalIf(StormFrontLine line) {
		// if <expression> then <command>
	}

	private void parseMath(StormFrontLine line) {
		// handle {variableName} {COUNTER {SET|ADD|SUBTRACT|MULTIPLY|DIVIDE|MODULUS}
	}
}
