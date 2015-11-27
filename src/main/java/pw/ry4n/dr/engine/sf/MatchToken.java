package pw.ry4n.dr.engine.sf;

public class MatchToken {
	public static final byte STRING = 1;
	public static final byte REGEX = 2;

	private byte type;
	private String matchString;

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getMatchString() {
		return matchString;
	}

	public void setMatchString(String matchString) {
		this.matchString = matchString;
	}
}
