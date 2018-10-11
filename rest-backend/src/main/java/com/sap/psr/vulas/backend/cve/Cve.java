package com.sap.psr.vulas.backend.cve;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Used for (de)serializing the JSON read from circl.lu
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Cve {
	
	public static final String CVE_REGEX = "(CVE-\\d{4}+-\\d{4,}).*";
	public static final Pattern CVE_PATTERN = Pattern.compile(CVE_REGEX);
	
	@JsonProperty(value = "id")
	private String id;
	
	@JsonProperty(value = "Published")
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone="GMT")
	private java.util.Calendar published;
	
	@JsonProperty(value = "Modified")
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone="GMT")
	private java.util.Calendar modified;
	
	@JsonProperty(value = "cvss")
	private Float cvssScore = null;
	
	@JsonProperty(value = "cvssVersion")
	private String cvssVersion = null;
	
	@JsonProperty(value = "cvssVector")
	private String cvssVector = null;
	
	@JsonProperty(value = "summary")
	private String summary;
	
	public Cve() { super(); }
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public java.util.Calendar getPublished() { return published; }
	public void setPublished(java.util.Calendar published) { this.published = published; }

	public java.util.Calendar getModified() { return modified; }
	public void setModified(java.util.Calendar modified) { this.modified = modified; }

	public Float getCvssScore() { return cvssScore; }
	public void setCvssScore(Float cvss) { this.cvssScore = cvss; }

	public String getCvssVersion() { return cvssVersion; }
	public void setCvssVersion(String cvssVersion) { this.cvssVersion = cvssVersion; }
	
	public String getCvssVector() { return cvssVector; }
	public void setCvssVector(String cvssVector) { this.cvssVector = cvssVector; }

	public String getSummary() { return summary; }
	public void setSummary(String summary) { this.summary = summary; }
	
	@Override
	public String toString() {
		final StringBuffer b = new StringBuffer();
		b.append("[id=").append(this.getId()).append(", cvss=").append(this.getCvssScore()).append("]");
		return b.toString();
	}
	
	/**
	 * Uses {@link Cve#CVE_REGEX} to extract a CVE identifier from the given {@link String}.
	 * Returns null if no such identifier can be found.
	 * 
	 * @param _string
	 * @return
	 */
	public static final String extractCveIdentifier(String _string) {
		if(_string==null)
			return null;
		final Matcher m = CVE_PATTERN.matcher(_string.toUpperCase());
		if(m.matches())
			return m.group(1);
		else
			return null;
	}
}