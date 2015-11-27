package pw.ry4n.dr.engine.parser;

import pw.ry4n.dr.engine.core.DataCharBuffer;

/**
 * The purpose of this class is to take a File and parse it into a Program.
 * 
 * @author Ryan Powell
 */
public class FileParser {
	private IndexBuffer tokenBuffer = null;
	private IndexBuffer elementBuffer = null;
	private int elementIndex = 0;
	private FileTokenizer jsonTokenizer = null;

	public FileParser(IndexBuffer tokenBuffer, IndexBuffer elementBuffer) {
		this.tokenBuffer = tokenBuffer;
		this.jsonTokenizer = new FileTokenizer(this.tokenBuffer);
		this.elementBuffer = elementBuffer;
	}

	public void parse(DataCharBuffer dataBuffer) {
		this.elementIndex = 0;

		this.jsonTokenizer.reinit(dataBuffer, this.tokenBuffer);

		parseObject(this.jsonTokenizer);

		this.elementBuffer.count = this.elementIndex;
	}

	private void parseObject(FileTokenizer tokenizer) {
		assertHasMoreTokens(tokenizer);
		tokenizer.parseToken();
		// assertThisTokenType(tokenizer.tokenType(),
		// TokenTypes.JSON_CURLY_BRACKET_LEFT);
		// setElementData(tokenizer, ElementTypes.JSON_OBJECT_START);

		tokenizer.nextToken();
		tokenizer.parseToken();
		byte tokenType = tokenizer.tokenType();

		while (tokenizer.hasMoreTokens()) {
			// assertThisTokenType(tokenType, TokenTypes.JSON_STRING_TOKEN);
			// setElementData(tokenizer, ElementTypes.JSON_PROPERTY_NAME);

			tokenizer.nextToken();
			tokenizer.parseToken();
			tokenType = tokenizer.tokenType();
			// assertThisTokenType(tokenType, TokenTypes.JSON_COLON);

			tokenizer.nextToken();
			tokenizer.parseToken();
			tokenType = tokenizer.tokenType();

			switch (tokenType) {
			// case TokenTypes.JSON_STRING_TOKEN : { setElementData(tokenizer,
			// ElementTypes.JSON_PROPERTY_VALUE_STRING); } break;
			// case TokenTypes.JSON_STRING_ENC_TOKEN : {
			// setElementData(tokenizer,
			// ElementTypes.JSON_PROPERTY_VALUE_STRING_ENC);} break;
			// case TokenTypes.JSON_NUMBER_TOKEN : { setElementData(tokenizer,
			// ElementTypes.JSON_PROPERTY_VALUE_NUMBER); } break;
			// case TokenTypes.JSON_BOOLEAN_TOKEN : { setElementData(tokenizer,
			// ElementTypes.JSON_PROPERTY_VALUE_BOOLEAN); } break;
			// case TokenTypes.JSON_NULL_TOKEN : { setElementData(tokenizer,
			// ElementTypes.JSON_PROPERTY_VALUE_NULL); } break;
			// case TokenTypes.JSON_CURLY_BRACKET_LEFT : {
			// parseObject(tokenizer); } break;
			// case TokenTypes.JSON_SQUARE_BRACKET_LEFT : {
			// parseArray(tokenizer); } break;
			}

			tokenizer.nextToken();
			tokenizer.parseToken();
			tokenType = tokenizer.tokenType();
			// if(tokenType == TokenTypes.JSON_COMMA) {
			// tokenizer.nextToken(); //skip , tokens if found here.
			// tokenizer.parseToken();
			// tokenType = tokenizer.tokenType();
			// }

		}
		// setElementData(tokenizer, ElementTypes.JSON_OBJECT_END);
	}

	private void setElementData(FileTokenizer tokenizer, byte elementType) {
		this.elementBuffer.position[this.elementIndex] = tokenizer.tokenPosition();
		this.elementBuffer.length[this.elementIndex] = tokenizer.tokenLength();
		this.elementBuffer.type[this.elementIndex] = elementType;
		this.elementIndex++;
	}

	private final void assertThisTokenType(byte tokenType, byte expectedTokenType) {
		if (tokenType != expectedTokenType) {
			throw new ParserException("Token type mismatch: Expected " + expectedTokenType + " but found " + tokenType);
		}
	}

	private void assertHasMoreTokens(FileTokenizer tokenizer) {
		if (!tokenizer.hasMoreTokens()) {
			throw new ParserException("Expected more tokens available in the tokenizer");
		}
	}
}
