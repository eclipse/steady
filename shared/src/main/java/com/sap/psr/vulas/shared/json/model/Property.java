package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.PropertySource;
import com.sap.psr.vulas.shared.util.DigestUtil;

/**
 * General purpose property referenced by all kinds of entities, e.g., {@link Library}s.
 * It is not dependent on (contained in) other entities but saved and created independently
 * in order to be able to have the same property referenced by multiple other entities.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "valueSha1" })
public class Property implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private Long id;

	private PropertySource source;

	private String name;
	
	private String value;
	
	@JsonIgnore
	private String valueSha1;
	
	public Property() { super(); }
	
	public Property(PropertySource source, String _name, String _value) {
		super();
		this.source = source;
		this.name = _name;
		this.value = _value;
		this.valueSha1 = DigestUtil.getDigestAsString(this.value, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
	}
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public PropertySource getSource() { return source; }
	public void setSource(PropertySource source) { this.source = source; }

	public String getName() { return name; }
	public void setName(String _name) { this.name = _name; }

	public String getValue() { return value; }
	public void setValue(String _value) {
		this.value = _value;
		this.valueSha1 = DigestUtil.getDigestAsString(this.value, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
	}

	public String getValueSha1() { return valueSha1; }
	public void setValueSha1(String valueSha1) { this.valueSha1 = valueSha1; }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((valueSha1 == null) ? 0 : valueSha1.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Property other = (Property) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (source != other.source)
			return false;
		if (valueSha1 == null) {
			if (other.valueSha1 != null)
				return false;
		} else if (!valueSha1.equals(other.valueSha1))
			return false;
		return true;
	}
}
