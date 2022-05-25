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
package org.eclipse.steady.kb.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.kb.ImportCommand;
import org.eclipse.steady.kb.model.Artifact;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.shared.enums.AffectedVersionSource;
import org.eclipse.steady.shared.json.model.AffectedConstructChange;
import org.eclipse.steady.shared.json.model.AffectedLibrary;
import org.eclipse.steady.shared.json.model.LibraryId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

/**
 * Imports affected libraries to steady
 * <p>
 * TODO: Handle regex for the purls given in the json
 * </p>
 */
public class ImportAffectedLibraries implements Task {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /** {@inheritDoc} */
  public void execute(
      Vulnerability vuln, HashMap<String, Object> args, BackendConnector backendConnector)
      throws MalformedPackageURLException, BackendConnectionException, JsonProcessingException {

    log.info("Initiating ImportAffectedLibraries for vulnerability " + vuln.getVulnId());

    List<Artifact> artifacts = vuln.getArtifacts();
    if (artifacts == null || artifacts.isEmpty()) {
      return;
    }
    if (args.containsKey(ImportCommand.DELETE) && (boolean) args.get(ImportCommand.DELETE)) {
      backendConnector.deletePatchEvalResults(vuln.getVulnId(), AffectedVersionSource.KAYBEE);
    }

    List<AffectedLibrary> affectedLibsToUpsert = new ArrayList<AffectedLibrary>();
    HashSet<org.eclipse.steady.shared.json.model.Artifact> ciaArtifactsCache = new HashSet<>();

    for (Artifact artifact : artifacts) {
      PackageURL purl = new PackageURL(artifact.getId());

      String purlGroup = purl.getNamespace();
      String purlArtifact = purl.getName();
      String purlVersion = purl.getVersion();
      AffectedLibrary[] affectedLibs =
          backendConnector.getBugAffectedLibraries(
              vuln.getVulnId(), purlGroup, purlArtifact, purlVersion, AffectedVersionSource.KAYBEE);
      if (affectedLibs != null && affectedLibs.length > 0) {
        AffectedLibrary affectedLibrary = affectedLibs[0];
        Boolean overwrite = (Boolean) args.get(ImportCommand.OVERWRITE_OPTION);
        if (overwrite || affectedLibrary.getAffected() == null) {
          setAfftectedLib(artifact, affectedLibrary);
          affectedLibsToUpsert.add(affectedLibrary);
        } else {
          log.info(
              "Affected version {} is already exists with source KAYBEE. Use option -o to"
                  + " overwrite the existing",
              artifact.getId());
        }
      } else {

        if (!ciaArtifactsCache.contains(
            new org.eclipse.steady.shared.json.model.Artifact(
                purlGroup, purlArtifact, purlVersion))) {
          org.eclipse.steady.shared.json.model.Artifact[] ciaArtifactsArr =
              backendConnector.getAllArtifactsGroupArtifact(purlGroup, purlArtifact);

          if (ciaArtifactsArr == null) {
            log.warn(
                "Affected version {} is not part of the configured repository.", artifact.getId());
            continue;
          }

          for (org.eclipse.steady.shared.json.model.Artifact ciaArtifact : ciaArtifactsArr) {
            LibraryId libId = ciaArtifact.getLibId();
            ciaArtifactsCache.add(
                new org.eclipse.steady.shared.json.model.Artifact(
                    libId.getMvnGroup(), libId.getArtifact(), libId.getVersion()));
          }

          if (!ciaArtifactsCache.contains(
              new org.eclipse.steady.shared.json.model.Artifact(
                  purlGroup, purlArtifact, purlVersion))) {
            log.warn(
                "Affected version {} is not part of the configured repository.", artifact.getId());
            continue;
          }
        }

        AffectedLibrary affectedLibrary = new AffectedLibrary();
        affectedLibrary.setLibraryId(new LibraryId(purlGroup, purlArtifact, purlVersion));
        setAfftectedLib(artifact, affectedLibrary);
        affectedLibsToUpsert.add(affectedLibrary);
      }
    }

    if (!affectedLibsToUpsert.isEmpty()) {
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(affectedLibsToUpsert.toArray());

      backendConnector.uploadBugAffectedLibraries(
          null, vuln.getVulnId(), json, AffectedVersionSource.KAYBEE);
    }

    log.info("ImportAffectedLibraries: " + vuln.getVulnId() + " complete");
  }

  private void setAfftectedLib(Artifact artifact, AffectedLibrary affectedLibrary) {
    affectedLibrary.setAffected(artifact.getAffected());
    affectedLibrary.setExplanation(artifact.getReason());
    affectedLibrary.setAffectedcc(Collections.<AffectedConstructChange>emptyList());
    affectedLibrary.setSource(AffectedVersionSource.KAYBEE);
  }

}
