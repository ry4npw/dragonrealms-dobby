package pw.ry4n.dr.engine.parser;

import pw.ry4n.dr.engine.core.DataCharBuffer;

/**
 * split on ", then _ or <space>
 * 
 * @author Ryan Powell
 */
public class FileTokenizer {
	private DataCharBuffer dataBuffer = null;
	private IndexBuffer tokenBuffer = null;

	private int tokenIndex = 0;
	private int dataPosition = 0;
	private int tokenLength = 0;

	public FileTokenizer(IndexBuffer tokenBuffer) {
        this.tokenBuffer = tokenBuffer;
    }

	public FileTokenizer(DataCharBuffer dataBuffer, IndexBuffer tokenBuffer) {
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

	public void parseToken() {
		skipWhiteSpace();
		// this.tokenLength = 0;

		this.tokenBuffer.position[this.tokenIndex] = this.dataPosition;
		char nextChar = this.dataBuffer.data[this.dataPosition];

		switch (nextChar) {

		}

		this.tokenBuffer.length[this.tokenIndex] = this.tokenLength;
	}



	private void skipWhiteSpace() {
		boolean isWhiteSpace = true;
		while (isWhiteSpace) {
			switch (this.dataBuffer.data[this.dataPosition]) {
			case ' ':
				; /*
					 * falling through - all white space characters are treated
					 * the same
					 */
			case '\r':
				;
			case '\n':
				;
			case '\t': {
				this.dataPosition++;
			}
				break;

			default: {
				isWhiteSpace = false;
			} /* any non white space char will break the while loop */
			}
		}
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
