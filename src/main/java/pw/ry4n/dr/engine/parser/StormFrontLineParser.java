package pw.ry4n.dr.engine.parser;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.core.ParserException;
import pw.ry4n.dr.engine.model.StormFrontCommands;
import pw.ry4n.dr.engine.model.StormFrontLine;

/**
 * <p>
 * The line parser will parse each line in the format: <command> (<argument1> ...)
 * </p>
 *
 * @author Ryan Powell
 */
public class StormFrontLineParser {
	protected DataCharBuffer dataBuffer = null;

	protected int dataPosition = 0;
	int lineCounter = 1;

	public StormFrontLineParser(DataCharBuffer dataBuffer) {
		this.dataBuffer = dataBuffer;
	}

	public StormFrontLine parseLine() {
		skipWhitespace();
		if (!hasMoreChars()) {
			return null;
		}

		StormFrontLine line = new StormFrontLine();

		parseCommand(line);

		if (line.getCommand() == StormFrontCommands.NONE) {
			// not a command, so check to see if this is a label
			parseLabel(line);
		}

		// parse any remaining arguments
		if (hasMoreChars()) {
			parseArguments(line);
		}

		if (line.getCommand() == StormFrontCommands.NONE) {
			// ParserException
			throw new ParserException("line " + this.lineCounter + ": Unrecognized command");
		}

		return line;
	}

	protected void parseLabel(StormFrontLine line) {
		int startPosition = this.dataPosition;
		while (!isEndOfLine()) {
			if (Character.isWhitespace(this.dataBuffer.data[dataPosition])) {
				// labels cannot contain whitespace
				break;
			} else if (this.dataBuffer.data[dataPosition] == ':') {
				line.setCommand(StormFrontCommands.LABEL);
				String label = new String(this.dataBuffer.data, startPosition, this.dataPosition - startPosition);
				line.setArguments(new String[] { label });
			}

			dataPosition++;
		}
		if (StormFrontCommands.NONE == line.getCommand()) {
			// we didn't find a label on this line, back to our starting position
			this.dataPosition = startPosition;
		}
	}

