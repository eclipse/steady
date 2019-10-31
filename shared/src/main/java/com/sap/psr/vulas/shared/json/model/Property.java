package com.sap.psr.vulas.shared.json.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.PropertySource;
import com.sap.psr.vulas.shared.util.DigestUtil;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * General purpose property referenced by all kinds of entities, e.g., {@link Library}s. It is not
 * dependent on (contained in) other entities but saved and created independently in order to be
 * able to have the same property referenced by multiple other entities.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"valueSha1"})
public class Property implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonIgnore private Long id;

  private PropertySource source;

  private String name;

  private String value;

  @JsonIgnore private String valueSha1;

  /** Constructor for Property. */
  public Property() {
    super();
  }

  /**
   * Constructor for Property.
   *
   * @param source a {@link com.sap.psr.vulas.shared.enums.PropertySource} object.
   * @param _name a {@link java.lang.String} object.
   * @param _value a {@link java.lang.String} object.
   */
  public Property(PropertySource source, String _name, String _value) {
    super();
    this.source = source;
    this.name = _name;
    this.value = _value;
    this.valueSha1 =
        DigestUtil.getDigestAsString(this.value, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
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
   * Getter for the field <code>source</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.PropertySource} object.
   */
  public PropertySource getSource() {
    return source;
  }
  /**
   * Setter for the field <code>source</code>.
   *
   * @param source a {@link com.sap.psr.vulas.shared.enums.PropertySource} object.
   */
  public void setSource(PropertySource source) {
    this.source = source;
  }

  /**
   * Getter for the field <code>name</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getName() {
    return name;
  }
  /**
   * Setter for the field <code>name</code>.
   *
   * @param _name a {@link java.lang.String} object.
   */
  public void setName(String _name) {
    this.name = _name;
  }

  /**
   * Getter for the field <code>value</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getValue() {
    return value;
  }
  /**
   * Setter for the field <code>value</code>.
   *
   * @param _value a {@link java.lang.String} object.
   */
  public void setValue(String _value) {
    this.value = _value;
    this.valueSha1 =
        DigestUtil.getDigestAsString(this.value, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
  }

  /**
   * Getter for the field <code>valueSha1</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getValueSha1() {
    return valueSha1;
  }
  /**
   * Setter for the field <code>valueSha1</code>.
   *
   * @param valueSha1 a {@link java.lang.String} object.
   */
  public void setValueSha1(String valueSha1) {
    this.valueSha1 = valueSha1;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    result = prime * result + ((valueSha1 == null) ? 0 : valueSha1.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Property other = (Property) obj;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (source != other.source) return false;
    if (valueSha1 == null) {
      if (other.valueSha1 != null) return false;
    } else if (!valueSha1.equals(other.valueSha1)) return false;
    return true;
  }
}
