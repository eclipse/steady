package com.sap.psr.vulas.cia.model.mavenCentral;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Corresponds to the JSON object structure returned by the RESTful search of the Maven Central. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenVersionsSearch {

  private MavenSearchResponse response;

  /** Constructor for MavenVersionsSearch. */
  public MavenVersionsSearch() {}

  /**
   * Getter for the field <code>response</code>.
   *
   * @return a {@link com.sap.psr.vulas.cia.model.mavenCentral.MavenSearchResponse} object.
   */
  public MavenSearchResponse getResponse() {
    return response;
  }
  /**
   * Setter for the field <code>response</code>.
   *
   * @param response a {@link com.sap.psr.vulas.cia.model.mavenCentral.MavenSearchResponse} object.
   */
  public void setResponse(MavenSearchResponse response) {
    this.response = response;
  }
}
