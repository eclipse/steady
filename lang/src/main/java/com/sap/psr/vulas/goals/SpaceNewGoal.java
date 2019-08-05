package com.sap.psr.vulas.goals;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Space;

/**
 * <p>SpaceNewGoal class.</p>
 *
 */
public class SpaceNewGoal extends AbstractSpaceGoal {
	
	private static final Log log = LogFactory.getLog(SpaceNewGoal.class);
	
	private Space createdSpace = null;

	/**
	 * <p>Constructor for SpaceNewGoal.</p>
	 */
	public SpaceNewGoal() { super(GoalType.SPACENEW); }
	
	/** {@inheritDoc} */
	@Override
	protected void executeTasks() throws Exception {
		final Space s = new Space();
		this.updateFromConfig(s);
		
		// Warn that any provided space token is ignored when creating a new space
		if(s.isValidSpaceToken())
			log.warn("Upon space creation a new token will be generated, the current token [" + s.getSpaceToken() + "] will be ignored");
		
		// Check that name and description are provided
		if(!s.hasNameAndDescription()) {
			this.skipGoalUpload();
			throw new GoalExecutionException("Space creation requires name and description, adjust the configuration accordingly", null);
		}
		
		// Create space and print token to be used in configuration
		this.createdSpace = BackendConnector.getInstance().createSpace(this.getGoalContext(), s);
		if(this.createdSpace==null) {
			log.error("Space creation failed");
		} else {
			log.info("Space creation succeeded: Use space token [" + this.createdSpace.spaceToken + "] for configuration setting [" + CoreConfiguration.SPACE_TOKEN + "]");
			
			// Update context so that goal execution is uploaded properly
			this.getGoalContext().setSpace(this.createdSpace);
		}
	}
	
	/**
	 * <p>getResultObject.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	protected Object getResultObject() {
		return this.createdSpace;
	}
}
