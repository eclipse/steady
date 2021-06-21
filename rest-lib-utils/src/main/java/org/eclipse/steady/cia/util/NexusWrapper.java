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
package org.eclipse.steady.cia.util;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.steady.cia.model.nexus.NexusArtifact;
import org.eclipse.steady.cia.model.nexus.NexusArtifactInfoResourceResponse;
import org.eclipse.steady.cia.model.nexus.NexusArtifactResolution;
import org.eclipse.steady.cia.model.nexus.NexusResolvedArtifact;
import org.eclipse.steady.cia.model.nexus.NexusSearchNGResponse;
import org.eclipse.steady.cia.util.ArtifactDownloader.DefaultRequestCallback;
import org.eclipse.steady.cia.util.ArtifactDownloader.FileResponseExtractor;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.Artifact;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * <p>NexusWrapper class.</p>
 */
public class NexusWrapper implements RepositoryWrapper {

  private static Logger log = LoggerFactory.getLogger(NexusWrapper.class);

  private static String searchUrl;
  private static String serviceUrl;
  private static String timestampUrl;

  private static boolean CONFIGURED = false;

  private static Set<ProgrammingLanguage> SUPP_LANG = new HashSet<ProgrammingLanguage>();

  static {
    SUPP_LANG.add(ProgrammingLanguage.JAVA);
    searchUrl =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getString("vulas.lib-utils.nexus.search", null);
    serviceUrl =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getString("vulas.lib-utils.nexus.service", null);
    timestampUrl =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getString("vulas.lib-utils.nexus.servicerepo", null);
    if (searchUrl != null && serviceUrl != null && timestampUrl != null) CONFIGURED = true;
  }

  /** {@inheritDoc} */
  @Override
  public Set<ProgrammingLanguage> getSupportedLanguages() {
    return SUPP_LANG;
  }

  private NexusSearchNGResponse searchInNexus(
      String _g, String _a, String _v, String _c, String _p, String _sha1)
      throws InterruptedException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("g", (_g != null) ? _g : "");
    params.put("a", (_a != null) ? _a : "");
    params.put("v", (_v != null) ? _v : "");
    params.put("p", (_p != null) ? _p : "");
    params.put("c", (_c != null) ? _c : "");
    params.put("sha1", (_sha1 != null) ? _sha1 : "");

    final RestTemplate rest_template = new RestTemplate();
    ResponseEntity<NexusSearchNGResponse> responseEntity = null;

