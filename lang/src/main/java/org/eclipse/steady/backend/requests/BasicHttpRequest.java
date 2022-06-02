/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.backend.requests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.HttpMethod;
import org.eclipse.steady.backend.HttpResponse;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.GoalContext;
import org.eclipse.steady.shared.connectivity.Service;
import org.eclipse.steady.shared.json.JsonBuilder;
import org.eclipse.steady.shared.json.JsonSyntaxException;
import org.eclipse.steady.shared.util.Constants;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.StringUtil;

/**
 * <p>BasicHttpRequest class.</p>
 */
public class BasicHttpRequest extends AbstractHttpRequest {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(BasicHttpRequest.class);

  private static final long serialVersionUID = 1L;

  private HttpMethod method = null;
  private String path = null;
  private Map<String, String> params = null;
  private Service service = null;

  /** Will not be serialized as part of the class, but is written to dedicated file. */
  private transient String payload = null;

  /** Will not be serialized as part of the class. */
  private transient FileInputStream binPayload = null;

  private String contentType = null;

  private String dir = null;

  /** Null in case the request does not exist on disk. */
  private String payloadPath = null;

  /** Cached in case {@link HttpRequest#send()} is called multiple times on the same request. */
  private transient HttpResponse response = null;

  private boolean checkJson = false;

  /**
   * <p>Constructor for BasicHttpRequest.</p>
   *
   * @param _method a {@link org.eclipse.steady.backend.HttpMethod} object.
   * @param _path a {@link java.lang.String} object.
   */
  public BasicHttpRequest(HttpMethod _method, String _path) {
    this(Service.BACKEND, _method, _path, null);
  }

  /**
   * Creates a request for the RESTful service {@link Service#BACKEND}.
   *
   * @param _method a {@link org.eclipse.steady.backend.HttpMethod} object.
   * @param _path a {@link java.lang.String} object.
   * @param _query_string_params a {@link java.util.Map} object.
   */
  public BasicHttpRequest(
      HttpMethod _method, String _path, Map<String, String> _query_string_params) {
    this(Service.BACKEND, _method, _path, _query_string_params);
  }

  /**
   * Creates a request for the given RESTful {@link Service}.
   *
   * @param _service a {@link org.eclipse.steady.shared.connectivity.Service} object.
   * @param _method a {@link org.eclipse.steady.backend.HttpMethod} object.
   * @param _path a {@link java.lang.String} object.
   * @param _query_string_params a {@link java.util.Map} object.
   */
  public BasicHttpRequest(
      Service _service,
      HttpMethod _method,
      String _path,
      Map<String, String> _query_string_params) {
    this.service = _service;
    this.method = _method;
    this.path = _path;
    this.params = _query_string_params;
  }

  /**
   * <p>Setter for the field <code>payload</code>.</p>
   *
   * @param _payload a {@link java.lang.String} object.
   * @param _type a {@link java.lang.String} object.
   * @param _check a boolean.
   * @return a {@link org.eclipse.steady.backend.requests.BasicHttpRequest} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public BasicHttpRequest setPayload(String _payload, String _type, boolean _check)
      throws IllegalArgumentException {
    if (this.method.equals(HttpMethod.POST)
        || this.method.equals(HttpMethod.PUT)
        || this.method.equals(HttpMethod.DELETE)) {
      this.payload = _payload;
      this.contentType = _type;
      this.checkJson = _check;
      return this;
    } else {
      throw new IllegalStateException(
          "Payload only possible for POST, PUT and DELETE, not for [" + this.method + "]");
    }
  }

  /**
   * <p>Setter for the field <code>binPayload</code>.</p>
   *
   * @param _payload a {@link java.io.FileInputStream} object.
   * @param _type a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.backend.requests.BasicHttpRequest} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public BasicHttpRequest setBinPayload(FileInputStream _payload, String _type)
      throws IllegalArgumentException {
    if (this.method.equals(HttpMethod.POST)) {
      this.binPayload = _payload;
      this.contentType = _type;
      return this;
    } else {
      throw new IllegalStateException(
          "Payload only possible for POST not for [" + this.method + "]");
    }
  }

  /** {@inheritDoc} */
  @Override
  public HttpRequest setGoalContext(GoalContext _ctx) {
    this.context = _ctx;
    return this;
  }

