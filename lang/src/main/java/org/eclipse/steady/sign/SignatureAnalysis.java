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
package org.eclipse.steady.sign;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.util.FileSearch;
import org.eclipse.steady.shared.util.StringList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * <p>SignatureAnalysis class.</p>
 */
@SuppressFBWarnings(
    value = "URF_UNREAD_FIELD",
    justification =
        "Fields app, isCLI, depPath and uRLClassLoader appear not being used because the metod"
            + " body of method execute() is commented out")
public class SignatureAnalysis {
  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();
  private Application app;

  private StringList bugs;

  private HashMap<String, String> clParameters = new HashMap<String, String>();
  private URLClassLoader uRLClassLoader;
  private boolean isCLI;
  private Path depPath;
  /*
  Singleton Instance
  */
  private static SignatureAnalysis instance = null;

  /**
   * <p>Constructor for SignatureAnalysis.</p>
   */
  protected SignatureAnalysis() {
    super();
  }

  /**
   * <p>Getter for the field <code>instance</code>.</p>
   *
   * @return a {@link org.eclipse.steady.sign.SignatureAnalysis} object.
   */
  public static synchronized SignatureAnalysis getInstance() {
    if (instance == null) {
      instance = new SignatureAnalysis();
    }
    return instance;
  }
  /**
   * <p>setUrlClassLoader.</p>
   *
   * @param _ucl a {@link java.net.URLClassLoader} object.
   */
  public void setUrlClassLoader(URLClassLoader _ucl) {
    this.uRLClassLoader = _ucl;
  }

  /**
   * <p>Setter for the field <code>app</code>.</p>
   *
   * @param _a a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public void setApp(Application _a) {
    this.app = _a;
  }
  /**
   * <p>setBug.</p>
   *
   * @param _bugs a {@link java.lang.String} object.
   */
  public void setBug(String _bugs) {
    this.bugs = new StringList();
    this.bugs.addAll(_bugs, ",", true);
  }
  /**
   * <p>Setter for the field <code>bugs</code>.</p>
   *
   * @param _bugs a {@link org.eclipse.steady.shared.util.StringList} object.
   */
  public void setBugs(StringList _bugs) {
    this.bugs = _bugs;
  }
  /**
   * <p>setCLParameter.</p>
   *
   * @param _param a {@link java.lang.String} object.
   * @param _value a {@link java.lang.String} object.
   */
  public void setCLParameter(String _param, String _value) {
    clParameters.put(_param, _value);
  }
  /**
   * <p>setIsCli.</p>
   *
   * @param _iscli a {@link java.lang.Boolean} object.
   */
  public void setIsCli(Boolean _iscli) {
    this.isCLI = _iscli;
  }
  /**
   * <p>setPath.</p>
   *
   * @param _p a {@link java.nio.file.Path} object.
   */
  public void setPath(Path _p) {
    this.depPath = _p;
  }

