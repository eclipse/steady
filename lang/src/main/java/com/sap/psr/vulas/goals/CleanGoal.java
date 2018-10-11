package com.sap.psr.vulas.goals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Application;

public class CleanGoal extends AbstractAppGoal {

	private static final Log log = LogFactory.getLog(CleanGoal.class);
	
	public CleanGoal() { super(GoalType.CLEAN); }

	@Override
	protected void executeTasks() throws Exception {
		final BackendConnector bc = BackendConnector.getInstance(); 
		final Application app = this.getApplicationContext();

		// Clean
		if(bc.isAppExisting(this.getGoalContext(), app)) {
			bc.cleanApp(this.getGoalContext(), app, this.getConfiguration().getConfiguration().getBoolean(CoreConfiguration.CLEAN_HISTORY, false));

			// Purge versions
			if(this.getConfiguration().getConfiguration().getBoolean(CoreConfiguration.CLEAN_PURGE_VERSIONS, false))
				bc.purgeAppVersions(this.getGoalContext(), app, this.getConfiguration().getConfiguration().getInt(CoreConfiguration.CLEAN_PURGE_KEEP_LAST, 3));
		}
		else {
			log.info("App " + app + " does not exist in backend, thus, cleaning not possible");
			this.skipGoalUpload();
		}
	}
}