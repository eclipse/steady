package com.sap.psr.vulas.shared.json.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** ConstructId class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstructId implements Serializable, Comparable {

  private static final long serialVersionUID = 1L;

  @JsonIgnore private Long id;

  private ProgrammingLanguage lang;

  private ConstructType type;

  private String qname;

  /** Constructor for ConstructId. */
  public ConstructId() {
    super();
  }

  /**
   * Constructor for ConstructId.
   *
   * @param lang a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
   * @param type a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
   * @param qname a {@link java.lang.String} object.
   */
  public ConstructId(ProgrammingLanguage lang, ConstructType type, String qname) {
    super();
    this.lang = lang;
    this.type = type;
    this.qname = qname;
  }

  /**
   * Getter for the field <code>id</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getId() {
    return id;
  }
  /**
   * Setter for the field <code>id</code>.
   *
   * @param id a {@link java.lang.Long} object.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Getter for the field <code>lang</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
   */
  public ProgrammingLanguage getLang() {
    return lang;
  }
  /**
   * Setter for the field <code>lang</code>.
   *
   * @param lang a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
   */
  public void setLang(ProgrammingLanguage lang) {
    this.lang = lang;
  }

  /**
   * Getter for the field <code>type</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
   */
  public ConstructType getType() {
    return type;
  }
  /**
   * Setter for the field <code>type</code>.
   *
   * @param type a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
   */
  public void setType(ConstructType type) {
    this.type = type;
  }

  /**
   * Getter for the field <code>qname</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getQname() {
    return qname;
  }
  /**
   * Setter for the field <code>qname</code>.
   *
   * @param qname a {@link java.lang.String} object.
   */
  public void setQname(String qname) {
    this.qname = qname;
  }

  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public final String toString() {
    final StringBuilder builder = new StringBuilder();
    builder
        .append("[")
        .append(this.getId())
        .append(":")
        .append(this.getLang())
        .append("|")
        .append(this.getType())
        .append("|")
        .append(this.getQname())
        .append("]");
    return builder.toString();
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((lang == null) ? 0 : lang.hashCode());
    result = prime * result + ((qname == null) ? 0 : qname.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ConstructId other = (ConstructId) obj;
    if (lang == null) {
      if (other.lang != null) return false;
    } else if (!lang.equals(other.lang)) return false;
    if (qname == null) {
      if (other.qname != null) return false;
    } else if (!qname.equals(other.qname)) return false;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public int compareTo(Object _other) {
    if (_other == null || !(_other instanceof ConstructId)) throw new IllegalArgumentException();
    return this.getQname().compareTo(((ConstructId) _other).getQname());
  }

  // -------------------------------------------------------- On top of core class

  /**
   * Models relationships between constructs, e.g., parent classes, implemented interfaces or
   * annotations. The type of relationship is modeled by the key of type {@link String}.
   */
  private transient Map<String, Set<ConstructId>> relates = null;

  /** Models construct attributes (modifiers) such as visibility, finalization, etc. */
  private transient Map<String, String> attributes =
      null; // (public,private,package-private,protected),static,final,synchronized,abstract,deprecated,synthetic,strict,native,super

  /**
   * Getter for the field <code>relates</code>.
   *
   * @return a {@link java.util.Map} object.
   */
  public Map<String, Set<ConstructId>> getRelates() {
    return relates;
  }
  /**
   * addRelates.
   *
   * @param _key a {@link java.lang.String} object.
   * @param _cid a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void addRelates(String _key, ConstructId _cid) {
    if (this.relates == null) this.relates = new HashMap<String, Set<ConstructId>>();
    Set<ConstructId> cids = this.relates.get(_key);
    if (cids == null) {
      cids = new HashSet<ConstructId>();
      this.relates.put(_key, cids);
    }
    cids.add(_cid);
  }
  /**
   * Setter for the field <code>relates</code>.
   *
   * @param relates a {@link java.util.Map} object.
   */
  public void setRelates(Map<String, Set<ConstructId>> relates) {
    this.relates = relates;
  }

  /**
   * Getter for the field <code>attributes</code>.
   *
   * @return a {@link java.util.Map} object.
   */
  public Map<String, String> getAttributes() {
    return attributes;
  }
  /**
   * addAttribute.
   *
   * @param _key a {@link java.lang.String} object.
   * @param _value a {@link java.lang.String} object.
   */
  public void addAttribute(String _key, String _value) {
    if (this.attributes == null) this.attributes = new HashMap<String, String>();
    this.attributes.put(_key, _value);
  }
  /**
   * addAttribute.
   *
   * @param _key a {@link java.lang.String} object.
   * @param _value a boolean.
   */
  public void addAttribute(String _key, boolean _value) {
    this.addAttribute(_key, Boolean.toString(_value));
  }
  /**
   * hasAttribute.
   *
   * @param _key a {@link java.lang.String} object.
   * @return a boolean.
   */
  public boolean hasAttribute(String _key) {
    return this.attributes != null && this.attributes.containsKey(_key);
  }
  /**
   * isAttributeTrue.
   *
   * @param _key a {@link java.lang.String} object.
   * @return a boolean.
   */
  public boolean isAttributeTrue(String _key) {
    return this.hasAttribute(_key) && Boolean.valueOf(this.attributes.get(_key)).booleanValue();
  }
  /**
   * Setter for the field <code>attributes</code>.
   *
   * @param attributes a {@link java.util.Map} object.
   */
  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }
}
