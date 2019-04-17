package com.sap.psr.vulas.patcheval2;



import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.java.sign.gson.GsonHelper;
import com.sap.psr.vulas.patcheval.representation.Bug;
import com.sap.psr.vulas.patcheval.utils.PEConfiguration;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.VulnerableDependency;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class PE_Run implements Runnable {

	private static final Log log = LogFactory.getLog(PE_Run.class);

	public void run() {

		String[] bugs = VulasConfiguration.getGlobal().getConfiguration().getStringArray(PEConfiguration.BUGID);
		ProgrammingLanguage lang = null;
		try{
			if(VulasConfiguration.getGlobal().getConfiguration().getString(PEConfiguration.LANG)!=null)
				lang = ProgrammingLanguage.valueOf(VulasConfiguration.getGlobal().getConfiguration().getString(PEConfiguration.LANG));
		} catch(IllegalArgumentException e){
			log.error("The specified language value "+VulasConfiguration.getGlobal().getConfiguration().getString(PEConfiguration.LANG)+" is not allowed. Allowed values: PY, JAVA.");
			return;
		}
		

		final Gson gson = GsonHelper.getCustomGsonBuilder().create();

		List<Bug> bugsToAnalyze = new ArrayList<Bug>();

		if (bugs == null || bugs.length == 0 || (bugs.length == 1 && bugs[0].equals("")) ) {
			String allbugs;
			try {
				allbugs = BackendConnector.getInstance().getBugsList(lang);
				bugsToAnalyze = Arrays.asList(gson.fromJson(allbugs, Bug[].class));
			} catch (BackendConnectionException e) {
				if(e.getHttpResponseStatus()==503)
					log.error("Vulas backend still unavailable (503) after 1h, could not get list of bugs to analyze");
			}
		} else {
			for (String bugId : bugs) {
				try {
					if (bugId.equals("U")) {
						List<String> contained = new ArrayList<String>();
						VulnerableDependency[] unconfirmedBugs = BackendConnector.getInstance()
								.getVulnDeps(Boolean.valueOf(true));
						for (VulnerableDependency vd : unconfirmedBugs) {
							if (vd.getDep().getLib().getLibraryId() != null) {
								if (!contained.contains(vd.getBug().getBugId())) {
									bugsToAnalyze.add(new Bug(vd.getBug().getBugId(), null));
									contained.add(vd.getBug().getBugId());
								}
							}
						}
						// bugsToAnalyze =
						// Arrays.asList((Bug[])bugSet.toArray());

					} else if (!bugId.equals("")) {
						bugsToAnalyze.add(new Bug(bugId, null));
					}
				} catch (BackendConnectionException e) {
					if(e.getHttpResponseStatus()==503)
						log.error("Vulas backend still unavailable (503) after 1h, could not get bug  [" + bugId + "] to analyze");
					PE_Run.log.error("Error when adding [" + bugId + "] to list of bug to analyze: " + e.getMessage());
				}
			}
		}

		try {
			BugLibManager.analyze(bugsToAnalyze);
		} catch (BackendConnectionException e) {
			if(e.getHttpResponseStatus()==503)
				log.error("Service still unavailable (503) after 1h, could not analyze bugs");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
