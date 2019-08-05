package com.sap.psr.vulas.goals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Space;

/**
 * <p>SpaceModGoal class.</p>
 *
 */
public class SpaceModGoal extends AbstractSpaceGoal {

	private static final Log log = LogFactory.getLog(SpaceModGoal.class);

	/**
	 * <p>Constructor for SpaceModGoal.</p>
	 */
	public SpaceModGoal() { super(GoalType.SPACEMOD); }

	/** {@inheritDoc} */
	@Override
	protected void executeTasks() throws Exception {
		final Space s = this.getGoalContext().getSpace();
		this.updateFromConfig(s);
		
		final BackendConnector bc = BackendConnector.getInstance();

		// Check that name and description are provided
		if(!s.hasNameAndDescription())
			throw new GoalExecutionException("Space modification requires a name and description, adjust the configuration accordingly", null);

		// Check that space exists
		if(!bc.isSpaceExisting(this.getGoalContext(), s))
			throw new GoalExecutionException("Space with token [" + s.getSpaceToken() + "] does not exist", null);

		bc.modifySpace(this.getGoalContext(), s);
	}
}
