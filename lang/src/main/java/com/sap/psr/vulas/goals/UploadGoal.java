package com.sap.psr.vulas.goals;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.shared.enums.GoalType;

/** UploadGoal class. */
public class UploadGoal extends AbstractAppGoal {

  /** Constructor for UploadGoal. */
  public UploadGoal() {
    super(GoalType.UPLOAD);
  }

  /** {@inheritDoc} */
  @Override
  protected void executeTasks() throws Exception {
    BackendConnector.getInstance().batchUpload(this.getGoalContext());
  }
}
