package com.sap.psr.vulas.tasks;

import com.sap.psr.vulas.shared.json.model.Application;

/** Abstract AbstractBomTask class. */
public abstract class AbstractBomTask extends AbstractTask implements BomTask {

  private Application app = null;

  /**
   * setCompletedApplication.
   *
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   */
  protected void setCompletedApplication(Application _app) {
    this.app = _app;
  }

  /** {@inheritDoc} */
  @Override
  public Application getCompletedApplication() {
    return app;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
