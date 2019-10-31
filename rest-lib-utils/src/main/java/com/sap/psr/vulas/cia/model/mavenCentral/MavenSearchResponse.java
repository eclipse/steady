package com.sap.psr.vulas.cia.model.mavenCentral;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;
import java.util.TreeSet;

/** Corresponds to the JSON object structure returned by the RESTful search of the Maven Central. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenSearchResponse {

  private long numFound;

  private Collection<ResponseDoc> docs;

  /** Constructor for MavenSearchResponse. */
  public MavenSearchResponse() {}

  /**
   * Getter for the field <code>numFound</code>.
   *
   * @return a long.
   */
  public long getNumFound() {
    return numFound;
  }
  /**
   * Setter for the field <code>numFound</code>.
   *
   * @param numFound a long.
   */
  public void setNumFound(long numFound) {
    this.numFound = numFound;
  }

  /**
   * Getter for the field <code>docs</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ResponseDoc> getDocs() {
    return docs;
  }
  /**
   * Setter for the field <code>docs</code>.
   *
   * @param docs a {@link java.util.Collection} object.
   */
  public void setDocs(Collection<ResponseDoc> docs) {
    this.docs = docs;
  }

  /**
   * getSortedDocs.
   *
   * @return a {@link java.util.TreeSet} object.
   */
  @JsonIgnore
  public TreeSet<ResponseDoc> getSortedDocs() {
    final TreeSet<ResponseDoc> set = new TreeSet<ResponseDoc>();
    set.addAll(this.getDocs());
    return set;
  }
}
