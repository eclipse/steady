package com.sap.psr.vulas;

import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.util.DigestUtil;
import java.nio.charset.StandardCharsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A programming construct represents a container for programming statements in a given programming
 * language, e.g., a Java constructor or a C function. Programming constructs are created by
 * instances of FileAnalyzer, e.g., JavaFileAnalyzer or JavaClassAnalyzer.
 */
public class Construct {

  private static final Log log = LogFactory.getLog(Construct.class);
  private ConstructId id = null;
  private String content, contentDigest = null;

  /**
   * Constructor for Construct.
   *
   * @param _id a {@link com.sap.psr.vulas.ConstructId} object.
   * @param _content a {@link java.lang.String} object.
   */
  public Construct(ConstructId _id, String _content) {
    if (_id == null || _content == null)
      throw new IllegalArgumentException("Id and content must be provided");
    this.id = _id;
    this.setContent(_content);
  }
  /**
   * getDigest.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDigest() {
    return contentDigest;
  }
  /**
   * Getter for the field <code>id</code>.
   *
   * @return a {@link com.sap.psr.vulas.ConstructId} object.
   */
  public ConstructId getId() {
    return id;
  }
  /**
   * Getter for the field <code>content</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getContent() {
    return content;
  }
  /**
   * Setter for the field <code>content</code>.
   *
   * @param _content a {@link java.lang.String} object.
   */
  public void setContent(String _content) {
    this.content = _content;
    this.contentDigest =
        DigestUtil.getDigestAsString(_content, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
  }
  /**
   * toJSON.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toJSON() {
    final JsonBuilder jb = new JsonBuilder();
    jb.startObject();
    jb.appendObjectProperty("id", this.id.toJSON(), false);
    jb.appendObjectProperty("cd", this.contentDigest);
    jb.endObject();
    return jb.getJson();
  }
  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((contentDigest == null) ? 0 : contentDigest.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }
  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Construct other = (Construct) obj;
    if (contentDigest == null) {
      if (other.contentDigest != null) return false;
    } else if (!contentDigest.equals(other.contentDigest)) return false;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    return true;
  }
  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return this.id.toString();
  }
}
