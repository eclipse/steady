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
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.cia.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.sap.psr.vulas.cia.util.ArtifactDownloader.DefaultRequestCallback;
import com.sap.psr.vulas.cia.util.ArtifactDownloader.FileResponseExtractor;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.mavenCentral.MavenVersionsSearch;
import com.sap.psr.vulas.shared.json.model.mavenCentral.ResponseDoc;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>MavenCentralWrapper class.</p>
 *
 */
public class MavenCentralWrapper implements RepositoryWrapper {

  private static final String baseUrl;

  private static String MAVEN_CENTRAL_REPO;

  private static Logger log = LoggerFactory.getLogger(MavenCentralWrapper.class);

  private static Integer mavenCentralDelay;

  private static Integer mavenCentralRetryCount;

  private static boolean CONFIGURED = false;

  private static Set<ProgrammingLanguage> SUPP_LANG = new HashSet<ProgrammingLanguage>();

  static {
    SUPP_LANG.add(ProgrammingLanguage.JAVA);
    MAVEN_CENTRAL_REPO =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getString("vulas.lib-utils.mavencentral.repo", null);
    baseUrl =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getString("vulas.lib-utils.mavencentral.search", null);
    mavenCentralDelay =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getInteger("vulas.lib-utils.mavencentral.retrydelay", 10000);
    mavenCentralRetryCount =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getInteger("vulas.lib-utils.mavencentral.retrycount", 3);
    if (MAVEN_CENTRAL_REPO != null && baseUrl != null) CONFIGURED = true;
  }

  /** {@inheritDoc} */
  @Override
  public Set<ProgrammingLanguage> getSupportedLanguages() {
    return SUPP_LANG;
  }

  private StringBuilder constructQ(String _g, String _a, String _v, String _c, String _p) {
    final StringBuilder query = new StringBuilder();
    query.append("g:\"").append(_g).append("\" AND ");
    query.append("a:\"").append(_a).append("\"");
    if (_v != null && !_v.equals("")) query.append(" AND v:\"").append(_v).append("\"");
    if (_c != null && !_c.equals("")) query.append(" AND l:\"").append(_c).append("\"");
    if (_p != null && !_p.equals("")) query.append(" AND p:\"").append(_p).append("\"");
    return query;
  }

  private MavenVersionsSearch getFromMavenCentral(Map<String, String> _params)
      throws InterruptedException {
    final RestTemplate rest_template = new RestTemplate();
    ResponseEntity<MavenVersionsSearch> responseEntity = null;

    for (Integer i = 1; i < this.mavenCentralRetryCount + 1; i++) {

      try {
        responseEntity = rest_template.getForEntity(baseUrl, MavenVersionsSearch.class, _params);
        break;
      } catch (HttpServerErrorException he) {
        MavenCentralWrapper.log.error(
            "HttpServerErrorException: Received status code ["
                + he.getStatusCode()
                + "] calling url ["
                + baseUrl
                + "] with app ["
                + _params.get("q")
                + "], retry from ["
                + i
                + "]");
        Thread.sleep(i * mavenCentralDelay);

      } catch (HttpClientErrorException he) {
        MavenCentralWrapper.log.warn(
            "HttpClientErrorException: Received status code ["
                + he.getStatusCode()
                + "] calling url ["
                + baseUrl
                + "] with app ["
                + _params.get("q")
                + "] :"
                + he.getMessage());
      }
    }

    if (responseEntity != null) return responseEntity.getBody();
    else {
      log.warn(
          "Maven central was unavailable and could not serve the request ["
              + baseUrl
              + "] with params ["
              + _params.get("q")
              + "]");
      return null;
    }
  }

