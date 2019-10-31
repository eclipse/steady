package com.sap.psr.vulas.backend.requests;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.HttpResponse;
import com.sap.psr.vulas.goals.GoalContext;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** HttpRequestList class. */
public class HttpRequestList extends AbstractHttpRequest {

  private static final Log log = LogFactory.getLog(HttpRequestList.class);

  /**
   * When set to true, the sending of requests will be stopped upon success, i.e., once a Http
   * response code 2xx will be received.
   */
  private boolean stopOnSuccess = true;

  private List<HttpRequest> list = new LinkedList<HttpRequest>();

  /** Constructor for HttpRequestList. */
  public HttpRequestList() {
    this(true);
  }

  /**
   * Constructor for HttpRequestList.
   *
   * @param _stop_on_success a boolean.
   */
  public HttpRequestList(boolean _stop_on_success) {
    this.stopOnSuccess = _stop_on_success;
  }

  /**
   * addRequest.
   *
   * @param _r a {@link com.sap.psr.vulas.backend.requests.HttpRequest} object.
   */
  public void addRequest(HttpRequest _r) {
    this.list.add(_r);
  }

  /** {@inheritDoc} */
  @Override
  public HttpRequest setGoalContext(GoalContext _ctx) {
    this.context = _ctx;
    for (HttpRequest r : this.list) r.setGoalContext(_ctx);
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Loops over the list of requests and calls {@link HttpRequest#send()}. Depending on the
   * boolean {@link HttpRequestList#stopOnSuccess}, the sending stops or does not stop in case of a
   * successful call.
   */
  @Override
  public HttpResponse send() throws BackendConnectionException {
    HttpResponse response = null;
    for (HttpRequest r : this.list) {
      response = r.send();
      if (this.stopOnSuccess && response != null && (response.isOk() || response.isCreated()))
        break;
    }
    return response;
  }

  /** {@inheritDoc} */
  @Override
  public String getFilename() {
    String prefix = this.ms + "-hrl";
    return prefix;
  }

  /** {@inheritDoc} */
  @Override
  public void savePayloadToDisk() throws IOException {
    for (HttpRequest r : this.list) {
      r.savePayloadToDisk();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void loadPayloadFromDisk() throws IOException {
    for (HttpRequest r : this.list) {
      r.loadPayloadFromDisk();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void deletePayloadFromDisk() throws IOException {
    for (HttpRequest r : this.list) {
      r.deletePayloadFromDisk();
    }
  }
}
