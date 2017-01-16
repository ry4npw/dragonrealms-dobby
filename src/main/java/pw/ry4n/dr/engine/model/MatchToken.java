package pw.ry4n.dr.engine.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchToken {
	public static final byte STRING = 1;
	public static final byte REGEX = 2;

	private byte type;
	private String label;
	private String matchString;
	private Matcher matcher;

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
			matchString = matchString.replaceAll("^/", "(").replaceAll("/$", ")").replaceAll("/i$", ")?i");
			if (matchString.endsWith(")")) {
				matchString = matchString.substring(1, matchString.length()-1);
			}
		}
		this.matchString = matchString.replace("\\" + "?", "?");
	}

	/**
	 * Returns the full input subsequence matched by the last {@link #match()}.
	 * 
	 * @return The input sequence matched.
	 * @see Matcher#group()
	 */
	public String getGroup() {
		return getGroup(0);
	}

	/**
	 * Returns the input subsequence captured by the given group during the
	 * previous {@link #match()}.
	 * 
	 * @param group
	 *            integer identifier of the group you want.
	 * @return The group corresponding to the matched string.
	 * @see Matcher#group(int)
	 */
	public String getGroup(int group) {
		if (matcher != null) {
			return matcher.group(group);
		}
		return null;
	}

	public boolean match(String line) {
		if (REGEX == type) {
			Matcher matcher = Pattern.compile(matchString).matcher(line);
			if (matcher.find()) {
				this.matcher = matcher;
				return true;
			}
			return false;
		} else {
			return line.contains(matchString);
		}
	}
}
