package com.sap.psr.vulas.cg;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.enums.PathSource;
import com.sap.psr.vulas.shared.util.ConstructIdUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>A2CGoal class.</p>
 *
 */
public class A2CGoal extends AbstractReachGoal {
	
	private static final Log log = LogFactory.getLog(A2CGoal.class);

	private Set<com.sap.psr.vulas.shared.json.model.ConstructId> entryPoints = null;
	
	/**
	 * <p>Constructor for A2CGoal.</p>
	 */
	public A2CGoal() { super(GoalType.A2C); }
	
	/**
	 * <p>Getter for the field <code>entryPoints</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	protected final Set<com.sap.psr.vulas.shared.json.model.ConstructId> getEntryPoints() {
		if(this.entryPoints==null) {
			// Filter app constructs (if requested)
			final String[] filter = this.getConfiguration().getConfiguration().getStringArray(ReachabilityConfiguration.REACH_CONSTR_FILTER);
			if(filter!=null && filter.length>0 && !(filter.length==1 && filter[0].equals(""))) {
				this.entryPoints = ConstructIdUtil.filterWithRegex(this.getAppConstructs(), filter);
			} else {
				this.entryPoints = this.getAppConstructs();
			}
		}
		return this.entryPoints;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * Sets the application constructs as entry points of the {@link ReachabilityAnalyzer}.
	 */
	protected final void setEntryPoints(ReachabilityAnalyzer _ra) {
		_ra.setEntryPoints(this.getEntryPoints(), PathSource.A2C, this.getConfiguration().getConfiguration().getBoolean(ReachabilityConfiguration.REACH_EXIT_UNKOWN_EP, false));
	}
}
