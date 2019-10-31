package com.sap.psr.vulas.backend.requests;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.HttpResponse;
import com.sap.psr.vulas.goals.GoalContext;
import java.io.IOException;
import java.io.Serializable;

/** Http request that can be send and saved to (loaded from) disk. */
public interface HttpRequest extends Serializable {

  /**
   * send.
   *
   * @return a {@link com.sap.psr.vulas.backend.HttpResponse} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public HttpResponse send() throws BackendConnectionException;

  /**
   * getGoalContext.
   *
   * @return a {@link com.sap.psr.vulas.goals.GoalContext} object.
   */
  public GoalContext getGoalContext();

  /**
   * setGoalContext.
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @return a {@link com.sap.psr.vulas.backend.requests.HttpRequest} object.
   */
  public HttpRequest setGoalContext(GoalContext _ctx);

  /**
   * saveToDisk.
   *
   * @throws java.io.IOException if any.
   */
  public void saveToDisk() throws IOException;

  /**
   * savePayloadToDisk.
   *
   * @throws java.io.IOException if any.
   */
  public void savePayloadToDisk() throws IOException;

  /**
   * loadFromDisk.
   *
   * @throws java.io.IOException if any.
   */
  public void loadFromDisk() throws IOException;

  /**
   * loadPayloadFromDisk.
   *
   * @throws java.io.IOException if any.
   */
  public void loadPayloadFromDisk() throws IOException;

  /**
   * deleteFromDisk.
   *
   * @throws java.io.IOException if any.
   */
  public void deleteFromDisk() throws IOException;

  /**
   * deletePayloadFromDisk.
   *
   * @throws java.io.IOException if any.
   */
  public void deletePayloadFromDisk() throws IOException;

  /**
   * getFilename.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFilename();
}
