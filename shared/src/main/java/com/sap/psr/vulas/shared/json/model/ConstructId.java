package com.sap.psr.vulas.shared.json.model;


import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstructId implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private Long id;

	private ProgrammingLanguage lang;

	private ConstructType type;

	private String qname;

	public ConstructId() { super(); }

	public ConstructId(ProgrammingLanguage lang, ConstructType type, String qname) {
		super();
		this.lang = lang;
		this.type = type;
		this.qname = qname;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; } 

	public ProgrammingLanguage getLang() { return lang; }
	public void setLang(ProgrammingLanguage lang) { this.lang = lang; }

	public ConstructType getType() { return type; }
	public void setType(ConstructType type) { this.type = type; }

	public String getQname() { return qname; }
	public void setQname(String qname) { this.qname = qname; }

	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[").append(this.getId()).append(":").append(this.getLang()).append("|").append(this.getType()).append("|").append(this.getQname()).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lang == null) ? 0 : lang.hashCode());
		result = prime * result + ((qname == null) ? 0 : qname.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ConstructId other = (ConstructId) obj;
		if (lang == null) {
			if (other.lang != null)
				return false;
		} else if (!lang.equals(other.lang))
			return false;
		if (qname == null) {
			if (other.qname != null)
				return false;
		} else if (!qname.equals(other.qname))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public int compareTo(Object _other) {
		if(_other==null || !(_other instanceof ConstructId))
			throw new IllegalArgumentException();
		return this.getQname().compareTo(((ConstructId)_other).getQname());
	}
	
	//-------------------------------------------------------- On top of core class
	
	/**
	 * Models relationships between constructs, e.g., parent classes, implemented interfaces or annotations.
	 * The type of relationship is modeled by the key of type {@link String}.
	 */
	private transient Map<String, Set<ConstructId>> relates = null;
	
	/**
	 * Models construct attributes (modifiers) such as visibility, finalization, etc.
	 */
	private transient Map<String,String> attributes = null; // (public,private,package-private,protected),static,final,synchronized,abstract,deprecated,synthetic,strict,native,super
	
	public Map<String, Set<ConstructId>> getRelates() { return relates; }
	public void addRelates(String _key, ConstructId _cid) {
		if(this.relates==null)
			this.relates = new HashMap<String, Set<ConstructId>>();
		Set<ConstructId> cids = this.relates.get(_key);
		if(cids==null) {
			cids = new HashSet<ConstructId>();
			this.relates.put(_key, cids);
		}
		cids.add(_cid);
	}
	public void setRelates(Map<String, Set<ConstructId>> relates) { this.relates = relates; }

	public Map<String,String> getAttributes() { return attributes; }
	public void addAttribute(String _key, String _value) {
		if(this.attributes==null)
			this.attributes = new HashMap<String,String>();
		this.attributes.put(_key, _value);
	}
	public void addAttribute(String _key, boolean _value) {
		this.addAttribute(_key, Boolean.toString(_value));
	}
	public boolean hasAttribute(String _key) {
		return this.attributes!=null && this.attributes.containsKey(_key);
	}
	public boolean isAttributeTrue(String _key) { 
		return this.hasAttribute(_key) && Boolean.valueOf(this.attributes.get(_key)).booleanValue();
	}
	public void setAttributes(Map<String,String> attributes) { this.attributes = attributes; }
}