  /**
   * <p>Setter for the field <code>dir</code>.</p>
   *
   * @param dir a {@link java.lang.String} object.
   */
  public void setDir(String dir) {
    this.dir = dir;
  }

  /**
   * <p>hasPayload.</p>
   *
   * @return a boolean.
   */
  public boolean hasPayload() {
    return this.payload != null && !this.payload.isEmpty();
  }

  private boolean isUploadRequest() {
    return this.method.equals(HttpMethod.POST) || this.method.equals(HttpMethod.PUT);
  }

  /**
   * <p>send.</p>
   *
   * @return a {@link org.eclipse.steady.backend.HttpResponse} object.
   * @throws org.eclipse.steady.backend.BackendConnectionException if any.
   */
  public HttpResponse send() throws BackendConnectionException {
    if (this.response == null) {
      // Check JSON
      Exception exception =
          (this.hasPayload() && this.checkJson ? BasicHttpRequest.checkJson(this.payload) : null);

      // Make call if one of the following holds:
      // - call is read request and connect is not offline
      // - call is write request, exception is null and connect is read_write
      if ((!this.isUploadRequest()
              && !CoreConfiguration.isBackendOffline(this.getVulasConfiguration()))
          || (this.isUploadRequest()
              && exception == null
              && CoreConfiguration.isBackendReadWrite(this.getVulasConfiguration()))) {
        try {
          response = this.sendRequest();

          // Delete file (if any) on success
          if (response.isCreated() || response.isOk()) {
            try {
              this.deleteFromDisk();
            } catch (IOException e) {
              BasicHttpRequest.log.error(
                  "Error deleting files from update directory: " + e.getMessage());
            }
          }
        } catch (BackendConnectionException e) {
          exception = e;
          BasicHttpRequest.log.error(e.getMessage());
          try {
            FileUtil.writeToFile(
                new File(
                    this.getVulasConfiguration().getTmpDir().toFile(),
                    this.getFilename() + ".html"),
                e.getHttpResponseBody());
          } catch (IOException e1) {
            BasicHttpRequest.log.error("Error saving HTTP error message: " + e1.getMessage(), e1);
          }
        }
      }

      // Save to disk if
      // - call is write request and exception is not null or connect is not read_write
      if (this.isUploadRequest()
          && !this.isPayloadSavedOnDisk()
          && (exception != null
              || !CoreConfiguration.isBackendReadWrite(this.getVulasConfiguration()))) {
        try {
          this.saveToDisk();
        } catch (IOException e) {
          BasicHttpRequest.log.info("HttpUploadRequest could not be saved: " + e.getMessage());
        }
      }

      // Throw exception (if any)
      if (exception != null)
        throw new BackendConnectionException(exception.getMessage(), exception);
    }

    return response;
  }

  private boolean isPayloadSavedOnDisk() {
    return this.payloadPath != null && Paths.get(this.payloadPath).toFile().exists();
  }

  /** {@inheritDoc} */
  @Override
  public String getFilename() {
    String prefix = this.path;
    if (prefix.startsWith("/")) prefix = prefix.substring(1);
    prefix = prefix.replace("/", "__");
    prefix = prefix.replace("?", "__");
    prefix = this.ms + "-" + prefix;
    return prefix;
  }

  /**
   * Returns the payload size (or -1 if there's no payload).
   *
   * @return a long
   */
  public long getPayloadSize() {
    if (this.payload != null) return this.payload.getBytes(StandardCharsets.UTF_8).length;
    else if (this.payloadPath != null) return Paths.get(this.payloadPath).toFile().length();
    else return -1;
  }

  /**
   * <p>getPayloadFilename.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPayloadFilename() {
    return this.getFilename() + ".json";
  }

  /** {@inheritDoc} */
  @Override
  public void savePayloadToDisk() throws IOException {
    if (this.hasPayload()) {
      final Path payload_path =
          Paths.get(
              this.getVulasConfiguration().getDir(CoreConfiguration.UPLOAD_DIR).toString(),
              this.getPayloadFilename());
      this.payloadPath = payload_path.toString();
      final File json_file = payload_path.toFile();
      FileUtil.writeToFile(json_file, this.payload);
      BasicHttpRequest.log.info("Request body (JSON) written to [" + json_file + "]");
    }
  }

