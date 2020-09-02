package org.eclipse.steady.kb.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
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
public class ImportAffectedLibraries {
  private Vulnerability vuln;
  private HashMap<String, Object> args;
  private static final String OVERWRITE_OPTION = "o";
  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  public ImportAffectedLibraries(Vulnerability vuln, HashMap<String, Object> args) {
    this.vuln = vuln;
    this.args = args;
  }

  public void execute()
      throws MalformedPackageURLException, BackendConnectionException, JsonProcessingException {
    List<Artifact> artifacts = vuln.getArtifacts();
    if (artifacts == null || artifacts.isEmpty()) {
      return;
    }

    Boolean overwrite = (Boolean) args.get(OVERWRITE_OPTION);

    List<AffectedLibrary> affectedLibsToUpsert = new ArrayList<AffectedLibrary>();

    for (Artifact artifact : artifacts) {
      PackageURL purl = new PackageURL(artifact.getId());

      AffectedLibrary[] affectedLibs =
          BackendConnector.getInstance()
              .getBugAffectedLibraries(
                  vuln.getVulnId(),
                  purl.getNamespace(),
                  purl.getName(),
                  purl.getVersion(),
                  AffectedVersionSource.KAYBEE);
      if (affectedLibs != null && affectedLibs.length > 0) {
        AffectedLibrary affectedLibrary = affectedLibs[0];
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
        org.eclipse.steady.shared.json.model.Artifact ciaArtifact =
            BackendConnector.getInstance()
                .getArtifact(purl.getNamespace(), purl.getName(), purl.getVersion());

        if (ciaArtifact == null) {
          continue;
        }

        AffectedLibrary affectedLibrary = new AffectedLibrary();
        affectedLibrary.setLibraryId(
            new LibraryId(purl.getNamespace(), purl.getName(), purl.getVersion()));
        setAfftectedLib(artifact, affectedLibrary);
        affectedLibsToUpsert.add(affectedLibrary);
      }
    }

    if (!affectedLibsToUpsert.isEmpty()) {
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(affectedLibsToUpsert.toArray());

      BackendConnector.getInstance()
          .uploadBugAffectedLibraries(null, vuln.getVulnId(), json, AffectedVersionSource.KAYBEE);
    }
  }

  private void setAfftectedLib(Artifact artifact, AffectedLibrary affectedLibrary) {
    affectedLibrary.setAffected(artifact.getAffected());
    affectedLibrary.setExplanation(artifact.getReason());
    affectedLibrary.setAffectedcc(Collections.<AffectedConstructChange>emptyList());
    affectedLibrary.setSource(AffectedVersionSource.KAYBEE);
  }
}