  private Set<Artifact> getArtifactVersions(
      String mvnGroup,
      String artifact,
      String version,
      Boolean latest,
      String greaterThanVersion,
      String classifier,
      String packaging)
      throws Exception {
    Set<Artifact> result = new TreeSet<Artifact>();
    try {

      final Map<String, String> params = new HashMap<String, String>();
      // NOTE: we do not use the packaging in the query to maven as e.g. jackson-databind 2.9.5 has
      // packaging bundle instead of jar
      // same for classifier
      params.put("q", constructQ(mvnGroup, artifact, version, null, null).toString());
      params.put("core", "gav");
      params.put("rows", "1000");
      params.put("wt", "json");

      // Make the query
      final MavenVersionsSearch search = this.getFromMavenCentral(params);

      // Set filters
      packaging = (packaging != null && packaging.equals("") ? null : packaging);
      classifier = (classifier != null && classifier.equals("") ? null : classifier);

      if (search != null) {
        // Client only wants the latest
        if (!search.getResponse().getDocs().isEmpty() && latest) {
          final TreeSet<ResponseDoc> filtered =
              this.filter(search.getResponse().getSortedDocs(), classifier, packaging);
          ResponseDoc latest_only = null;
          if (filtered.size() > 0) latest_only = filtered.last();
          if (latest_only != null) result.add(latest_only.toArtifact());

        }
        // Client only wants versions GT x
        else if (!search.getResponse().getDocs().isEmpty()
            && greaterThanVersion != null
            && !greaterThanVersion.equals("")) {

          // Find the timestamp of version X
          long timestamp = -1;
          for (ResponseDoc d : search.getResponse().getSortedDocs()) {
            if (d.getV().equals(greaterThanVersion)) {
              timestamp = d.getTimestamp();
              break;
            }
          }

          // Return bad request if version is not found
          if (timestamp == -1) {
            log.error(
                "Version ["
                    + greaterThanVersion
                    + "] not found in Maven Central for group ["
                    + mvnGroup
                    + "] and artifact ["
                    + artifact
                    + "]");
            return null;
          }

          // Find all with a timestamp after that of X
          for (ResponseDoc d : search.getResponse().getSortedDocs()) {
            if (d.getTimestamp() > timestamp && d.availableWith(classifier, packaging)) {
              result.add(d.toArtifact());
            }
          }

        }
        // Client wants all versions
        else if (!search.getResponse().getDocs().isEmpty()) {
          for (ResponseDoc d :
              this.filter(search.getResponse().getSortedDocs(), classifier, packaging)) {
            result.add(d.toArtifact());
          }
        }
      }

    } catch (Exception e) {
      log.error("Error: " + e.getMessage(), e);
      throw e;
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Path downloadArtifact(Artifact _doc) throws Exception {
    final String url = MAVEN_CENTRAL_REPO;
    final StringBuilder b = new StringBuilder();
    b.append(url);
    b.append(_doc.getLibId().getMvnGroup().replace('.', '/')).append("/");
    b.append(_doc.getLibId().getArtifact()).append("/");
    b.append(_doc.getLibId().getVersion()).append("/");
    b.append(_doc.getM2Filename());

    // Make the query
    final RestTemplate rest_template = new RestTemplate();
    Path result = null;
    try {
      rest_template.execute(
          b.toString(),
          HttpMethod.GET,
          new DefaultRequestCallback(),
          new FileResponseExtractor(_doc, _doc.getAbsM2Path()));
      result = _doc.getAbsM2Path();
    } catch (HttpClientErrorException e) {
      MavenCentralWrapper.log.error(_doc + " not available at [" + b.toString() + "]");
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Artifact getArtifactForDigest(String _sha1) throws RepoException {
    Artifact doc = null;
    try {
      // URL and params
      final StringBuilder query = new StringBuilder();
      query.append("1:\"").append(_sha1).append("\"");
      final Map<String, String> params = new HashMap<String, String>();
      params.put("q", query.toString());
      params.put("rows", "50");
      params.put("wt", "json");
      params.put("core", "gav");

      // Make the query
      final RestTemplate rest_template = new RestTemplate();
      final MavenVersionsSearch search =
          rest_template.getForObject(baseUrl, MavenVersionsSearch.class, params);

      // Return results
      final TreeSet<ResponseDoc> versions = new TreeSet<ResponseDoc>();
      versions.addAll(search.getResponse().getDocs());

      if (versions.isEmpty()) {
        log.info("No artifact found for SHA1 [" + _sha1 + "] in Maven Central");
      } else if (versions.size() == 1) {
        ResponseDoc d = versions.first();
        doc = new Artifact(d.getG(), d.getA(), d.getV());
        doc.setPackaging(d.getP());
        doc.setClassifier(d.getC());
        doc.setTimestamp(d.getTimestamp());
        doc.setProgrammingLanguage(ProgrammingLanguage.JAVA);
        log.info("Found artifact " + versions.first() + " for SHA1 [" + _sha1 + "]");
      } else {
        log.error(
            "Found ["
                + versions.size()
                + "] artifacts for SHA1 ["
                + _sha1
                + "], should be none or one only");
        throw new RepoException(
            "Found ["
                + versions.size()
                + "] artifacts for SHA1 ["
                + _sha1
                + "], should be none or one only");
      }
    } catch (Exception e) {
      log.error(
          "Error when searching for [" + _sha1 + "] in repo [" + baseUrl + "]: " + e.getMessage(),
          e);
      e.printStackTrace();
      throw new RepoException(
          "Error when searching for [" + _sha1 + "] in repo [" + baseUrl + "]: " + e.getMessage(),
          e);
    }
    return doc;
  }

  /** {@inheritDoc} */
  @Override
  public Set<Artifact> getAllArtifactVersions(
      String group, String artifact, String classifier, String packaging) throws Exception {
    return this.getArtifactVersions(group, artifact, null, false, null, classifier, packaging);
  }

  /** {@inheritDoc} */
  @Override
  public Set<Artifact> getGreaterArtifactVersions(
      String group, String artifact, String greaterThanVersion, String classifier, String packaging)
      throws Exception {
    return this.getArtifactVersions(
        group, artifact, null, false, greaterThanVersion, classifier, packaging);
  }

  /** {@inheritDoc} */
  @Override
  public Artifact getLatestArtifactVersion(
      String group, String artifact, String classifier, String packaging) throws Exception {
    Set<Artifact> a =
        this.getArtifactVersions(group, artifact, null, true, null, classifier, packaging);
    List<Artifact> list = new ArrayList<Artifact>(a);
    if (list.size() > 1) {
      throw new Exception(
          "More than one latest version found ["
              + a.size()
              + "] for GA ["
              + group
              + ":"
              + artifact
              + "]");
    }
    if (list.isEmpty()) return null;
    return list.get(0);
  }

  /** {@inheritDoc} */
  @Override
  public Artifact getArtifactVersion(
      String group,
      String artifact,
      String version,
      String classifier,
      String packaging,
      ProgrammingLanguage lang)
      throws Exception {
    Set<Artifact> a =
        this.getArtifactVersions(group, artifact, version, false, null, classifier, packaging);
    List<Artifact> list = new ArrayList<Artifact>(a);
    if (list.size() > 1) {
      throw new Exception(
          "More than one artifact found ["
              + a.size()
              + "] for GAV ["
              + group
              + ":"
              + artifact
              + ":"
              + version
              + (classifier != null ? ":" + classifier : "")
              + "]");
    }
    if (list.isEmpty()) return null;
    return list.get(0);
  }

  /**
   * <p>getArtifactForClass.</p>
   *
   * @param classname a {@link java.lang.String} object.
   * @param rows a {@link java.lang.String} object.
   * @param classifierFilter a {@link java.lang.String} object.
   * @param packagingFilter a {@link java.lang.String} object.
   * @return a {@link java.util.Set} object.
   * @throws java.lang.Exception if any.
   */
  public Set<Artifact> getArtifactForClass(
      String classname, String rows, String classifierFilter, String packagingFilter)
      throws Exception {
    final StringBuilder query = new StringBuilder();
    query.append("fc:\"").append(classname).append("\"");
    final Map<String, String> params = new HashMap<String, String>();
    params.put("q", query.toString());
    params.put("rows", rows);
    params.put("core", "gav");
    params.put("wt", "json");

    final MavenVersionsSearch search = this.getFromMavenCentral(params);

    final TreeSet<Artifact> all = new TreeSet<Artifact>();
    if (search != null) {
      for (ResponseDoc d :
          this.filter(search.getResponse().getSortedDocs(), classifierFilter, packagingFilter))
        all.add(d.toArtifact());
    }
    return all;
  }

  private TreeSet<ResponseDoc> filter(
      Collection<ResponseDoc> _input, String _classifier, String _packaging) {
    final TreeSet<ResponseDoc> filtered = new TreeSet<ResponseDoc>();
    for (ResponseDoc doc : _input) {
      if (doc.availableWith(_classifier, _packaging)) filtered.add(doc);
    }
    return filtered;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isConfigured() {
    return CONFIGURED;
  }
}
