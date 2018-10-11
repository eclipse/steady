package com.sap.psr.vulas.goals;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Space;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Represents a goal executed in the context of a given {@link Space}.
 *
 */
public abstract class AbstractSpaceGoal extends AbstractGoal {

	private static final Log log = LogFactory.getLog(AbstractSpaceGoal.class);

	protected AbstractSpaceGoal(GoalType _type) { super(_type); }
	
	/**
	 * All {@link Space}-related goals need to have a tenant configured.
	 * @throws GoalConfigurationException if no tenant is specified
	 */
	protected void checkPreconditions() throws GoalConfigurationException {
		if(!this.getGoalContext().hasTenant())
			throw new GoalConfigurationException("No tenant configured");
	}
	
	protected void updateFromConfig(Space _s) {
		final Configuration c = this.getConfiguration().getConfiguration();
		_s.setSpaceName(c.getString(CoreConfiguration.SPACE_NAME, null));
		_s.setSpaceDescription(c.getString(CoreConfiguration.SPACE_DESCR, null));
		_s.setExportConfiguration(ExportConfiguration.parse(c.getString(CoreConfiguration.SPACE_EXPCFG, "AGGREGATED")));
		_s.setPublic(c.getBoolean(CoreConfiguration.SPACE_PUBLIC, true));
		_s.setBugFilter(c.getInt(CoreConfiguration.SPACE_BUGFLT, -1));
		_s.setOwnerEmails(new HashSet<String>(Arrays.asList(c.getStringArray(CoreConfiguration.SPACE_OWNERS))));
	}
}