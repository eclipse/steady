package com.sap.psr.vulas.cg;

import java.util.Set;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.enums.PathSource;
import com.sap.psr.vulas.shared.util.ConstructIdUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class T2CGoal extends AbstractReachGoal {

	private Set<com.sap.psr.vulas.shared.json.model.ConstructId> entryPoints = null;

	private Set<com.sap.psr.vulas.shared.json.model.ConstructId> tracedConstructs = null;

	public T2CGoal() { super(GoalType.T2C); }

	protected final Set<com.sap.psr.vulas.shared.json.model.ConstructId> getEntryPoints() {
		if(this.entryPoints==null) {
			try {
				// Get traces
				this.tracedConstructs = BackendConnector.getInstance().getAppTraces(this.getGoalContext(), this.getApplicationContext());
				
				// Filter constructs (if requested)
				final String[] filter = this.getConfiguration().getConfiguration().getStringArray(ReachabilityConfiguration.REACH_CONSTR_FILTER);
				if(filter!=null && filter.length>0 && !(filter.length==1 && filter[0].equals(""))) {
					this.entryPoints = ConstructIdUtil.filterWithRegex(this.tracedConstructs, filter);
				} else {
					this.entryPoints = this.tracedConstructs;
				}
			} catch (BackendConnectionException e) {
				throw new IllegalStateException(e.getMessage());
			}
		}
		return this.entryPoints;
	}

	/**
	 * Sets the traced constructs as entry points of the {@link ReachabilityAnalyzer}.
	 */
	protected final void setEntryPoints(ReachabilityAnalyzer _ra) {
		_ra.setEntryPoints(this.getEntryPoints(), PathSource.T2C, this.getConfiguration().getConfiguration().getBoolean(ReachabilityConfiguration.REACH_EXIT_UNKOWN_EP, false));
	}
}