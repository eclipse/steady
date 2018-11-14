package com.sap.psr.vulas.backend.repo;



import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.ConstructChange;
import com.sap.psr.vulas.backend.model.VulnerableDependency;


public interface ApplicationRepositoryCustom {

	public Application customSave(Application _app);

	public VulnerableDependency getVulnerableDependencyBugDetails(Application a, String digest, String bugid);

	public void updateFlags(TreeSet<VulnerableDependency> _vdList, Boolean _withChangeList);

	public void updateFlags(VulnerableDependency _vuldep, Boolean _withChangeList);

	public SortedSet<Application> getApplications(boolean _skip_empty, String _space);

	public void deleteAnalysisResults(Application _app, boolean _clean_goal_history);

	public TreeSet<VulnerableDependency> findAppVulnerableDependencies(Application _app, boolean _add_excemption_info, boolean _log);
	
	public HashMap<Long, HashMap<String, Boolean>> findAffectedApps(String[] _bugs);
	
	public void refreshVulnChangebyChangeList(Collection<ConstructChange> _listOfConstructChanges);
}