    if (this.searchUrl == null) {
      log.warn("No url configured for searching in Nexus, skip NexusWrapper");
    } else {
      try {
        responseEntity = rest_template.getForEntity(searchUrl, NexusSearchNGResponse.class, params);
      } catch (HttpServerErrorException he) {
        NexusWrapper.log.error(
            "HttpServerErrorException: Received status ["
                + he.getStatusCode()
                + "] calling url ["
                + searchUrl
                + "]");
      } catch (HttpClientErrorException he) {
        NexusWrapper.log.warn(
            "HttpClientErrorException: Received status ["
                + he.getStatusCode()
                + "] calling url ["
                + searchUrl
                + "]");
      }
    }
    if (responseEntity != null
        && responseEntity.getStatusCode().is2xxSuccessful()
        && responseEntity.getBody() != null) return responseEntity.getBody();
    return null;
  }

  private Artifact resolveInNexus(
      String group, String artifact, String version, String classifier, String packaging) {
    final Map<String, String> params = new HashMap<String, String>();
    params.put("g", group);
    params.put("a", artifact);
    params.put("v", version);
    params.put("p", packaging);
    params.put("cl", classifier);
    params.put("action", "resolve");
    params.put("r", "build.snapshots");

    NexusResolvedArtifact response = null;

    final RestTemplate rest_template = new RestTemplate();
    ResponseEntity<NexusArtifactResolution> responseEntity = null;

    if (this.serviceUrl == null) {
      log.warn("No url configured for resolving artifact in Nexus, skip NexusWrapper");
    } else {
      try {
        responseEntity =
            rest_template.getForEntity(serviceUrl, NexusArtifactResolution.class, params);
      } catch (HttpServerErrorException he) {
        NexusWrapper.log.error(
            "Received internal server error ["
                + he.getStatusCode()
                + "] for url ["
                + serviceUrl
                + "] with params ["
                + params.get("g")
                + ":"
                + params.get("a")
                + ":"
                + params.get("v")
                + "]");
      } catch (HttpClientErrorException he) {
        NexusWrapper.log.error(
            "Http Client exception ["
                + he.getStatusCode()
                + "] received when calling url ["
                + serviceUrl
                + "]with params ["
                + params.get("g")
                + ":"
                + params.get("a")
                + ":"
                + params.get("v")
                + ":"
                + params.get("cl")
                + ":"
                + params.get("p")
                + "]");
      }
    }
    if (responseEntity != null && responseEntity.getBody() != null)
      response = responseEntity.getBody().getData();
    Artifact result = null;
    if (response != null) {
      result = new Artifact(response.getGroupId(), response.getArtifactId(), response.getVersion());
      result.setProgrammingLanguage(ProgrammingLanguage.JAVA);
      ResponseEntity<NexusArtifactInfoResourceResponse> responseForTimestamp = null;

      if (this.timestampUrl == null) {
        log.warn("No url configured for retriving artifact timestamp in Nexus, skip NexusWrapper");
      } else {
        final Map<String, String> params1 = new HashMap<String, String>();
        params1.put("artifact", response.getRepositoryPath());
        try {
          responseForTimestamp =
              rest_template.getForEntity(
                  timestampUrl, NexusArtifactInfoResourceResponse.class, params1);
        } catch (HttpServerErrorException he) {
          NexusWrapper.log.error(
              "Received internal server error ["
                  + he.getStatusCode()
                  + "] for url ["
                  + responseForTimestamp
                  + "]");
        } catch (HttpClientErrorException he) {
          NexusWrapper.log.error(
              "Http Client exception ["
                  + he.getStatusCode()
                  + "] received when calling url ["
                  + responseForTimestamp
                  + "]");
        }
      }
      if (responseForTimestamp != null && responseForTimestamp.getBody() != null)
        result.setTimestamp(responseForTimestamp.getBody().getData().getUploaded());
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Set<Artifact> getAllArtifactVersions(
      String group, String artifact, String classifier, String packaging)
      throws InterruptedException {

    final NexusSearchNGResponse nexusResp =
        this.searchInNexus(group, artifact, "", classifier, packaging, null);

    Set<Artifact> result = new TreeSet<Artifact>();

    if (nexusResp != null
        && nexusResp.getData() != null
        && nexusResp.getData().getArtifactList() != null) {
      for (NexusArtifact a : nexusResp.getData().getArtifactList()) {
        // TODO (2018-04-19): To get the timestamp we would need an additional get for each artifact
        // (the same done in getArtifactVersion)
        result.add(new Artifact(a.getGroupId(), a.getArtifactId(), a.getVersion()));
      }
    } else {
      log.info(
          "No artifact found for group ["
              + group
              + "], artifact ["
              + artifact
              + "], classifier ["
              + classifier
              + "], packaging ["
              + packaging
              + "] in Nexus");
    }

    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Set<Artifact> getGreaterArtifactVersions(
      String group, String artifact, String greaterThanVersion, String classifier, String packaging)
      throws Exception {

    Set<Artifact> all = this.getAllArtifactVersions(group, artifact, classifier, packaging);

    Set<Artifact> allWithTimestamp = new TreeSet<Artifact>();
    Artifact toCompare = null;
    for (Artifact a : all) {
      if (a.getLibId().getVersion().equals(greaterThanVersion))
        toCompare =
            this.getArtifactVersion(
                a.getLibId().getMvnGroup(),
                a.getLibId().getArtifact(),
                a.getLibId().getVersion(),
                null,
                null,
                null);
      else
        allWithTimestamp.add(
            this.getArtifactVersion(
                a.getLibId().getMvnGroup(),
                a.getLibId().getArtifact(),
                a.getLibId().getVersion(),
                null,
                null,
                null));
    }
    // Return bad request if version is not found
    if (toCompare == null) {
      log.error(
          "Version ["
              + greaterThanVersion
              + "] not found in Nexus for group ["
              + group
              + "] and artifact ["
              + artifact
              + "]");
      return null;
    }

    Set<Artifact> greater = new TreeSet<Artifact>();

    for (Artifact t : allWithTimestamp) {
      if (t.compareTo(toCompare) > 0) greater.add(t);
    }

    return greater;
  }

  /** {@inheritDoc} */
  @Override
  public Artifact getLatestArtifactVersion(
      String group, String artifact, String classifier, String packaging) {
    return this.resolveInNexus(group, artifact, "RELEASE", classifier, packaging);
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
    return this.resolveInNexus(group, artifact, version, classifier, packaging);
  }

  /** {@inheritDoc} */
  @Override
  public Path downloadArtifact(Artifact a) throws Exception {
    final Map<String, String> params = new HashMap<String, String>();
    params.put("g", a.getLibId().getMvnGroup());
    params.put("a", a.getLibId().getArtifact());
    params.put("v", a.getLibId().getVersion());
    params.put("p", a.getPackaging());
    params.put("cl", a.getClassifier());
    params.put("action", "content");
    params.put("r", "build.snapshots");

    final RestTemplate rest_template = new RestTemplate();
    Path result = null;
    if (this.serviceUrl == null) {
      log.warn("No url configured for downloading artifact from Nexus, skip NexusWrapper");
    } else {
      try {
        rest_template.execute(
            serviceUrl,
            HttpMethod.GET,
            new DefaultRequestCallback(),
            new FileResponseExtractor(a, a.getAbsM2Path()),
            params);
        result = a.getAbsM2Path();
      } catch (HttpServerErrorException he) {
        // exception thrown when 5xx is received
        NexusWrapper.log.error(
            "Received code ["
                + he.getStatusCode()
                + "] for url ["
                + serviceUrl
                + "] while downloading artifact ["
                + a.toString()
                + "]");
      } catch (HttpClientErrorException he) {
        // exception thrown when 4xx is received
        NexusWrapper.log.warn(
            "Received code ["
                + he.getStatusCode()
                + "] downloading artifact ["
                + a.toString()
                + "] from url ["
                + serviceUrl
                + "]");
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Artifact getArtifactForDigest(String digest) throws InterruptedException {
    // currently it only returns the lib id, to check whether package classifier and timestamp can
    // be obtained (if needed)

    NexusSearchNGResponse nexusResp =
        this.searchInNexus(null, null, null, null, null, digest.toLowerCase());

    Artifact result = null;
    if (nexusResp.getData() != null
        && nexusResp.getData().getArtifactList() != null
        && nexusResp.getData().getArtifactList().size() == 1) {
      result =
          new Artifact(
              nexusResp.getData().getArtifactList().get(0).getGroupId(),
              nexusResp.getData().getArtifactList().get(0).getArtifactId(),
              nexusResp.getData().getArtifactList().get(0).getVersion());
      result.setProgrammingLanguage(ProgrammingLanguage.JAVA);
    } else if (nexusResp.getData().getArtifactList() != null
        && nexusResp.getData().getArtifactList().size() > 1) {
      log.error("More than one artifact found for SHA1 [" + digest + "] in Nexus");
    } else {
      log.info("No artifact found for SHA1 [" + digest + "] in Nexus");
    }

    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isConfigured() {
    return CONFIGURED;
  }
}
