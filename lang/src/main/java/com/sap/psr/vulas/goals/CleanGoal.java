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
	/*
	 * This method cleans the backend and also eventually purges the older versions of the application.
	 * If application version (GAV) exists, it eventually purges the keepLast versions, otherwise it eventually purges the (keepLast-1) versions 
	 * @see com.sap.psr.vulas.goals.AbstractGoal#executeTasks()
	 */
	protected void executeTasks() throws Exception {
		final BackendConnector bc = BackendConnector.getInstance(); 
		final Application app = this.getApplicationContext();

		int keepLast = this.getConfiguration().getConfiguration().getInt(CoreConfiguration.CLEAN_PURGE_KEEP_LAST, 3);
		
		// Clean
		if(bc.isAppExisting(this.getGoalContext(), app)) {
			bc.cleanApp(this.getGoalContext(), app, this.getConfiguration().getConfiguration().getBoolean(CoreConfiguration.CLEAN_HISTORY, false));
		}else {
			log.info("App [" + app + "] does not exist in backend, thus, cleaning not possible");
			this.skipGoalUpload();
			//in case the GAV does not exist, then reducing by 1 the number of versions to be kept
			if (keepLast > 0) {
				--keepLast;
			}
		}
		
		// Purge versions
		if(this.getConfiguration().getConfiguration().getBoolean(CoreConfiguration.CLEAN_PURGE_VERSIONS, false)) {
			bc.purgeAppVersions(this.getGoalContext(), app, keepLast);
		}
	}
}
