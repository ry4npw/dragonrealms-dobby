package pw.ry4n.dr.engine.model;

import java.util.regex.Pattern;

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
		setMatchString(matchString);
	}

	public MatchToken(byte type, String label, String matchString) {
		this.type = type;
		this.label = label;
		setMatchString(matchString);
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
		if (matchString.startsWith("/")) {
			// In StormFront, regex groups are implied. This is a workaround.
			matchString = matchString.replaceAll("^/","(").replaceAll("/$", ")").replaceAll("/i$", ")?i");
		}
		this.matchString = matchString.replace("\\" + "?", "?");
	}

	public boolean match(String line) {
		if (REGEX == type) {
			return Pattern.compile(matchString).matcher(line).find();
		} else {
			return line.contains(matchString);
		}
	}
}