  /** {@inheritDoc} */
  @Override
  public void loadPayloadFromDisk() throws IOException {
    if (this.payloadPath != null) this.payload = FileUtil.readFile(this.payloadPath);
  }

  /** {@inheritDoc} */
  @Override
  public void deletePayloadFromDisk() throws IOException {
    if (this.payloadPath != null) Paths.get(this.payloadPath).toFile().deleteOnExit();
  }

  private final HttpResponse sendRequest() throws BackendConnectionException {

    int response_code = -1;
    org.apache.http.HttpResponse httpResponse = null;
    final URI uri = this.getUri();
    HttpUriRequest httpUriRequest = null;
    final RequestRepeater repeater =
        new RequestRepeater(
            this.getVulasConfiguration()
                .getConfiguration()
                .getLong(CoreConfiguration.REPEAT_MAX, 50),
            this.getVulasConfiguration()
                .getConfiguration()
                .getLong(CoreConfiguration.REPEAT_WAIT, 60000));

    boolean is_503;
    RequestBuilder requestBuilder = null;
    switch (this.method) {
      case GET:
        requestBuilder = RequestBuilder.get();
        break;
      case PUT:
        requestBuilder = RequestBuilder.put();
        break;
      case POST:
        requestBuilder = RequestBuilder.post();
        break;
      case OPTIONS:
        requestBuilder = RequestBuilder.options();
        break;
      case DELETE:
        requestBuilder = RequestBuilder.delete();
        break;
      default:
        throw new BackendConnectionException("Invalid HTTP method: [" + this.method + "]", null);
    }

    requestBuilder = requestBuilder.setUri(uri);

    // Include tenant and space Http headers
    String tenant_token = null, space_token = null;
    if (this.context != null && this.context.hasTenant()) {
      tenant_token = this.context.getTenant().getTenantToken();
      requestBuilder.addHeader(Constants.HTTP_TENANT_HEADER, tenant_token);
    }
    if (this.context != null && this.context.hasSpace()) {
      space_token = this.context.getSpace().getSpaceToken();
      requestBuilder.addHeader(Constants.HTTP_SPACE_HEADER, space_token);
    }

    // Include version and component as request header
    requestBuilder.addHeader(Constants.HTTP_VERSION_HEADER, CoreConfiguration.getVulasRelease());
    requestBuilder.addHeader(
        Constants.HTTP_COMPONENT_HEADER, Constants.VulasComponent.client.toString());

    // Include additional headers from configuration (if any)
    final Map<String, String> add_headers =
        this.getVulasConfiguration().getServiceHeaders(this.service);
    if (add_headers != null && !add_headers.isEmpty()) {
      for (Map.Entry<String, String> e : add_headers.entrySet()) {
        requestBuilder.addHeader(e.getKey(), e.getValue());
      }
    }

    // Only if put something in the body
    if (this.hasPayload()) {
      requestBuilder.addHeader("Content-Type", "application/json; charset=utf-8");
      requestBuilder.addHeader("Content-Language", "en-US");
    } else if (this.binPayload != null) {
      requestBuilder.addHeader("Content-Type", this.contentType);
    }

    if (this.hasPayload()) {
      requestBuilder.setEntity(new StringEntity(this.payload, StandardCharsets.UTF_8));
    } else if (this.binPayload != null) {
      requestBuilder.setEntity(new InputStreamEntity(this.binPayload));
    }
    RequestConfig config = RequestConfig.custom().setExpectContinueEnabled(true).build();
    requestBuilder.setConfig(config);
    httpUriRequest = requestBuilder.build();

    try {
      do {
        is_503 = false;

        final long start_nano = System.nanoTime();

        if (!this.hasPayload()) {
          BasicHttpRequest.log.info(
              "HTTP "
                  + this.method.toString().toUpperCase()
                  + " [uri="
                  + uri
                  + (tenant_token == null ? "" : ", tenant=" + tenant_token)
                  + (space_token == null ? "" : ", space=" + space_token)
                  + "]");
        } else if (this.binPayload == null) {
          BasicHttpRequest.log.info(
              "HTTP "
                  + this.method.toString().toUpperCase()
                  + " [uri="
                  + uri
                  + ", size="
                  + StringUtil.byteToKBString(this.payload.getBytes(StandardCharsets.UTF_8).length)
                  + (tenant_token == null ? "" : ", tenant=" + tenant_token)
                  + (space_token == null ? "" : ", space=" + space_token)
                  + "]");
        } else {
          BasicHttpRequest.log.info(
              "HTTP "
                  + this.method.toString().toUpperCase()
                  + " [uri="
                  + uri
                  + ", size="
                  + this.binPayload.available()
                  + (tenant_token == null ? "" : ", tenant=" + tenant_token)
                  + (space_token == null ? "" : ", space=" + space_token)
                  + "]");
        }

        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).build();
        HttpClient client =
            HttpClients.custom().setDefaultSocketConfig(socketConfig).useSystemProperties().build();

        // Read response
        httpResponse = client.execute(httpUriRequest);
        response_code = httpResponse.getStatusLine().getStatusCode();
        response = new HttpResponse(response_code);
        // If the response body contains a JAR file, save it
        if (response.isOk()
            && httpResponse.getFirstHeader("Content-Type") != null
            && httpResponse
                .getFirstHeader("Content-Type")
                .getValue()
                .contains("application/java-archive")) {
          String fileName = "";
          Header disposition = httpResponse.getFirstHeader("Content-Disposition");
          if (disposition != null) {
            String dispositionValue = disposition.getValue();
            // Extracts file name from header field
            int index = dispositionValue.indexOf("filename=");
            if (index > 0) {
              fileName = dispositionValue.substring(index + 9, dispositionValue.length());
            }
          } else {
            // Extracts file name from URL
            fileName = this.path.substring(this.path.lastIndexOf("/") + 1, this.path.length());
          }

          String saveFilePath = null;
          if (this.dir != null) {
            // create directories if not existing
            if (!Files.exists(Paths.get(dir))) {
              Files.createDirectories(Paths.get(dir));
            }
            saveFilePath = dir + File.separator + fileName;
          } else {
            saveFilePath =
                Paths.get(this.getVulasConfiguration().getTmpDir().toString()).toString()
                    + File.separator
                    + fileName;
          }

          try (
          // Opens input stream from the HTTP connection
          InputStream inputStream = httpResponse.getEntity().getContent();
              // Opens an output stream to save into file
              FileOutputStream outputStream = new FileOutputStream(saveFilePath); ) {
            int bytesRead = -1;
            byte[] buffer = new byte[inputStream.available()];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
              outputStream.write(buffer, 0, bytesRead);
            }

            response.setBody(saveFilePath);
          }
        } else if (response.isOk() || response.isCreated()) {
          final String body = this.readResponse(httpResponse);
          if (StringUtils.isNotBlank(body)) response.setBody(body);
        }

        // Stats
        final long end_nano = System.nanoTime();
        BasicHttpRequest.log.info(
            "HTTP "
                + this.method.toString().toUpperCase()
                + " completed with response code ["
                + response_code
                + "] in "
                + StringUtil.nanoToFlexDurationString(end_nano - start_nano)
                + " (proxy="
                + isProxySet()
                + ")");

        // 503: Retry
        if (response.isServiceUnavailable()) {
          is_503 = true;
        }
        // 5xx: Throw exception
        else if (response.isServerError()
            || response.getStatus() == 400
            || response.getStatus() == 403) {
          final BackendConnectionException bce =
              new BackendConnectionException(this.method, uri, response_code, null);
          throwBceException(httpResponse, bce);
        }
      } while (repeater.repeat(is_503));
      if (is_503) throw new BackendConnectionException(this.method, uri, 503, null);
      /*} catch (BackendConnectionException bce) {
      this.logHeaderFields("    Request-header", httpUriRequest.getAllHeaders());
      this.logHeaderFields("    Response-header", httpResponse.getAllHeaders());
      if (bce.getHttpResponseBody() != null)
        BasicHttpRequest.log.error(
            "    Response-body: ["
                + bce.getHttpResponseBody().replaceAll("[\\t\\n\\x0B\\f\\r]*", "")
                + "]");
      BasicHttpRequest.log.error("    Exception message: [" + bce.getMessage() + "]");
      if (this.hasPayload())
        BasicHttpRequest.log.error("    HTTP Request body: [" + this.payload.toString() + "]");*/
    } catch (IOException e) {
      final BackendConnectionException bce =
          new BackendConnectionException(this.method, uri, response_code, e);
      throwBceException(httpResponse, bce);
    }
    return response;
  }

  private boolean isProxySet() {
    return StringUtils.isNotBlank(System.getProperty("http.proxyHost"))
        ? true
        : false || StringUtils.isNotBlank(System.getProperty("https.proxyHost")) ? true : false;
  }

  /**
   * @param _httpResponse
   * @param _bce
   * @throws BackendConnectionException
   */
  private void throwBceException(
      org.apache.http.HttpResponse _httpResponse, final BackendConnectionException _bce)
      throws BackendConnectionException {
    try {
      final String body = this.readResponse(_httpResponse);
      if (StringUtils.isNotBlank(body)) _bce.setHttpResponseBody(body);
    } catch (IOException e) {
      // BasicHttpRequest.log.error("Cannot read input stream: " + e1.getMessage());
    }
    throw _bce;
  }

  /**
   * @param _prefix
   * @param _fields
   */
  private void logHeaderFields(String _prefix, Header[] _fields) {
    if (_fields == null) {
      return;
    }

    for (Header header : _fields)
      BasicHttpRequest.log.error(
          _prefix + " " + "[" + header.getName() + "]" + " = " + header.getValue());
  }

  /**
   * @param _c
   * @throws IOException
   */
  private String readResponse(org.apache.http.HttpResponse _c) throws IOException {
    String response = null;
    if (_c != null) {
      InputStream is = _c.getEntity().getContent();
      if (is != null) {
        response = FileUtil.readInputStream(is, FileUtil.getCharset());
      }
    }
    return response;
  }

  private URI getUri() {
    return this.getUri(this.service, this.path, this.params);
  }

  /**
   * <p>getUri.</p>
   *
   * @param _service a {@link org.eclipse.steady.shared.connectivity.Service} object.
   * @param _path a {@link java.lang.String} object.
   * @param _params a {@link java.util.Map} object.
   * @return a {@link java.net.URI} object.
   */
  public URI getUri(Service _service, String _path, Map<String, String> _params) {

    // Check whether URL is present
    if (!CoreConfiguration.isBackendOffline(this.getVulasConfiguration())
        && !this.getVulasConfiguration().hasServiceUrl(_service))
      throw new IllegalStateException("URL for service [" + _service + "] is not configured");

    URI uri = null;

    final StringBuilder builder = new StringBuilder();
    builder.append(this.getVulasConfiguration().getServiceUrl(_service));
    builder.append(_path);
    int i = 0;
    if (_params != null) {
      for (Map.Entry<String, String> entry : _params.entrySet()) {
        if (i == 0) builder.append('?');
        else if (i <= _params.size()) builder.append('&');
        builder.append(entry.getKey()).append('=').append(entry.getValue());
        i++;
      }
    }
    try {
      if (builder.toString().contains("[") || builder.toString().contains("]")) {
        URL url = new URL(builder.toString());
        uri =
            new URI(
                url.getProtocol(),
                url.getUserInfo(),
                url.getHost(),
                url.getPort(),
                url.getPath(),
                url.getQuery(),
                url.getRef());
      } else {
        uri = new URI(builder.toString());
      }
    } catch (MalformedURLException e) {
      throw new IllegalStateException(
          "Error when creating URI out of [" + builder.toString() + "]: " + e.getMessage());
    } catch (URISyntaxException e) {
      throw new IllegalStateException(
          "Error when creating URI out of [" + builder.toString() + "]: " + e.getMessage());
    }

    return uri;
  }

  /**
   * Checks the syntax of the given JSON string.
   * @param _json
   * @return a JsonSyntaxException if there is a problem, null otherwise
   */
  private static final Exception checkJson(String _json) {
    Exception exception = null;
    if (_json != null) {
      try {
        JsonBuilder.checkJsonValidity(_json);
      } catch (JsonSyntaxException jse) {
        BasicHttpRequest.log.error("Invalid JSON syntax: " + jse.getMessage());
        exception = jse;
      }
    }
    return exception;
  }

  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return "HTTP " + this.method.toString().toUpperCase() + " [uri=" + this.getUri() + "]";
  }

  /**
   * First calls the default method {@link ObjectInputStream#defaultReadObject()}, then calls {@link HttpRequest#loadFromDisk()}
   * @param in
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.loadFromDisk();
  }
}
