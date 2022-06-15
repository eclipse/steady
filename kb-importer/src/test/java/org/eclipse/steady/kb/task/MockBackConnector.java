package org.eclipse.steady.kb.task;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.goals.GoalContext;
import org.eclipse.steady.shared.enums.AffectedVersionSource;
import org.eclipse.steady.shared.json.model.AffectedLibrary;
import org.eclipse.steady.shared.json.model.Artifact;
import org.eclipse.steady.shared.json.model.Bug;
import org.eclipse.steady.shared.json.model.LibraryId;

public class MockBackConnector extends BackendConnector {
  private String uploadJson;
  private List<String> uploadedLibraries = new ArrayList<String>();
  private List<String> uploadedChangeLists = new ArrayList<String>();

  @Override
  public AffectedLibrary[] getBugAffectedLibraries(
      String _bugId,
      String _group,
      String _artifact,
      String _version,
      AffectedVersionSource _source)
      throws BackendConnectionException {
    if (_artifact.equals("javaee-api") && _version.equals("6.0")) {
      AffectedLibrary[] arr1 = {
        new AffectedLibrary(new Bug(_bugId), new LibraryId("javax", "javaee-api", "6.0"), true)
      };
      return arr1;
    }
    return null;
  }

  @Override
  public Artifact getArtifact(String _g, String _a, String _v) throws BackendConnectionException {
    if (_a.equals("javax.faces") && _v.equals("2.3.7")) {
      return null;
    }
    return new Artifact(_g, _a, _v);
  }

  @Override
  public Artifact[] getAllArtifactsGroupArtifact(String _g, String _a)
      throws BackendConnectionException {
    ArrayList<Artifact> artifacts = new ArrayList<>();
    if (_g.equals("javax") && _a.equals("javaee-api")) {
      artifacts.add(new Artifact("javax", "javaee-api", "6.0"));
      artifacts.add(new Artifact("javax", "javaee-api", "7.0"));
      artifacts.add(new Artifact("javax", "javaee-api", "8.0"));
    } else if (_g.equals("javax") && _a.equals("javaee-web-api")) {
      artifacts.add(new Artifact("javax", "javaee-web-api", "6.0"));
      artifacts.add(new Artifact("javax", "javaee-web-api", "7.0"));
      artifacts.add(new Artifact("javax", "javaee-web-api", "8.0.1"));
    } else {
      return null;
    }

    return artifacts.toArray(new Artifact[artifacts.size()]);
  }

  @Override
  public void uploadBugAffectedLibraries(
      GoalContext _g, String _bugId, String _json, AffectedVersionSource _source)
      throws BackendConnectionException {
    uploadJson = _json;
    uploadedLibraries.add(_json);
  }

  @Override
  public void uploadChangeList(String _bug, String _json) throws BackendConnectionException {
    uploadJson = _json;
    uploadedChangeLists.add(_json);
  }

  @Override
  public String getCVE(String _bugId) throws BackendConnectionException {
    return "{\n" + "  \"summary\": \"Test Desc\"\n" + "}";
  }

  @Override
  public boolean isBugExisting(String _bug) throws BackendConnectionException {
    if (_bug.equals("CVE-TEST01")) {
      return true;
    }
    return false;
  }

  public String getUploadJson() {
    return uploadJson;
  }

  public List<String> getUploadedChangeLists() {
    return uploadedChangeLists;
  }

  public List<String> getUploadedLibraries() {
    return uploadedLibraries;
  }
}