  /* execute the signature analysis */
  /**
   * <p>execute.</p>
   *
   * @throws java.lang.Exception if any.
   */
  public void execute() throws Exception {
    /*ArchiveFixContainmentCheck archive_check = null;

    Archive archive = null;
    Dependency dependency = null; // substitute of archive

    Artifact artifact = null;
    Path artifactPath = null;
    String bugid = null;
    Set<ConstructChange> construct_changes = null;
    Set<String> paths_to_src = null;
    boolean archive_fixed;

    // The results of the vesion check
    final JsonArray results = new JsonArray();

    // Needed for inlining constants
    JarAnalyzer ja = null;

    UniqueNameNormalizer.getInstance().setClassLoader(uRLClassLoader);

    if(VulasConfiguration.getSingleton().getConfiguration().getBoolean(SignatureConfiguration.RELAX_EDIT_SCRIPT, true)) {
        SourceCodeEntity.setEditScriptRelax(true);
    }

    // Get all constructs to be checked from the Vulas backend (only makes sense after the execution of vulas:app, which is when bug change lists and app archive constructs are joined)

    // get the list of vulnerable dependencies after having called /apps
    final VulnerableDependency[] vulndeps = BackendConnector.getInstance().getVulnerableAppArchiveConstructs(app);

    JsonArray finalResult = new JsonArray();

    for ( VulnerableDependency vulndep : vulndeps ){
        finalResult = new JsonArray();
        String sha1 = new String(vulndep.getDep().getLib().getSha1());
        String bugId = vulndep.getBug().getBugId();

        // Do not perform the GET request for the bugs not to be analyzed
        if(!bugs.isEmpty() && !bugs.contains(vulndep.getBug().getBugId(),  StringList.ComparisonMode.EQUALS, StringList.CaseSensitivity.CASE_INSENSITIVE)) {
            this.log.info("Version check skipped for bug [" + vulndep.getBug().getBugId() + "] and archive(dependency) [" + vulndep.getDep().getFilename() + "]");
            continue;
        }

        VulnerableDependency input = BackendConnector.getInstance().getVulnerableAppArchiveDependencyConstructs(app, sha1, bugId);

        // Get all construct changes (both for constructs contained and not contained in the archive)
        construct_changes = new HashSet<ConstructChange>();

        for( ConstructChangeInDependency cle : input.getConstructList() ){
            construct_changes.add(cle.getConstructChange());
        }

        vulndep.setConstructList(input.getConstructList());

        dependency = vulndep.getDep();
        LibraryId mavenid = dependency.getLib().getLibraryId();

        archive = new Archive();
        archive.setDigest(sha1);
        archive.setFilename(vulndep.getDep().getFilename());
        archive.setLibraryId(mavenid);
        archive_fixed = false;

        if ( !isCLI ){
            artifact = this.getDependency(dependency.getFilename(), mavenid);
        }
        artifactPath = this.getArtifactPath(artifact, archive);

        // Skip if artifact cannot be found among the dependencies
        // Can happen if a dependency is updated followed by the execution of vulas:app (w/o prior vulas:clean)
        // In that case the previously declared dependency is not found any longer
        if(artifactPath==null) {
                this.log.warn("Archive [filename=" + dependency.getFilename() + ", GAV="
                        + mavenid + "] not found among declared dependencies, skip analysis");
                continue;
        }

        // Add all class names of the current archive, to be prepared for the inlining of constants in SourceCodeEntity
        ja = new JarAnalyzer(artifactPath.toString(), false);
        UniqueNameNormalizer.getInstance().addStrings(ja.getClassNames());

        // add all transitive dependencies for this artifact ( if not running on cli )
        if (!isCLI){
            findAllDependencies(artifact);
        }
        // Get distinct changes for trunk and branches (if any)
        paths_to_src = ConstructChange.getRepoPathsToSrc(construct_changes);

        int x = 0;

        JsonObject result = new JsonObject();
        JsonObject jlib = new JsonObject();
        jlib.addProperty("sha1", vulndep.getDep().getLib().getSha1());
        result.add("lib", jlib);
        JsonArray partialPathResults = new JsonArray();
        int fixed_paths = 0;

        // Check fix containment separately for each path
        for(String path_to_src: paths_to_src) {
            this.log.info("Version check to be done for bug [" + bugId + "], archive [" + archive.getFilename() + "], path to src [" + path_to_src + "]");
            boolean fixed_on_path = false;

            archive_check = new ArchiveFixContainmentCheck(bugId, artifactPath);

            archive_check.addConstructChanges(ConstructChange.filter(construct_changes, path_to_src));

            fixed_on_path = archive_check.containsFix();

            // do not consider a path as fixed if there is no construct
            if ( archive_check.getConstructsFixedCount() == 0 && fixed_on_path ){
                fixed_on_path = false;
            }

            archive_fixed = fixed_on_path || archive_fixed;

            // output json object cc for current path
            partialPathResults.addAll(archive_check.toJson());
            this.log.info("Version check done for bug [" + bugId + "], archive [" + archive.getFilename() + "], path to src ["
                    + path_to_src + "]: archive is " + (fixed_on_path?"FIXED":"VULNERABLE") + " on this path, constructs fixed/vulnerable ["
                    + archive_check.getConstructsFixedCount() + "/" + archive_check.getConstructsVulnerableCount() + "]");

            fixed_paths = (fixed_on_path? ++fixed_paths : fixed_paths);

        }

        this.log.info("Version check done for bug [" + bugId + "], archive [" + archive.getFilename() + "], archive is GLOBALLY " +
                    (archive_fixed?"FIXED":"VULNERABLE") + " , paths fixed/vulnerable ["
                    + fixed_paths + "/" + (paths_to_src.size()-fixed_paths) + "]");

        if ( partialPathResults.size() != 0 ){
            result.add("affectedcc", partialPathResults);
            // delete before insert since update is not supported ( yet )

            // with the new backend update must be performed as soon as a bugid has been analyzed
            result.addProperty("source", "CHECK_VERSION");
            result.addProperty("affected", !archive_fixed);
            finalResult.add(result);
            String resultsToUpload = finalResult.toString();

            // upload results for the current CVE
            BackendConnector.getInstance().uploadCheckVersionResults(bugId, finalResult.toString());
        }
    }*/
  }

  @Deprecated
  /*private URLClassLoader getClassLoader() {
      final List<URL> urls = new ArrayList<URL>();
      for(Artifact a: this.project.getArtifacts()) {
          try {
              urls.add(a.getFile().toURL());
          } catch (MalformedURLException ex) {
              Logger.getLogger(SignatureAnalysis.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
      return new URLClassLoader(urls.toArray(new URL[urls.size()]));
  }*/

  private static Set<Path> getDependencies(Path _p) {
    return new FileSearch(new String[] {"jar"}).search(_p);
  }

  /*private void findAllDependencies( Artifact _a ){
      String thisArtifact = _a.getGroupId()+":"+_a.getArtifactId()+":"+_a.getType()+":"+_a.getBaseVersion();

      for(int i=0; i<this.artifacts.size(); i++){
          Artifact tmp = Iterables.get(this.artifacts, i);
          if ( tmp.getDependencyTrail().contains(thisArtifact) && !tmp.equals(_a) ){
              // if the artifact is in the dependency trail of one of the other artifacts,
              JarAnalyzer ja;
              try {
                  ja = new JarAnalyzer(tmp.getFile().toPath().toString(), false);
                  UniqueNameNormalizer.getInstance().addStrings(ja.getClassNames());
              } catch (IllegalStateException ise) {
                  Logger.getLogger(SignatureAnalysis.class.getName()).log(Level.SEVERE, null, ise);
              } catch (IOException ioe) {
                  Logger.getLogger(SignatureAnalysis.class.getName()).log(Level.SEVERE, null, ioe);
              }
          }
      }
  }*/

  /*private Path getArtifactPath(Artifact artifact, Archive archive) {
      Path artifactPath = null;
      if ( !isCLI ){
          artifactPath = artifact.getFile().toPath();
      } else {
          artifactPath = FileUtil.findMatch(this.getDependencies(depPath), archive.getFilename(), archive.getDigest());
      }
      return artifactPath;
  }*/

}
