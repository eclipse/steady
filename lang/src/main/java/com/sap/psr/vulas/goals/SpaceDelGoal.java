package com.sap.psr.vulas.goals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Space;

public class SpaceDelGoal extends AbstractSpaceGoal {
	
	private static final Log log = LogFactory.getLog(SpaceDelGoal.class);

	public SpaceDelGoal() { super(GoalType.SPACEDEL); }

	@Override
	protected void executeTasks() throws Exception {
		final Space s = this.getGoalContext().getSpace();

		final BackendConnector bc = BackendConnector.getInstance();

		// Check that space exists
		if(!bc.isSpaceExisting(this.getGoalContext(), s))
			throw new GoalExecutionException("Space with token [" + s.getSpaceToken() + "] does not exist", null);

		bc.deleteSpace(this.getGoalContext(), s);
	}
}