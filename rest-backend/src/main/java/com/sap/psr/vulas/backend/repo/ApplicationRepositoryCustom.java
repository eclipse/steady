package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.AffectedLibrary;
import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.ConstructChange;
import com.sap.psr.vulas.backend.model.VulnerableDependency;
import com.sap.psr.vulas.shared.enums.VulnDepOrigin;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/** ApplicationRepositoryCustom interface. */
public interface ApplicationRepositoryCustom {

  /**
   * customSave.
   *
   * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @return a {@link com.sap.psr.vulas.backend.model.Application} object.
   */
  public Application customSave(Application _app);

  /**
   * getVulnerableDependencyBugDetails.
   *
   * @param a a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param digest a {@link java.lang.String} object.
   * @param bugid a {@link java.lang.String} object.
   * @param origin a {@link com.sap.psr.vulas.shared.enums.VulnDepOrigin} object.
   * @param bundledLibrary a {@link java.lang.String} object.
   * @param bundledGroup a {@link java.lang.String} object.
   * @param bundledArtifact a {@link java.lang.String} object.
   * @param bundledVersion a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.model.VulnerableDependency} object.
   */
  public VulnerableDependency getVulnerableDependencyBugDetails(
      Application a,
      String digest,
      String bugid,
      VulnDepOrigin origin,
      String bundledLibrary,
      String bundledGroup,
      String bundledArtifact,
      String bundledVersion);

  /**
   * updateFlags.
   *
   * @param _vdList a {@link java.util.TreeSet} object.
   * @param _withChangeList a {@link java.lang.Boolean} object.
   */
  public void updateFlags(TreeSet<VulnerableDependency> _vdList, Boolean _withChangeList);

  /**
   * updateFlags.
   *
   * @param _vuldep a {@link com.sap.psr.vulas.backend.model.VulnerableDependency} object.
   * @param _withChangeList a {@link java.lang.Boolean} object.
   */
  public void updateFlags(VulnerableDependency _vuldep, Boolean _withChangeList);

  /**
   * getApplications.
   *
   * @param _skip_empty a boolean.
   * @param _space a {@link java.lang.String} object.
   * @param _asOfTimestamp a long.
   * @return a {@link java.util.SortedSet} object.
   */
  public SortedSet<Application> getApplications(
      boolean _skip_empty, String _space, long _asOfTimestamp);

  /**
   * deleteAnalysisResults.
   *
   * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param _clean_goal_history a boolean.
   */
  public void deleteAnalysisResults(Application _app, boolean _clean_goal_history);

  /**
   * findAppVulnerableDependencies.
   *
   * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param _add_excemption_info a boolean.
   * @param _log a boolean.
   * @return a {@link java.util.TreeSet} object.
   */
  public TreeSet<VulnerableDependency> findAppVulnerableDependencies(
      Application _app, boolean _add_excemption_info, boolean _log);

  /**
   * findAffectedApps.
   *
   * @param _bugs an array of {@link java.lang.String} objects.
   * @return a {@link java.util.HashMap} object.
   */
  public HashMap<Long, HashMap<String, Boolean>> findAffectedApps(String[] _bugs);

  /**
   * refreshVulnChangebyChangeList.
   *
   * @param _listOfConstructChanges a {@link java.util.Collection} object.
   */
  public void refreshVulnChangebyChangeList(Collection<ConstructChange> _listOfConstructChanges);

  /**
   * refreshVulnChangebyAffLib.
   *
   * @param _affLib a {@link com.sap.psr.vulas.backend.model.AffectedLibrary} object.
   */
  public void refreshVulnChangebyAffLib(AffectedLibrary _affLib);

  /**
   * refreshLastScanbyApp.
   *
   * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
   */
  public void refreshLastScanbyApp(Application _app);
}
