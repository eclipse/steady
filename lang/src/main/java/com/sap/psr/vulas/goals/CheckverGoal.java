package com.sap.psr.vulas.goals;

import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.sign.SignatureAnalysis;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** CheckverGoal class. */
public class CheckverGoal extends AbstractAppGoal {

  private static final Log log = LogFactory.getLog(CheckverGoal.class);

  private StringList bugsWhitelist = new StringList();

  /** Constructor for CheckverGoal. */
  public CheckverGoal() {
    super(GoalType.CHECKVER);
  }

  /**
   * Used to specify the bugs for which the analysis will be done.
   *
   * @param _bugs a {@link java.lang.String} object.
   */
  public void addToBugsWhitelist(String _bugs) {
    if (_bugs != null && !_bugs.equals("")) this.bugsWhitelist.addAll(_bugs, ",", true);
  }

  private URLClassLoader getClassLoader() {
    final List<URL> urls = new ArrayList<URL>();
    Set<Path> dep_jars = this.getKnownDependencies().keySet();
    for (Path d : dep_jars) {
      try {
        urls.add(d.toFile().toURI().toURL());
      } catch (MalformedURLException e) {
        log.error("No URL for dependency [" + d + "]");
      }
    }
    return new URLClassLoader(urls.toArray(new URL[urls.size()]));
  }

  /** {@inheritDoc} */
  @Override
  protected void executeTasks() throws Exception {
    SignatureAnalysis signatureAnalysis = SignatureAnalysis.getInstance();
    signatureAnalysis.setUrlClassLoader(this.getClassLoader());
    signatureAnalysis.setIsCli(true);
    // signatureAnalysis.setPath(dep_path);
    signatureAnalysis.setApp(this.getApplicationContext());
    signatureAnalysis.setBugs(this.bugsWhitelist);
    signatureAnalysis.execute();
  }
}
