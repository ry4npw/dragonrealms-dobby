package pw.ry4n.dr.engine.parser;

import pw.ry4n.dr.engine.core.DataCharBuffer;
import pw.ry4n.dr.engine.model.Line;

/**
 * split on ", then _ or <space>
 * 
 * @author Ryan Powell
 */
public class LineParser {
	private DataCharBuffer dataBuffer = null;
	private IndexBuffer tokenBuffer = null;

	Line line = new Line();

	private int tokenIndex = 0;
	private int dataPosition = 0;
	private int tokenLength = 0;

	public LineParser(IndexBuffer tokenBuffer) {
		this.tokenBuffer = tokenBuffer;
	}

	public LineParser(DataCharBuffer dataBuffer, IndexBuffer tokenBuffer) {
		this.dataBuffer = dataBuffer;
		this.tokenBuffer = tokenBuffer;
	}

	public void reinit(DataCharBuffer dataBuffer, IndexBuffer tokenBuffer) {
		this.dataBuffer = dataBuffer;
		this.tokenBuffer = tokenBuffer;
		this.tokenIndex = 0;
		this.dataPosition = 0;
		this.tokenLength = 0;
	}

	public boolean hasMoreTokens() {
		return (this.dataPosition + this.tokenLength) < this.dataBuffer.length;
	}

