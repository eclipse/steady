package com.sap.psr.vulas.goals;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.shared.enums.GoalType;

/**
 * <p>UploadGoal class.</p>
 *
 */
public class UploadGoal extends AbstractAppGoal {

	/**
	 * <p>Constructor for UploadGoal.</p>
	 */
	public UploadGoal() { super(GoalType.UPLOAD); }

	/** {@inheritDoc} */
	@Override
	protected void executeTasks() throws Exception {
		BackendConnector.getInstance().batchUpload(this.getGoalContext());
	}
}
