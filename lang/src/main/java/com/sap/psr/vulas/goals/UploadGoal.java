package com.sap.psr.vulas.goals;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.shared.enums.GoalType;

public class UploadGoal extends AbstractAppGoal {

	public UploadGoal() { super(GoalType.UPLOAD); }

	@Override
	protected void executeTasks() throws Exception {
		BackendConnector.getInstance().batchUpload(this.getGoalContext());
	}
}