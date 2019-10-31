package com.sap.psr.vulas.cia.dependencyfinder;

import com.jeantessier.classreader.AggregatingClassfileLoader;
import com.jeantessier.classreader.ClassfileLoader;
import com.jeantessier.classreader.PackageMapper;
import com.jeantessier.dependencyfinder.cli.DiffCommand;
import com.jeantessier.diff.Differences;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.diff.JarDiffResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** JarDiffCmd class. */
public class JarDiffCmd extends DiffCommand {

  private Artifact oldLib, newLib;
  private Path oldPath, newPath;
  private JarDiffVisitor visitor;

  /**
   * Constructor for JarDiffCmd.
   *
   * @param _old a {@link com.sap.psr.vulas.shared.json.model.Artifact} object.
   * @param _old_path a {@link java.nio.file.Path} object.
   * @param _new a {@link com.sap.psr.vulas.shared.json.model.Artifact} object.
   * @param _new_path a {@link java.nio.file.Path} object.
   */
  public JarDiffCmd(Artifact _old, Path _old_path, Artifact _new, Path _new_path) {
    this.oldLib = _old;
    this.oldPath = _old_path;
    this.newLib = _new;
    this.newPath = _new_path;
  }

  /**
   * doProcessing.
   *
   * @throws java.lang.Exception if any.
   */
  protected void doProcessing() throws Exception {
    // Old JAR
    PackageMapper oldPackages = new PackageMapper();
    ClassfileLoader oldJar = new AggregatingClassfileLoader();
    oldJar.addLoadListener(oldPackages);
    List<String> old_files = new ArrayList<String>();
    old_files.add(this.oldPath.toString());
    oldJar.load(old_files);

    // New JAR
    PackageMapper newPackages = new PackageMapper();
    ClassfileLoader newJar = new AggregatingClassfileLoader();
    newJar.addLoadListener(newPackages);
    List<String> new_files = new ArrayList<String>();
    new_files.add(this.newPath.toString());
    newJar.load(new_files);

    String name = this.oldLib.getLibId().getMvnGroup() + ":" + this.oldLib.getLibId().getArtifact();
    String oldLabel = name + ":" + oldLib.getLibId().getVersion();
    String newLabel = name + ":" + newLib.getLibId().getVersion();

    Differences differences =
        getDifferencesFactory()
            .createProjectDifferences(name, oldLabel, oldPackages, newLabel, newPackages);

    // Report report = new Report(getCommandLine().getSingleSwitch("encoding"),
    // getCommandLine().getSingleSwitch("dtd-prefix"));
    visitor = new JarDiffVisitor(this.oldLib, this.newLib);

    differences.accept(visitor);
  }

  /**
   * getResult.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.diff.JarDiffResult} object.
   * @throws java.lang.IllegalStateException if any.
   */
  public JarDiffResult getResult() throws IllegalStateException {
    if (visitor == null) throw new IllegalStateException("Processing did not start");
    return this.visitor.getJarDiffResult();
  }
}