	public void parseLine() {
		skipWhiteSpace();

		this.line = new Line();

		this.tokenBuffer.position[this.tokenIndex] = this.dataPosition;
		char nextChar = this.dataBuffer.data[this.dataPosition];

		switch (nextChar) {
		case '#':
			// COMMENT
			this.line.setCommand(TokenTypes.COMMENT);
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
				// COUNTER {SET|ADD|SUBTRACT|MULTIPLY|DIVIDE}
				this.line.setCommand(TokenTypes.COUNTER);
				this.dataPosition += 7;
			}
			break;
		case 'e':
		case 'E':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'c' || this.dataBuffer.data[this.dataPosition + 1] == 'C')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'h' || this.dataBuffer.data[this.dataPosition + 2] == 'H')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'o' || this.dataBuffer.data[this.dataPosition + 3] == 'O')) {
				// ECHO
				this.line.setCommand(TokenTypes.ECHO);
				this.dataPosition += 4;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'x' || this.dataBuffer.data[this.dataPosition + 1] == 'X')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'i' || this.dataBuffer.data[this.dataPosition + 2] == 'I')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 't' || this.dataBuffer.data[this.dataPosition + 3] == 'T')) {
				// EXIT
				this.line.setCommand(TokenTypes.EXIT);
				this.dataPosition += 4;
			}
			break;
		case 'g':
		case 'G':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'o' || this.dataBuffer.data[this.dataPosition + 1] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'o' || this.dataBuffer.data[this.dataPosition + 3] == 'O')) {
				// GOTO
				this.tokenBuffer.type[this.tokenIndex] = TokenTypes.GOTO;
				this.dataPosition += 4;
			}
			break;
		case 'i':
		case 'I':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'f' || this.dataBuffer.data[this.dataPosition + 1] == 'F')
					&& this.dataBuffer.data[this.dataPosition + 2] == '_') {
				// IF_
				this.tokenBuffer.type[this.tokenIndex] = TokenTypes.IF_;
				this.dataPosition += 3;
			}
			break;
		case 'm':
		case 'M':
			if ((this.dataBuffer.data[this.dataPosition + 1] == 'o' || this.dataBuffer.data[this.dataPosition + 1] == 'O')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 'v' || this.dataBuffer.data[this.dataPosition + 2] == 'V')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'e' || this.dataBuffer.data[this.dataPosition + 3] == 'E')) {
				// MOVE
				this.tokenBuffer.type[this.tokenIndex] = TokenTypes.MOVE;
				this.dataPosition += 4;
			} else if ((this.dataBuffer.data[this.dataPosition + 1] == 'a' || this.dataBuffer.data[this.dataPosition + 1] == 'A')
					&& (this.dataBuffer.data[this.dataPosition + 2] == 't' || this.dataBuffer.data[this.dataPosition + 2] == 'T')
					&& (this.dataBuffer.data[this.dataPosition + 3] == 'c' || this.dataBuffer.data[this.dataPosition + 3] == 'C')
					&& (this.dataBuffer.data[this.dataPosition + 4] == 'h' || this.dataBuffer.data[this.dataPosition + 4] == 'H')) {
				if ((this.dataBuffer.data[this.dataPosition + 5] == 'r' || this.dataBuffer.data[this.dataPosition + 5] == 'R')
						&& (this.dataBuffer.data[this.dataPosition + 6] == 'e' || this.dataBuffer.data[this.dataPosition + 6] == 'E')) {
					// MATCHRE
					line.setCommand(TokenTypes.MATCHRE);
					this.dataPosition += 7;
				} else if ((this.dataBuffer.data[this.dataPosition + 5] == 'w' || this.dataBuffer.data[this.dataPosition + 5] == 'W')
						&& (this.dataBuffer.data[this.dataPosition + 6] == 'a' || this.dataBuffer.data[this.dataPosition + 6] == 'A')
						&& (this.dataBuffer.data[this.dataPosition + 7] == 'i' || this.dataBuffer.data[this.dataPosition + 7] == 'I')
						&& (this.dataBuffer.data[this.dataPosition + 8] == 't' || this.dataBuffer.data[this.dataPosition + 8] == 'T')) {
					// MATCHWAIT
					line.setCommand(TokenTypes.MATCHWAIT);
					this.dataPosition += 9;
				} else if (this.dataBuffer.data[this.dataPosition + 5] == ' ') {
					// MATCH
					line.setCommand(TokenTypes.MATCH);
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
				line.setCommand(TokenTypes.NEXTROOM);
				this.dataPosition += 8;
			}
			break;
		default:
			// ParseException
			break;
		}

		/*
		 * PAUSE PUT SAVE SETVARIABLE SHIFT WAIT WAITFORRE WATIFOR
		 */
	//}

	parseArguments();

	//this.tokenBuffer.length[this.tokenIndex]=this.tokenLength;

	}

	/**
	 * A recursive argument parser that will split the remainder of the line into words.
	 */
	void parseArguments() {
		if (isEndOfLine()) {
			return;
		}

		if (this.dataBuffer.data[this.dataPosition] == ' ' || this.dataBuffer.data[this.dataPosition] == '\t') {
			this.dataPosition++;
			parseArguments();
			return;
		}

		boolean isWhiteSpace = false;
		int tempPos = this.dataPosition; 

		// skip until we hit whitespace again
		while (!isWhiteSpace) {
			switch (this.dataBuffer.data[this.dataPosition]) {
			case ' ':
			case '\r':
			case '\n':
			case '\t':
				isWhiteSpace = true;
				break;
			default:
				this.dataPosition++;
				break;
			}
		}

		System.out.println("dataPosition: " + this.dataPosition);

		if (this.dataPosition > tempPos + 1) {
			this.line.getArguments().add(new String(this.dataBuffer.data, tempPos, this.dataPosition - tempPos));
		}

		// recursive call
		parseArguments();
	}

	private void skipWhiteSpace() {
		boolean isWhiteSpace = true;
		while (isWhiteSpace) {
			switch (this.dataBuffer.data[this.dataPosition]) {
			case ' ':
			case '\r':
			case '\n':
			case '\t':
				this.dataPosition++;
				break;
			default:
				isWhiteSpace = false; // any non white space char will break the while loop
			}
		}
	}

	void skipLine() {
		while (!isEndOfLine()) {
			this.dataPosition++;
		}
	}

	boolean isEndOfLine() {
		return this.dataBuffer.data[this.dataPosition] == '\r' || this.dataBuffer.data[this.dataPosition] == '\n';
	}

	public void nextToken() {
		switch (this.tokenBuffer.type[this.tokenIndex]) {

		default: {
			this.dataPosition += this.tokenLength;
		}
		}
		// this.dataPosition += this.tokenBuffer.length[this.tokenIndex]; //move
		// data position to end of current token.
		this.tokenIndex++; // point to next token index array cell.
	}

	public int tokenPosition() {
		return this.tokenBuffer.position[this.tokenIndex];
	}

	public int tokenLength() {
		return this.tokenBuffer.length[this.tokenIndex];
	}

	public byte tokenType() {
		return this.tokenBuffer.type[this.tokenIndex];
	}
}
