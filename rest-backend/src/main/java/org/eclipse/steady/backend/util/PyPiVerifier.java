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
package org.eclipse.steady.backend.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.LibraryId;
import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;

/**
 * <p>PyPiVerifier class.</p>
 */
public class PyPiVerifier implements DigestVerifier {

  private static Logger log = LoggerFactory.getLogger(PyPiVerifier.class);

  private static Set<ProgrammingLanguage> SUPP_LANG = new HashSet<ProgrammingLanguage>();

  private static Set<DigestAlgorithm> SUPP_ALG = new HashSet<DigestAlgorithm>();

  static {
    SUPP_LANG.add(ProgrammingLanguage.PY);
    SUPP_ALG.add(DigestAlgorithm.MD5);
  }

  private String url = null;

  /** Release timestamp of the given digest (null if unknown). */
  private java.util.Calendar timestamp;

  private SimpleDateFormat dateFormat = null;

  /**
   * <p>Constructor for PyPiVerifier.</p>
   */
  public PyPiVerifier() {
    dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /** {@inheritDoc} */
  @Override
  public Set<ProgrammingLanguage> getSupportedLanguages() {
    return SUPP_LANG;
  }

  /** {@inheritDoc} */
  @Override
  public Set<DigestAlgorithm> getSupportedDigestAlgorithms() {
    return SUPP_ALG;
  }

  /** {@inheritDoc} */
  @Override
  public String getVerificationUrl() {
    return url;
  }

  /** {@inheritDoc} */
  @Override
  public java.util.Calendar getReleaseTimestamp() {
    return this.timestamp;
  }

  /** {@inheritDoc} */
  @Override
  public List<LibraryId> verify(final Library _lib) throws VerificationException {
    if (_lib == null || _lib.getDigest() == null)
      throw new IllegalArgumentException("No library or digest provided: [" + _lib + "]");

    if (_lib.getLibraryId() == null
        || _lib.getLibraryId().getMvnGroup() == null
        || _lib.getLibraryId().getVersion() == null) return null;

    this.url =
        "https://pypi.python.org/pypi/<name>/<version>/json"
            .replaceAll("<name>", _lib.getLibraryId().getMvnGroup())
            .replaceAll("<version>", _lib.getLibraryId().getVersion());

    String response_body = null;
    List<LibraryId> verified_lids = null;
    int sc = -1;
    try {
      final CloseableHttpClient httpclient = HttpClients.createDefault();
      final HttpGet method = new HttpGet(this.url);
      if (ConnectionUtil.getProxyConfig() != null)
        method.setConfig(ConnectionUtil.getProxyConfig());
      final CloseableHttpResponse response = httpclient.execute(method);
      try {
        sc = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (sc == HttpStatus.SC_OK && entity != null) {
          response_body = ConnectionUtil.readInputStream(entity.getContent());
          verified_lids = new ArrayList<LibraryId>();
          if (this.containsMD5(response_body, _lib.getDigest()))
            verified_lids.add(_lib.getLibraryId());
        }
      } finally {
        response.close();
      }
    } catch (Exception e) {
      throw new VerificationException(_lib, this.url, e);
    }
    return verified_lids;
  }

  /**
   * Returns true if the given JSON (produced by PyPi) contains a release having the given MD5 digest, false otherwise.
   * Example PyPi response for the Python library called requests: https://pypi.org/pypi/requests/2.18.4/json
   *
   * @param _json
   * @param _md5
   * @return
   */
  boolean containsMD5(String _json, final String _md5) {
    final List<String> releases =
        JsonPath.read(
            _json, "$.urls..[?(@.md5_digest == \"" + _md5.toLowerCase() + "\")].upload_time");

    // One result, take the release's timestamp
    if (releases.size() == 1) {
      final String upload_time = releases.get(0);
      try {
        final Date parsedDate = dateFormat.parse(upload_time);
        this.timestamp = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        this.timestamp.setTimeInMillis(parsedDate.getTime());
      } catch (ParseException e) {
        log.error(
            "Error when parsing the timestamp ["
                + upload_time
                + "] of PyPi package with MD5 digest ["
                + _md5
                + "]");
      }
    }

    // More than 1 result, don't take any timestamp
    else if (releases.size() > 1) {
      log.warn(
          "The lookup of MD5 digest ["
              + _md5
              + "] in PyPi returned ["
              + releases.size()
              + "] artifacts");
    }

    return !releases.isEmpty();
  }
}
