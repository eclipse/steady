package com.sap.psr.vulas.backend.requests;

import com.sap.psr.vulas.backend.HttpResponse;

/** StatusCondition class. */
public class StatusCondition implements ResponseCondition {

  private int status;

  /**
   * Constructor for StatusCondition.
   *
   * @param _status a int.
   */
  public StatusCondition(int _status) {
    this.status = _status;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns true if the HTTP status of the given {@link HttpResponse} equals the status of the
   * condition, false otherwise.
   */
  @Override
  public boolean meetsCondition(HttpResponse _response) {
    return (_response != null && _response.getStatus() == this.status);
  }

  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return "[HTTP RC==" + this.status + "]";
  }
}