	protected void parseCommand(StormFrontLine line) {
		switch (this.dataBuffer.data[this.dataPosition]) {
		case '#':
			// COMMENT
			line.setCommand(StormFrontCommands.COMMENT);
			skipComment(); // skip comments
			break;
		case 'c':
		case 'C':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'o' || this.dataBuffer.data[this.dataPosition + 1] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'u' || this.dataBuffer.data[this.dataPosition + 2] == 'U')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'n' || this.dataBuffer.data[this.dataPosition + 3] == 'N')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 't' || this.dataBuffer.data[this.dataPosition + 4] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'e' || this.dataBuffer.data[this.dataPosition + 5] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'r' || this.dataBuffer.data[this.dataPosition + 6] == 'R')
					&& this.dataBuffer.data[this.dataPosition + 7] == ' ') {
				parseCounter(line);
			}
			break;
		case 'd':
		case 'D':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'e' || this.dataBuffer.data[this.dataPosition + 1] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'l' || this.dataBuffer.data[this.dataPosition + 2] == 'L')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'e' || this.dataBuffer.data[this.dataPosition + 3] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 't' || this.dataBuffer.data[this.dataPosition + 4] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'e' || this.dataBuffer.data[this.dataPosition + 5] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'v' || this.dataBuffer.data[this.dataPosition + 6] == 'V')
					&& (this.dataBuffer.data[this.dataPosition + 7] == 'a' || this.dataBuffer.data[this.dataPosition + 7] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 8] == 'r' || this.dataBuffer.data[this.dataPosition + 8] == 'R')
					&& (this.dataBuffer.data[this.dataPosition + 9] == 'i' || this.dataBuffer.data[this.dataPosition + 9] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 10] == 'a' || this.dataBuffer.data[this.dataPosition + 10] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 11] == 'b' || this.dataBuffer.data[this.dataPosition + 11] == 'B')
					&& (this.dataBuffer.data[this.dataPosition + 12] == 'l' || this.dataBuffer.data[this.dataPosition + 12] == 'L')
					&& (this.dataBuffer.data[this.dataPosition + 13] == 'e' || this.dataBuffer.data[this.dataPosition + 13] == 'E')
					&& this.dataBuffer.data[this.dataPosition + 14] == ' ') {
				// DELETEVARIABLE
				line.setCommand(StormFrontCommands.DELETEVARIABLE);
				this.dataPosition += 14;
			}
			break;
		case 'e':
		case 'E':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'c' || this.dataBuffer.data[this.dataPosition + 1] == 'C')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'h' || this.dataBuffer.data[this.dataPosition + 2] == 'H')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'o' || this.dataBuffer.data[this.dataPosition + 3] == 'O')) {
				// ECHO
				line.setCommand(StormFrontCommands.ECHO);
				this.dataPosition += 4;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'x' || this.dataBuffer.data[this.dataPosition + 1] == 'X')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'i' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')) {
				// EXIT
				line.setCommand(StormFrontCommands.EXIT);
				this.dataPosition += 4;
			}
			break;
		case 'g':
		case 'G':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'o' || this.dataBuffer.data[this.dataPosition + 1] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'o' || this.dataBuffer.data[this.dataPosition + 3] == 'O')
					&& this.dataBuffer.data[this.dataPosition + 4] == ' ') {
				// GOTO
				line.setCommand(StormFrontCommands.GOTO);
				this.dataPosition += 4;
			}
			break;
		case 'i':
		case 'I':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'f' || this.dataBuffer.data[this.dataPosition + 1] == 'F')
					&& this.dataBuffer.data[this.dataPosition + 2] == '_') {
				// IF_
				parseIf(line);
			}
			break;
		case 'm':
		case 'M':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'o' || this.dataBuffer.data[this.dataPosition + 1] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'v' || this.dataBuffer.data[this.dataPosition + 2] == 'V')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'e' || this.dataBuffer.data[this.dataPosition + 3] == 'E')
					&& this.dataBuffer.data[this.dataPosition + 4] == ' ') {
				// MOVE
				this.dataPosition += 4;
				line.setCommand(StormFrontCommands.MOVE);
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'c' || this.dataBuffer.data[this.dataPosition + 3] == 'C')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'h' || this.dataBuffer.data[this.dataPosition + 4] == 'H')) {
				if ((this.dataBuffer.data[this.dataPosition + 5] == 'r' || this.dataBuffer.data[this.dataPosition + 5] == 'R')
						&& (this.dataBuffer.data[this.dataPosition + 6] == 'e' || this.dataBuffer.data[this.dataPosition + 6] == 'E')
						&& this.dataBuffer.data[this.dataPosition + 7] == ' ') {
					// MATCHRE
					this.dataPosition += 7;
					line.setCommand(StormFrontCommands.MATCHRE);
					parseLabelAndMatchString(line);
				} else if ((this.dataBuffer.data[this.dataPosition + 5] == 'w' || this.dataBuffer.data[this.dataPosition + 5] == 'W')
						&& (this.dataBuffer.data[this.dataPosition + 6] == 'a' || this.dataBuffer.data[this.dataPosition + 6] == 'A')
						&& (this.dataBuffer.data[this.dataPosition + 7] == 'i' || this.dataBuffer.data[this.dataPosition + 7] == 'I')
						&& (this.dataBuffer.data[this.dataPosition + 8] == 't' || this.dataBuffer.data[this.dataPosition + 8] == 'T')) {
					// MATCHWAIT
					this.dataPosition += 9;
					line.setCommand(StormFrontCommands.MATCHWAIT);
				} else if (this.dataBuffer.data[this.dataPosition + 5] == ' ') {
					// MATCH
					this.dataPosition += 5;
					line.setCommand(StormFrontCommands.MATCH);
					parseLabelAndMatchString(line);
				}
			}
			break;
		case 'n':
		case 'N':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'e' || this.dataBuffer.data[this.dataPosition + 1] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'x' || this.dataBuffer.data[this.dataPosition + 2] == 'X')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'r' || this.dataBuffer.data[this.dataPosition + 4] == 'R')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'o' || this.dataBuffer.data[this.dataPosition + 5] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'o' || this.dataBuffer.data[this.dataPosition + 6] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 7] == 'm' || this.dataBuffer.data[this.dataPosition + 7] == 'M')) {
				// NEXTROOM
				line.setCommand(StormFrontCommands.NEXTROOM);
				this.dataPosition += 8;
			}
			break;
		case 'p':
		case 'P':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'u' || this.dataBuffer.data[this.dataPosition + 2] == 'U')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 's' || this.dataBuffer.data[this.dataPosition + 3] == 'S')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'e' || this.dataBuffer.data[this.dataPosition + 4] == 'E')) {
				// PAUSE
				line.setCommand(StormFrontCommands.PAUSE);
				this.dataPosition += 5;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'u' || this.dataBuffer.data[this.dataPosition + 1] == 'U')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')
					&& this.dataBuffer.data[this.dataPosition + 3] == ' ') {
				// PUT
				line.setCommand(StormFrontCommands.PUT);
				this.dataPosition += 3;
			}
			break;
		case 's':
		case 'S':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'v' || this.dataBuffer.data[this.dataPosition + 2] == 'V')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'e' || this.dataBuffer.data[this.dataPosition + 3] == 'E')
					&& this.dataBuffer.data[this.dataPosition + 4] == ' ') {
				// SAVE
				line.setCommand(StormFrontCommands.SAVE);
				this.dataPosition += 4;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'e' || this.dataBuffer.data[this.dataPosition + 1] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'v' || this.dataBuffer.data[this.dataPosition + 3] == 'V')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'a' || this.dataBuffer.data[this.dataPosition + 4] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'r' || this.dataBuffer.data[this.dataPosition + 5] == 'R')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'i' || this.dataBuffer.data[this.dataPosition + 6] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 7] == 'a' || this.dataBuffer.data[this.dataPosition + 7] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 8] == 'b' || this.dataBuffer.data[this.dataPosition + 8] == 'B')
					&& (this.dataBuffer.data[this.dataPosition + 9] == 'l' || this.dataBuffer.data[this.dataPosition + 9] == 'L')
					&& (this.dataBuffer.data[this.dataPosition + 10] == 'e' || this.dataBuffer.data[this.dataPosition + 10] == 'E')
					&& this.dataBuffer.data[this.dataPosition + 11] == ' ') {
				// SETVARIABLE
				line.setCommand(StormFrontCommands.SETVARIABLE);
				this.dataPosition += 11;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'h' || this.dataBuffer.data[this.dataPosition + 1] == 'H')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'i' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'f' || this.dataBuffer.data[this.dataPosition + 3] == 'F')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 't' || this.dataBuffer.data[this.dataPosition + 4] == 'T')) {
				// SHIFT
				line.setCommand(StormFrontCommands.SHIFT);
				this.dataPosition += 5;
			}
			break;
		case 'w':
		case 'W':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'i' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'f' || this.dataBuffer.data[this.dataPosition + 4] == 'F')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'o' || this.dataBuffer.data[this.dataPosition + 5] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'r' || this.dataBuffer.data[this.dataPosition + 6] == 'R')
					&& (this.dataBuffer.data[this.dataPosition + 7] == 'r' || this.dataBuffer.data[this.dataPosition + 7] == 'R')
					&& (this.dataBuffer.data[this.dataPosition + 8] == 'e' || this.dataBuffer.data[this.dataPosition + 8] == 'E')
					&& this.dataBuffer.data[this.dataPosition + 9] == ' ') {
				// WAITFORRE
				line.setCommand(StormFrontCommands.WAITFORRE);
				this.dataPosition += 9;
				line.setArguments(new String[] { getRestOfLine() });
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'i' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'f' || this.dataBuffer.data[this.dataPosition + 4] == 'F')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'o' || this.dataBuffer.data[this.dataPosition + 5] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'r' || this.dataBuffer.data[this.dataPosition + 6] == 'R')
					&& this.dataBuffer.data[this.dataPosition + 7] == ' ') {
				// WAITFOR
				line.setCommand(StormFrontCommands.WAITFOR);
				this.dataPosition += 7;
				line.setArguments(new String[] { getRestOfLine() });
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'i' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')) {
				// WAIT
				line.setCommand(StormFrontCommands.WAIT);
				this.dataPosition += 4;
			}
			break;
		}
	}

	protected void parseLabelAndMatchString(StormFrontLine line) {
		skipWhitespace();
		if (!hasMoreChars()) {
			throw new ParserException(this.lineCounter + ": MATCH statment must have a <label> and <match string>.");
		}

		// parse label
		int startPosition = this.dataPosition;
		skipToNextWhiteSpace();
		String label = new String(this.dataBuffer.data, startPosition, dataPosition - startPosition);

		// parse match string
		String matchString = getRestOfLine();

		// set arguments
		line.setArguments(new String[]{ label.toLowerCase(), matchString });
	}

	protected String getRestOfLine() {
		skipWhitespace();
		int startPosition = this.dataPosition;
		skipLine();
		String matchString = new String(this.dataBuffer.data, startPosition, this.dataPosition - startPosition);
		return matchString;
	}

	protected void skipToNextWhiteSpace() {
		boolean isWhiteSpace = false;
		while (!isWhiteSpace) {
			switch(this.dataBuffer.data[this.dataPosition]) {
			case '\r':
			case '\n':
			case ' ':
			case '\t':
				isWhiteSpace = true;
				break;
			default:
				this.dataPosition++;
				break;
			}
		}
	}

	protected void parseCounter(StormFrontLine line) {
		line.setCommand(StormFrontCommands.COUNTER);
		this.dataPosition += 8;

		// handle COUNTER {SET|ADD|SUBTRACT|MULTIPLY|DIVIDE}
		switch (this.dataBuffer.data[this.dataPosition]) {
		case 'a':
		case 'A':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'd' || this.dataBuffer.data[this.dataPosition + 1] == 'D')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'd' || this.dataBuffer.data[this.dataPosition + 2] == 'D')) {
				// ADD
				line.setSubCommand(StormFrontCommands.ADD);
				this.dataPosition += 3;
			}
			break;
		case 'd':
		case 'D':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'i' || this.dataBuffer.data[this.dataPosition + 1] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'v' || this.dataBuffer.data[this.dataPosition + 2] == 'V')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'i' || this.dataBuffer.data[this.dataPosition + 3] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'd' || this.dataBuffer.data[this.dataPosition + 4] == 'D')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'e' || this.dataBuffer.data[this.dataPosition + 5] == 'E')) {
				// DIVIDE
				line.setSubCommand(StormFrontCommands.DIVIDE);
				this.dataPosition += 6;
			}
			break;
		case 'm':
		case 'M':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'u' || this.dataBuffer.data[this.dataPosition + 1] == 'U')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'l' || this.dataBuffer.data[this.dataPosition + 2] == 'L')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'i' || this.dataBuffer.data[this.dataPosition + 4] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'p' || this.dataBuffer.data[this.dataPosition + 5] == 'P')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'l' || this.dataBuffer.data[this.dataPosition + 6] == 'L')
					&& (this.dataBuffer.data[this.dataPosition + 7] == 'y' || this.dataBuffer.data[this.dataPosition + 7] == 'Y')) {
				// MULTIPLY
				line.setSubCommand(StormFrontCommands.MULTIPLY);
				this.dataPosition += 8;
			}
			break;
		case 's':
		case 'S':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'e' || this.dataBuffer.data[this.dataPosition + 1] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')) {
				// SET
				line.setSubCommand(StormFrontCommands.SET);
				this.dataPosition += 3;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'u' || this.dataBuffer.data[this.dataPosition + 1] == 'U')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'b' || this.dataBuffer.data[this.dataPosition + 2] == 'B')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'r' || this.dataBuffer.data[this.dataPosition + 4] == 'R')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'a' || this.dataBuffer.data[this.dataPosition + 5] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'c' || this.dataBuffer.data[this.dataPosition + 6] == 'C')
					&& (this.dataBuffer.data[this.dataPosition + 7] == 't' || this.dataBuffer.data[this.dataPosition + 7] == 'T')) {
				// SUBTRACT
				line.setSubCommand(StormFrontCommands.SUBTRACT);
				this.dataPosition += 8;
			}
			break;
		}

		if (line.getSubCommand() == StormFrontCommands.NONE) {
			throw new ParserException(this.lineCounter + ": COUNTER must be used with one of (SET|ADD|SUBTRACT|MULTIPLY|DIVIDE).");
		}
	}

	protected void parseIf(StormFrontLine line) {
		line.setCommand(StormFrontCommands.IF_);
		this.dataPosition += 3;

		switch (this.dataBuffer.data[this.dataPosition]) {
		case '0':
			line.setN(0);
			break;
		case '1':
			line.setN(1);
			break;
		case '2':
			line.setN(2);
			break;
		case '3':
			line.setN(3);
			break;
		case '4':
			line.setN(4);
			break;
		case '5':
			line.setN(5);
			break;
		case '6':
			line.setN(6);
			break;
		case '7':
			line.setN(7);
			break;
		case '8':
			line.setN(8);
			break;
		case '9':
			line.setN(9);
			break;
		default:
			throw new ParserException(this.lineCounter + ": The value following IF_ must be a number between 1 and 9.");
		}

		this.dataPosition++;

		StormFrontLine subline = parseLine();
		line.setSubCommand(subline.getCommand());
		line.setArguments(subline.getArguments());
	}

	/**
	 * Split the remainder of the line into arguments based on whitespace.
	 */
	protected void parseArguments(StormFrontLine line) {
		if (this.dataBuffer.data[this.dataPosition] == ' ') {
			// ignore first space
			this.dataPosition++;
		}

		int tempPos = this.dataPosition;
		boolean inString = false;

		while (hasMoreChars() && !isEndOfLine()) {
			if (this.dataBuffer.data[this.dataPosition] == '"') {
				inString = !inString;
			}

			if (inString && this.dataBuffer.data[this.dataPosition] == ' ') {
				this.dataBuffer.data[this.dataPosition] = '_';
			}

			this.dataPosition++;
		}

		if (tempPos < this.dataPosition) {
			line.setArguments(new String(this.dataBuffer.data, tempPos, this.dataPosition - tempPos).split(" "));
		}
	}

	public boolean hasMoreChars() {
		return this.dataBuffer.data.length > this.dataPosition && this.dataBuffer.data[this.dataPosition] != '\0';
	}

	protected void skipWhitespace() {
		boolean isWhiteSpace = true;
		try {
			while (isWhiteSpace) {
				switch (this.dataBuffer.data[this.dataPosition]) {
				case '\r':
					if (this.dataBuffer.data[this.dataPosition + 1] == '\n') {
						// prevent double counting of a CRLF as 2 lines
						this.dataPosition++;
					}
					// fallthrough (no break!)
				case '\n':
					this.lineCounter++;
					// fallthrough (no break!)
				case ' ':
				case '\t':
					this.dataPosition++;
					break;
				default:
					isWhiteSpace = false; // any non white space char will break the while loop
				}
			}
		} catch (IndexOutOfBoundsException e) {
			// we reached EOF
		}
	}

	protected void skipComment() {
		while (!isEndOfLine()) {
			this.dataPosition++;
		}
	}

	protected void skipLine() {
		while (!isEndOfLine() && !(this.dataBuffer.data[this.dataPosition] == '#')) {
			this.dataPosition++;
		}
	}

	protected boolean isEndOfLine() {
		// EOF or # or CR or LF
		return !hasMoreChars() || this.dataBuffer.data[this.dataPosition] == '\r' || this.dataBuffer.data[this.dataPosition] == '\n';
	}
}
