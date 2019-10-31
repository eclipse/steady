package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.json.model.metrics.Metrics;
import java.util.Set;

/**
 * Describes the update of a {@link Dependency} of an {@link Application} from one version of a
 * {@link Library} to another one. To that end, it contains a list of calls that require to be
 * modified because certain constructs are not available in the target {@link Library}. Moreover,
 * diverse {@link Metrics} quantify the update effort.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DependencyUpdate {

  LibraryId fromLibraryId;

  LibraryId toLibraryId;

  Metrics metrics;

  Set<TouchPoint> callsToModify;

  /**
   * Constructor for DependencyUpdate.
   *
   * @param f a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
   * @param t a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
   */
  public DependencyUpdate(LibraryId f, LibraryId t) {
    this.fromLibraryId = f;
    this.toLibraryId = t;
  }

  /**
   * Getter for the field <code>callsToModify</code>.
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<TouchPoint> getCallsToModify() {
    return callsToModify;
  }
  /**
   * Setter for the field <code>callsToModify</code>.
   *
   * @param c a {@link java.util.Set} object.
   */
  public void setCallsToModify(Set<TouchPoint> c) {
    this.callsToModify = c;
  }

  /**
   * Getter for the field <code>fromLibraryId</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
   */
  public LibraryId getFromLibraryId() {
    return fromLibraryId;
  }
  /**
   * Setter for the field <code>fromLibraryId</code>.
   *
   * @param f a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
   */
  public void setFromLibraryId(LibraryId f) {
    this.fromLibraryId = f;
  }

  /**
   * Getter for the field <code>toLibraryId</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
   */
  public LibraryId getToLibraryId() {
    return toLibraryId;
  }
  /**
   * Setter for the field <code>toLibraryId</code>.
   *
   * @param t a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
   */
  public void setToLibraryId(LibraryId t) {
    this.toLibraryId = t;
  }

  /**
   * Getter for the field <code>metrics</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.metrics.Metrics} object.
   */
  public Metrics getMetrics() {
    return metrics;
  }
  /**
   * Setter for the field <code>metrics</code>.
   *
   * @param m a {@link com.sap.psr.vulas.shared.json.model.metrics.Metrics} object.
   */
  public void setMetrics(Metrics m) {
    this.metrics = m;
  }
}
