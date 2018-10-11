package com.sap.psr.vulas.backend.requests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.psr.vulas.backend.HttpResponse;

public class ContentCondition implements ResponseCondition {

	public enum Mode { MATCH, EQ_STRING, LT_DOUBLE, GT_DOUBLE }

	private String regex = null;

	private Mode mode = Mode.MATCH;

	private String value = null;

	private Pattern pattern = null; //Pattern.compile("\\\"constructCounter\\\"\\s*:\\s*[\\d]*");

	public ContentCondition(String _regex, Mode _mode, String _value) {
		this.regex = _regex; 
		this.pattern = Pattern.compile(this.regex);
		this.mode = _mode;
		this.value = _value;
	}

	/**
	 * Returns true if the content of the given {@link HttpResponse} matches the regular expression of the condition, false otherwise.
	 * @param _response
	 * @return
	 */
	@Override
	public boolean meetsCondition(HttpResponse _response) {
		if(_response==null || !_response.hasBody())
			return false;

		boolean meets = false;
		final Matcher m = pattern.matcher(_response.getBody());
		if(this.mode.equals(Mode.MATCH)) {
			meets = m.matches();
		}
		else if(this.mode.equals(Mode.EQ_STRING)) {
			meets = m.find() && this.value.equals(m.group(1));
		}
		else if(this.mode.equals(Mode.LT_DOUBLE)) {
			if(m.find()) {
				double actual = Double.parseDouble(m.group(1));
				meets = actual < Double.parseDouble(this.value);
			}
		}
		else if(this.mode.equals(Mode.GT_DOUBLE)) {
			if(m.find()) {
				double actual = Double.parseDouble(m.group(1));
				meets = actual > Double.parseDouble(this.value);
			}
		}

		return meets;
	}

	public String toString() { return "[body " + this.mode + " " + this.value + "]"; }
}
