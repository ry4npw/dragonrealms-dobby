package pw.ry4n.dr.engine.sf.parser;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.core.ParserException;
import pw.ry4n.dr.engine.sf.model.Commands;
import pw.ry4n.dr.engine.sf.model.Line;

/**
 * <p>
 * The line parser will parse each line in the format: <command> (<argument1> ...)
 * </p>
 *
 * @author Ryan Powell
 */
public class LineParser {
	private DataCharBuffer dataBuffer = null;

	private int dataPosition = 0;
	int lineCounter = 1;

	public LineParser(DataCharBuffer dataBuffer) {
		this.dataBuffer = dataBuffer;
	}

	public void reinit(DataCharBuffer dataBuffer) {
		this.dataBuffer = dataBuffer;
		this.dataPosition = 0;
	}

	public boolean hasMoreLines() {
		return this.dataBuffer.data.length > this.dataPosition;
	}

	public Line parseLine() {
		skipWhiteSpace();
		if (!hasMoreLines()) {
			return null;
		}

		Line line = new Line();

		switch (this.dataBuffer.data[this.dataPosition]) {
		case '#':
			// COMMENT
			line.setCommand(Commands.COMMENT);
			skipLine(); // skip comments
			break;
		case 'c':
		case 'C':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'o' || this.dataBuffer.data[this.dataPosition + 1] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'u' || this.dataBuffer.data[this.dataPosition + 2] == 'U')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'n' || this.dataBuffer.data[this.dataPosition + 3] == 'N')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 't' || this.dataBuffer.data[this.dataPosition + 4] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'e' || this.dataBuffer.data[this.dataPosition + 5] == 'E')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'r' || this.dataBuffer.data[this.dataPosition + 6] == 'R')) {
				line.setCommand(Commands.COUNTER);
				// TODO handle COUNTER {SET|ADD|SUBTRACT|MULTIPLY|DIVIDE}
				this.dataPosition += 7;
			}
			break;
		case 'e':
		case 'E':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'c' || this.dataBuffer.data[this.dataPosition + 1] == 'C')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'h' || this.dataBuffer.data[this.dataPosition + 2] == 'H')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'o' || this.dataBuffer.data[this.dataPosition + 3] == 'O')) {
				// ECHO
				line.setCommand(Commands.ECHO);
				this.dataPosition += 4;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'x' || this.dataBuffer.data[this.dataPosition + 1] == 'X')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'i' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')) {
				// EXIT
				line.setCommand(Commands.EXIT);
				this.dataPosition += 4;
			}
			break;
		case 'g':
		case 'G':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'o' || this.dataBuffer.data[this.dataPosition + 1] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'o' || this.dataBuffer.data[this.dataPosition + 3] == 'O')) {
				// GOTO
				line.setCommand(Commands.GOTO);
				this.dataPosition += 4;
			}
			break;
		case 'i':
		case 'I':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'f' || this.dataBuffer.data[this.dataPosition + 1] == 'F')
					&& this.dataBuffer.data[this.dataPosition + 2] == '_') {
				// IF_
				line.setCommand(Commands.IF_);
				this.dataPosition += 3;
			}
			break;
		case 'm':
		case 'M':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'o' || this.dataBuffer.data[this.dataPosition + 1] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'v' || this.dataBuffer.data[this.dataPosition + 2] == 'V')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'e' || this.dataBuffer.data[this.dataPosition + 3] == 'E')) {
				// MOVE
				line.setCommand(Commands.MOVE);
				this.dataPosition += 4;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'c' || this.dataBuffer.data[this.dataPosition + 3] == 'C')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'h' || this.dataBuffer.data[this.dataPosition + 4] == 'H')) {
				if ((this.dataBuffer.data[this.dataPosition + 5] == 'r' || this.dataBuffer.data[this.dataPosition + 5] == 'R')
						&& (this.dataBuffer.data[this.dataPosition + 6] == 'e' || this.dataBuffer.data[this.dataPosition + 6] == 'E')) {
					// MATCHRE
					line.setCommand(Commands.MATCHRE);
					this.dataPosition += 7;
				} else if ((this.dataBuffer.data[this.dataPosition + 5] == 'w' || this.dataBuffer.data[this.dataPosition + 5] == 'W')
						&& (this.dataBuffer.data[this.dataPosition + 6] == 'a' || this.dataBuffer.data[this.dataPosition + 6] == 'A')
						&& (this.dataBuffer.data[this.dataPosition + 7] == 'i' || this.dataBuffer.data[this.dataPosition + 7] == 'I')
						&& (this.dataBuffer.data[this.dataPosition + 8] == 't' || this.dataBuffer.data[this.dataPosition + 8] == 'T')) {
					// MATCHWAIT
					line.setCommand(Commands.MATCHWAIT);
					this.dataPosition += 9;
				} else if (this.dataBuffer.data[this.dataPosition + 5] == ' ') {
					// MATCH
					line.setCommand(Commands.MATCH);
					this.dataPosition += 5;
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
				line.setCommand(Commands.NEXTROOM);
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
				line.setCommand(Commands.PAUSE);
				this.dataPosition += 5;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'u' || this.dataBuffer.data[this.dataPosition + 1] == 'U')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')) {
				// PUT
				line.setCommand(Commands.PUT);
				this.dataPosition += 3;
			}
			break;
		case 's':
		case 'S':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'v' || this.dataBuffer.data[this.dataPosition + 2] == 'V')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'e' || this.dataBuffer.data[this.dataPosition + 3] == 'E')) {
				// SAVE
				line.setCommand(Commands.SAVE);
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
					&& (this.dataBuffer.data[this.dataPosition + 10] == 'e' || this.dataBuffer.data[this.dataPosition + 10] == 'E')) {
				// SETVARIABLE
				line.setCommand(Commands.SETVARIABLE);
				this.dataPosition += 11;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'h' || this.dataBuffer.data[this.dataPosition + 1] == 'H')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'i' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'f' || this.dataBuffer.data[this.dataPosition + 3] == 'F')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 't' || this.dataBuffer.data[this.dataPosition + 4] == 'T')) {
				// SHIFT
				line.setCommand(Commands.SHIFT);
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
					&& (this.dataBuffer.data[this.dataPosition + 8] == 'e' || this.dataBuffer.data[this.dataPosition + 8] == 'E')) {
				// WAITFORRE
				line.setCommand(Commands.WAITFORRE);
				this.dataPosition += 9;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'i' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'f' || this.dataBuffer.data[this.dataPosition + 4] == 'F')
					&& (this.dataBuffer.data[this.dataPosition + 5] == 'o' || this.dataBuffer.data[this.dataPosition + 5] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 6] == 'r' || this.dataBuffer.data[this.dataPosition + 6] == 'R')) {
				// WAITFORRE
				line.setCommand(Commands.WAITFORRE);
				this.dataPosition += 9;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'i' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')) {
				// WAIT
				line.setCommand(Commands.WAIT);
				this.dataPosition += 4;
			}
		}

		parseArguments(line);

		if (line.getCommand() == Commands.NOOP
				&& line.getArguments() != null
				&& line.getArguments().length > 0
				&& line.getArguments()[0].endsWith(":")) {
			// LABEL:
			line.setCommand(Commands.LABEL);
			String label = line.getArguments()[0];
			line.getArguments()[0] = label.substring(0, label.length() - 1);
		}

		if (line.getCommand() == Commands.NOOP) {
			// ParserException
			throw new ParserException("Unrecognized command on line " + this.lineCounter);
		}

		return line;
	}

	/**
	 * Split the remainder of the line into arguments based on whitespace.
	 */
	void parseArguments(Line line) {
		if (this.dataBuffer.data[this.dataPosition] == ' ') {
			// ignore first space
			this.dataPosition++;
		}

		int tempPos = this.dataPosition;
		boolean inString = false;

		while (!isEndOfLine()) {
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

	void skipWhiteSpace() {
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

	void skipLine() {
		while (!isEndOfLine()) {
			this.dataPosition++;
		}
	}

	boolean isEndOfLine() {
		// EOF or CR or LF
		return this.dataBuffer.data.length <= this.dataPosition || this.dataBuffer.data[this.dataPosition] == '\r' || this.dataBuffer.data[this.dataPosition] == '\n';
	}
}