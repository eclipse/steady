package com.sap.psr.vulas.cia.model.pypi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/** PypiResponse class. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PypiResponse {

  PypiInfo info;

  LinkedHashMap<String, ArrayList<PypiRelease>> releases;

  /**
   * Getter for the field <code>info</code>.
   *
   * @return a {@link com.sap.psr.vulas.cia.model.pypi.PypiInfo} object.
   */
  public PypiInfo getInfo() {
    return info;
  }

  /**
   * Setter for the field <code>info</code>.
   *
   * @param info a {@link com.sap.psr.vulas.cia.model.pypi.PypiInfo} object.
   */
  public void setInfo(PypiInfo info) {
    this.info = info;
  }

  /**
   * Getter for the field <code>releases</code>.
   *
   * @return a {@link java.util.LinkedHashMap} object.
   */
  public LinkedHashMap<String, ArrayList<PypiRelease>> getReleases() {
    return releases;
  }

  /**
   * Setter for the field <code>releases</code>.
   *
   * @param releases a {@link java.util.LinkedHashMap} object.
   */
  public void setReleases(LinkedHashMap<String, ArrayList<PypiRelease>> releases) {
    this.releases = releases;
  }
}
