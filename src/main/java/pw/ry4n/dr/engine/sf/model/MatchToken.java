package pw.ry4n.dr.engine.sf.model;

public class MatchToken {
	public static final byte STRING = 1;
	public static final byte REGEX = 2;

	private byte type;
	private String label;
	private String matchString;

	public MatchToken() {
		// empty constructor
	}

	public MatchToken(byte type, String matchString) {
		this.type = type;
		this.matchString = matchString;
	}

	public MatchToken(byte type, String label, String matchString) {
		this.type = type;
		this.label = label;
		this.matchString = matchString;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getMatchString() {
		return matchString;
	}

	public void setMatchString(String matchString) {
		this.matchString = matchString;
	}

	public boolean match(String line) {
		if (REGEX == type) {
			return line.matches(matchString);
		} else {
			return line.contains(matchString);
		}
	}
}
