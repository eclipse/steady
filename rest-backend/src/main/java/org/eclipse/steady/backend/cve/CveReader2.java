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
package org.eclipse.steady.backend.cve;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.steady.backend.util.ConnectionUtil;
import org.eclipse.steady.shared.cache.Cache;
import org.eclipse.steady.shared.cache.CacheException;
import org.eclipse.steady.shared.cache.ObjectFetcher;
import org.eclipse.steady.shared.connectivity.Service;
import org.eclipse.steady.shared.connectivity.ServiceConnectionException;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

/**
 * Reads {@link Cve} information from a service configured with {@link #CVE_SERVICE_URL}.
 */
public class CveReader2 implements ObjectFetcher<String, Cve> {

  private static Logger log = LoggerFactory.getLogger(CveReader2.class);

  /**
   * Cache entries are invalidated after one day (1440 min) in order to not miss any changes.
   */
  private static Cache<String, Cve> CVE_CACHE = new Cache<String, Cve>(new CveReader2(), 1440);

  /**
   * <p>read.</p>
   *
   * @param _key a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.backend.cve.Cve} object.
   * @throws org.eclipse.steady.shared.cache.CacheException if any.
   */
  public static Cve read(String _key) throws CacheException {
    return CVE_CACHE.get(_key);
  }

  /**
   * <p>read.</p>
   *
   * @param _key a {@link java.lang.String} object.
   * @param _force_fetch a boolean.
   * @return a {@link org.eclipse.steady.backend.cve.Cve} object.
   * @throws org.eclipse.steady.shared.cache.CacheException if any.
   */
  public static Cve read(String _key, boolean _force_fetch) throws CacheException {
    return CVE_CACHE.get(_key, _force_fetch);
  }

  /**
   * Returns CVE information for the given key (or null in case the key is null).
   * This information is retrieved from a (remote) service {@link Service#CVE}.
   *
   * @param _key a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.backend.cve.Cve} object.
   * @throws org.eclipse.steady.shared.cache.CacheException if any.
   */
  public Cve fetch(String _key) throws CacheException {
    if (_key == null) return null;

    Cve cve = null;
    int sc = -1;
    String result = null;
    String uri = null;

    try {
      final String url = VulasConfiguration.getGlobal().getServiceUrl(Service.CVE, true);

      final CloseableHttpClient httpclient = HttpClients.createDefault();
      uri = url.replaceAll("<ID>", _key);
      log.info("Query details of [" + _key + "] at [" + uri + "]");
      final HttpGet method = new HttpGet(uri);
      if (ConnectionUtil.getProxyConfig() != null)
        method.setConfig(ConnectionUtil.getProxyConfig());
      final CloseableHttpResponse response = httpclient.execute(method);
      try {
        sc = response.getStatusLine().getStatusCode();
        final HttpEntity entity = response.getEntity();
        if (sc == org.apache.http.HttpStatus.SC_OK && entity != null) {
          result = ConnectionUtil.readInputStream(entity.getContent());
          cve = CveReader2.buildFromJson(_key, result);
        } else {
          log.error(
              "HTTP GET [url="
                  + uri
                  + "] completed with ["
                  + sc
                  + "], and entity ["
                  + entity
                  + "]");
        }
      } catch (ParseException e) {
        log.error("HTTP GET [url=" + uri + "] caused an exception: " + e.getMessage());
        throw new CacheException(_key, e);
      } finally {
        response.close();
      }
      log.info("Fetched " + cve + " for key [" + _key + "]");
    } catch (ClientProtocolException e) {
      log.error("HTTP GET [url=" + uri + "] caused an exception: " + e.getMessage());
      throw new CacheException(_key, e);
    } catch (IOException e) {
      log.error("HTTP GET [url=" + uri + "] caused an exception: " + e.getMessage());
      log.error("Error: " + e.getMessage(), e);
      throw new CacheException(_key, e);
    } catch (ServiceConnectionException e) {
      log.error(e.getMessage());
      throw new CacheException(_key, e);
    }
    return cve;
  }

  private static final Cve buildFromJson(String _id, String _json) throws ParseException {
    final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    final Cve cve = new Cve();
    cve.setId(_id);

    final Configuration conf = Configuration.defaultConfiguration();
    // conf.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
    final Object document = conf.jsonProvider().parse(_json);

    // Take first english description
    final JSONArray descriptions =
        JsonPath.read(document, "$.result.CVE_Items[0].cve.description.description_data[?(@.lang=='en')].value");
    if (descriptions == null || descriptions.size() == 0) {
      log.warn("No english description found for CVE [" + _id + "]");
      cve.setSummary("Not available");
    } else {
      cve.setSummary(descriptions.get(0).toString());
    }

    final String published = JsonPath.read(document, "$.result.CVE_Items[0].publishedDate");
    final Calendar publ = new GregorianCalendar();
    publ.setTime(format.parse(published));
    cve.setPublished(publ);

    final String modified = JsonPath.read(document, "$.result.CVE_Items[0].lastModifiedDate");
    final Calendar modi = new GregorianCalendar();
    modi.setTime(format.parse(modified));
    cve.setModified(modi);

    String cvss3_version = null;
    Double cvss3_score = null;
    String cvss3_vector = null;
    try {
      cvss3_version = JsonPath.read(document, "$.result.CVE_Items[0].impact.baseMetricV3.cvssV3.version");
      cvss3_score = JsonPath.read(document, "$.result.CVE_Items[0].impact.baseMetricV3.cvssV3.baseScore");
      cvss3_vector = JsonPath.read(document, "$.result.CVE_Items[0].impact.baseMetricV3.cvssV3.vectorString");
    } catch (Exception e) {
      log.warn(
          "Exception when reading CVSS v3 information for CVE [" + _id + "]: " + e.getMessage());
    }

    String cvss2_version = null;
    Double cvss2_score = null;
    String cvss2_vector = null;
    try {
      cvss2_version = JsonPath.read(document, "$.result.CVE_Items[0].impact.baseMetricV2.cvssV2.version");
      cvss2_score = JsonPath.read(document, "$.result.CVE_Items[0].impact.baseMetricV2.cvssV2.baseScore");
      cvss2_vector = JsonPath.read(document, "$.result.CVE_Items[0].impact.baseMetricV2.cvssV2.vectorString");
    } catch (Exception e) {
      log.warn(
          "Exception when reading CVSS v2 information for CVE [" + _id + "]: " + e.getMessage());
    }

    if (cvss3_version != null && cvss3_score != null && cvss3_vector != null) {
      cve.setCvssScore(cvss3_score.floatValue());
      cve.setCvssVector(cvss3_vector);
      cve.setCvssVersion(cvss3_version);
    } else if (cvss2_version != null && cvss2_score != null && cvss2_vector != null) {
      cve.setCvssScore(cvss2_score.floatValue());
      cve.setCvssVector(cvss2_vector);
      cve.setCvssVersion(cvss2_version);
    }

    return cve;
  }
}
