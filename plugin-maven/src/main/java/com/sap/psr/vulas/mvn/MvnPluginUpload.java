package com.sap.psr.vulas.mvn;

import com.sap.psr.vulas.goals.UploadGoal;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Uploads all JSON files contained in the upload folder, i.e., which have been produced in previous
 * goal executions. Files are deleted after successful upload.
 */
@Mojo(name = "upload", defaultPhase = LifecyclePhase.VERIFY, requiresOnline = true)
public class MvnPluginUpload extends AbstractVulasMojo {

  /** {@inheritDoc} */
  @Override
  protected void createGoal() {
    this.goal = new UploadGoal();
  }
}
