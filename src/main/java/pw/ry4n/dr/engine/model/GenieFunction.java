package pw.ry4n.dr.engine.model;

import java.util.regex.Pattern;

public class GenieFunction extends GenieExpression {
	// contains(source text, search text)
	// count(source text, search text)
	// element(source text, index)
	// endswith(match text)
	// ￼indexof(source text, search text)
	// instr - see contains()
	// instring - see contains()
	// lastindexof(source text, search text)
	// len(source text)
	// length - see len()

	/**
	 * match(source text, match text)
	 * 
	 * @param sourceText
	 * @param matchText
	 * @return
	 */
	public boolean match(String sourceText, String matchText) {
		return sourceText == null ? null : sourceText.contains(matchText);
	}

	/**
	 * matchre(source text, pattern)
	 * 
	 * @param sourceText
	 * @param pattern
	 * @return
	 */
	public boolean matchre(String sourceText, String pattern) {
		return Pattern.compile(pattern).matcher(sourceText).find();
	}

	/**
	 * replace(source text, replace text)
	 * 
	 * @param sourceText
	 * @param replaceText
	 * @return
	 */
	public String replace(String sourceText, String replaceText) {
		return sourceText.replace(sourceText, replaceText);
	}

	/**
	 * replacere(source text, pattern, replacement text)
	 * 
	 * @param sourceText
	 * @param pattern
	 * @param replaceText
	 * @return
	 */
	public String replacere(String sourceText, String pattern, String replaceText) {
		if (sourceText == null || pattern == null || replaceText == null) {
			return null;
		}

		return Pattern.compile(pattern).matcher(sourceText).replaceAll(replaceText);
	}

	// startswith(match text)
	// substr(source text, start index, end index)
	// substring - see substr()
	// tolower(source text)
	// toupper(source text)
	// trim(source text)
}
