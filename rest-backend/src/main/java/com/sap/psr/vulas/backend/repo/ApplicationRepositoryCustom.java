/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.backend.repo;



import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.psr.vulas.backend.model.AffectedLibrary;
import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.ConstructChange;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.model.VulnerableDependency;
import com.sap.psr.vulas.shared.enums.VulnDepOrigin;


/**
 * <p>ApplicationRepositoryCustom interface.</p>
 *
 */
public interface ApplicationRepositoryCustom {

	/**
	 * <p>customSave.</p>
	 *
	 * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 * @return a {@link com.sap.psr.vulas.backend.model.Application} object.
	 */
	public Application customSave(Application _app);

	/**
	 * <p>getVulnerableDependencyBugDetails.</p>
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
	public VulnerableDependency getVulnerableDependencyBugDetails(Application a, String digest, String bugid, VulnDepOrigin origin, String bundledLibrary, String bundledGroup, String bundledArtifact, String bundledVersion);

	/**
	 * <p>updateFlags.</p>
	 *
	 * @param _vdList a {@link java.util.TreeSet} object.
	 * @param _withChangeList a {@link java.lang.Boolean} object.
	 */
	public void updateFlags(TreeSet<VulnerableDependency> _vdList, Boolean _withChangeList);

	/**
	 * <p>updateFlags.</p>
	 *
	 * @param _vuldep a {@link com.sap.psr.vulas.backend.model.VulnerableDependency} object.
	 * @param _withChangeList a {@link java.lang.Boolean} object.
	 */
	public void updateFlags(VulnerableDependency _vuldep, Boolean _withChangeList);

	/**
	 * <p>getApplications.</p>
	 *
	 * @param _skip_empty a boolean.
	 * @param _space a {@link java.lang.String} object.
	 * @param _asOfTimestamp a long.
	 * @return a {@link java.util.SortedSet} object.
	 */
	public SortedSet<Application> getApplications(boolean _skip_empty, String _space, long _asOfTimestamp);

	/**
	 * <p>deleteAnalysisResults.</p>
	 *
	 * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 * @param _clean_goal_history a boolean.
	 */
	public void deleteAnalysisResults(Application _app, boolean _clean_goal_history);

	/**
	 * <p>findAppVulnerableDependencies.</p>
	 *
	 * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 * @param _add_excemption_info a boolean.
	 * @param _log a boolean.
	 * @return a {@link java.util.TreeSet} object.
	 */
	public TreeSet<VulnerableDependency> findAppVulnerableDependencies(Application _app, boolean _add_excemption_info, boolean _log);
	
	/**
	 * <p>findAffectedApps.</p>
	 *
	 * @param _bugs an array of {@link java.lang.String} objects.
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<Long, HashMap<String, Boolean>> findAffectedApps(String[] _bugs);
	
	/**
	 * <p>refreshVulnChangebyChangeList.</p>
	 *
	 * @param _listOfConstructChanges a {@link java.util.Collection} object.
	 */
	public void refreshVulnChangebyChangeList(Collection<ConstructChange> _listOfConstructChanges);
	
	/**
	 * <p>refreshVulnChangebyAffLib.</p>
	 *
	 * @param _affLib a {@link com.sap.psr.vulas.backend.model.AffectedLibrary} object.
	 */
	public void refreshVulnChangebyAffLib(AffectedLibrary _affLib);
	
	/**
	 * <p>refreshLastScanbyApp.</p>
	 *
	 * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 */
	public void refreshLastScanbyApp(Application _app);
}
