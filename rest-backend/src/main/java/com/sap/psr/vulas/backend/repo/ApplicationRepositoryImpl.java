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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.sap.psr.vulas.backend.model.AffectedConstructChange;
import com.sap.psr.vulas.backend.model.AffectedLibrary;
import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructChange;
import com.sap.psr.vulas.backend.model.ConstructChangeInDependency;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.GoalExecution;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.model.Path;
import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.model.TouchPoint;
import com.sap.psr.vulas.backend.model.Trace;
import com.sap.psr.vulas.backend.model.VulnerableDependency;
import com.sap.psr.vulas.backend.util.ReferenceUpdater;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.PathSource;
import com.sap.psr.vulas.shared.enums.VulnDepOrigin;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.ExemptionSet;
import com.sap.psr.vulas.shared.json.model.IExemption;
import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.shared.util.StringUtil;


/**
 * <p>ApplicationRepositoryImpl class.</p>
 *
 */
public class ApplicationRepositoryImpl implements ApplicationRepositoryCustom {

	private static Logger log = LoggerFactory.getLogger(ApplicationRepositoryImpl.class);

	@Autowired
	ApplicationRepository appRepository;

	@Autowired
	LibraryRepository libRepository;

	@Autowired
	LibraryIdRepository libIdRepository;

	@Autowired
	BugRepository bugRepository;

	@Autowired
	SpaceRepository spaceRepository;

	@Autowired
	TenantRepository tenantRepository;

	@Autowired
	AffectedLibraryRepository affLibRepository;

	@Autowired
	DependencyRepository depRepository;

	@Autowired
	TracesRepository traceRepository;

	@Autowired
	PathRepository pathRepository;

	@Autowired
	GoalExecutionRepository gexeRepository;

	@Autowired
	V_AppVulndepRepository v_appVulnDepRepository;

	@Autowired
	ReferenceUpdater refUpdater;


	/** {@inheritDoc} */
	@Transactional
	public Application customSave(Application _app) {
		final StopWatch sw = new StopWatch("Save app " + _app).start();

		final String group = _app.getMvnGroup();
		final String artifact = _app.getArtifact();
		final String version = _app.getVersion();

		// Does it already exist?
		Application managed_app = null;

		try {
			managed_app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(group, artifact, version, _app.getSpace()));
			managed_app.setModifiedAt(Calendar.getInstance());
			_app = this.updateDependencies(managed_app, _app);
			sw.lap("Updated refs to nested deps of existing application' dependencies");
		} catch (EntityNotFoundException e1) {
			//if the application does not exist, we create an empty one so that we can later add the dependencies incrementally
			Application new_app = new Application(group,artifact,version);
			new_app.setSpace(_app.getSpace());
			managed_app = this.appRepository.save(new_app);		
			sw.lap("Saved new empty application [" + _app +"], next steps will be to save constructs and dependencies");
		}

		_app.setId(managed_app.getId());
		_app.setCreatedAt(managed_app.getCreatedAt());
		_app.setModifiedAt(managed_app.getModifiedAt());	
		_app.setLastScan(managed_app.getLastScan());
		_app.setLastVulnChange(managed_app.getLastVulnChange());

		// Update refs to independent entities
		_app.setConstructs(refUpdater.saveNestedConstructIds(_app.getConstructs()));
		sw.lap("Updated refs to nested constructs");

		_app = this.updateLibraries(_app);
		sw.lap("Updated refs to nested libs");


		_app.orderDependenciesByDepth();
		_app = this.saveDependencyTree(_app);
		sw.lap("Saved and updated refs to dependencies' (including parents)");
		
		// Save (if existing, saves the updated fields, otherwise it should all already be there)
		try {
			managed_app = this.appRepository.save(_app);
			sw.stop();
		} catch (Exception e) {
			sw.stop(e);
			throw new PersistenceException("Error while saving application " + _app + ": " + e.getMessage());
		}

		return managed_app;
	}
	
	/**
	 * <p>Saves the dependency tree ensuring that no transient entities are used for parent dependencies. This is done by setting the parent with 
	 *  its already saved entity. Note that after the save operation the entity is still just handled by Hibernate and may not be persisted in the database yet. 
	 *  Still the handling of Hibernate ensures that once persisted the id will be set correctly linking the parent to its entity).</p>
	 * 
	 * @return a  {@link com.sap.psr.vulas.backend.model.Application} object.
	 */
	private Application saveDependencyTree(@NotNull Application _app) {
		for(Dependency d: _app.getDependencies()) { 
			if(d.getParent()!=null){
				d.setParent(getManagedParent(d.getParent(),_app));
			}
			d = this.depRepository.save(d);
		}
		return _app;
	}	
	
	/**
	 * <p> Given a dependency parent <code>_dep</code>, it retrieves the dependency within the plain list of  all dependencies of the application <code>_app</code>.
	 * 	Because of how dependencies are saved, this operation amounts to retrieve the managed dependency (with id) corresponding to the provided one <code>_dep</code>.</p>
	 * 
	 * @return a  {@link com.sap.psr.vulas.backend.model.Dependency} object.
	 */
	private Dependency getManagedParent(Dependency _dep,Application _app){
		if(_dep.getParent()!=null)
			_dep.setParent(getManagedParent(_dep.getParent(),_app));
		
		for(Dependency d:_app.getDependencies()) {
			if(d.equalLibParentRelPath(_dep))
				return d;
		}
		throw new PersistenceException("Error while saving parent dependency on lib " + _dep.getLib() + "] of application " + _app + ": parent does not exist in application collection");
	}
	
	/**
	 * <p>Updates the provided dependencies of the provided application with managed ones of the managed application if the same dependency exists in both.</p>
	 * 
	 * @return a  {@link com.sap.psr.vulas.backend.model.Application} object.
	 */
	private Application updateDependencies(@NotNull Application _managed_app, @NotNull Application _provided_app) {
		for(Dependency provided_dep: _provided_app.getDependencies()) {
			for(Dependency managed_dep: _managed_app.getDependencies()) {
				//if a dependency with the same lib parent and relPath exists, we update it
				// with the newly provided values of transitive, declared, etc.
				// and we keep the previously computed values for traces, reachable constructs, etc.
				if(provided_dep.equalLibParentRelPath(managed_dep)) {
					provided_dep.setId(managed_dep.getId());
					//as they have the same parent, we have to take also the existing parent to get its db id
					provided_dep.setParent(managed_dep.getParent());
					provided_dep.setTraced(managed_dep.getTraced());
					provided_dep.setReachableConstructIds(managed_dep.getReachableConstructIds());
					provided_dep.setTouchPoints((Set<TouchPoint>)managed_dep.getTouchPoints());
					provided_dep.setTraces(managed_dep.getTraces());
					break;
				}
				// if no dependency  with the same lib parent and relPath exists, we do not look for a library with same lib only but we consider it a new dependency
				// here we check whether it has a parent to see whether it already exists among the managed entities 
				//else if(provided_dep.getParent()!=null){
				//	updateParents(provided_dep,_managed_app);
				//}
			}

		}
		return _provided_app;
	}


	private Application updateLibraries(@NotNull Application _provided_app) throws PersistenceException{

		for(Dependency d: _provided_app.getDependencies()){
			updateLibrary(d,_provided_app);

		}
		return _provided_app;
	}

	private void updateLibrary(Dependency _dep,Application _provided_app){
		if(_dep.getParent()!=null){
			updateLibrary(_dep.getParent(),_provided_app);
		}
		_dep.setLib(findManagedLibrary(_dep,_provided_app));
	}

	private Library findManagedLibrary(Dependency _dep, Application _provided_app){
		Library managed_lib = null;
		try{
			managed_lib = LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(_dep.getLib().getDigest()));
		} catch (EntityNotFoundException e) {
			ApplicationRepositoryImpl.log.error("EntityNotFoundException while finding dependency library ["+ _dep.getLib().getDigest() + "] of application "+ _provided_app + ": " + e.getMessage());
			managed_lib = this.libRepository.saveIncomplete(_dep.getLib());
		} catch (Exception e) {
			throw new PersistenceException("Error while saving dependency on lib " + _dep.getLib() + "] of application " + _provided_app + ": " + e.getMessage());
		}
		return managed_lib;
	}

	/** {@inheritDoc} */
	public void updateFlags(TreeSet<VulnerableDependency> _vd_list, Boolean _withChangeList) {
		for (VulnerableDependency vd : _vd_list){
			this.updateFlags(vd, _withChangeList);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Calls {@link VulnerableDependency#setTraced(int)} and {@link VulnerableDependency#setReachable(int)} depending
	 * on the following conditions: If the {@link Bug} has a change list, it is checked whether vulnerable constructs
	 * were traced during dynamic analysis or found reachable during static analysis. If it does not have a change
	 * list, it is checked whether any of the library's constructs has been traced or found reachable.
	 */
	@Override
	public void updateFlags(VulnerableDependency _vd, Boolean _withChangeList) {
		// 1) Bugs WITH change list
		if(_withChangeList) {
			_vd.setVulnDepOrigin(VulnDepOrigin.CC);

			// 1.1) Dynamic analysis
			final List<Trace> trace_list = traceRepository.findVulnerableTracesOfLibraries(_vd.getDep().getApp(),_vd.getDep().getLib(),_vd.getBugId());
			if(trace_list.size()>0)
				_vd.setTraced(1);
			else 
				_vd.setTraced(0);

			// 1.2) Static analysis
			final List<Path> path_list = pathRepository.findPathsForLibraryBug(_vd.getDep().getApp(), _vd.getDep().getLib(), _vd.getBugId());
			boolean reachable = false;
			for (Path p : path_list) {
				if(p.getSource() != PathSource.X2C) {
					reachable=true;
					break;
				}
			}
			if(reachable)
				_vd.setReachable(1);
			else
				_vd.setReachable(0);			
		}
		// 2) Bugs WITHOUT change list
		else {
			_vd.setVulnDepOrigin(VulnDepOrigin.AFFLIBID);

			// 2.1) Dynamic analysis
			if(_vd.getDep().isTraced())
				_vd.setTraced(1);
			else 
				_vd.setTraced(0);

			// 2.2) Static analysis
			final Collection<ConstructId> reachable_constructs = _vd.getDep().getReachableConstructIds();
			if(reachable_constructs!=null && reachable_constructs.size()>0)
				_vd.setReachable(1);
			else 
				_vd.setReachable(0);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Returns a {@link SortedSet} of all {@link Application}s of the given {@link Space}.
	 */
	@Override
	public SortedSet<Application> getApplications(boolean _skip_empty, String _space, long _asOfTimestamp) {
		final StopWatch w = new StopWatch("Read all apps of space [" + _space + "]").start();
		final SortedSet<Application> sorted_apps = new TreeSet<Application>();
		List<Application> result = null;
		if(_skip_empty)
			result = this.appRepository.findNonEmptyApps(_space, _asOfTimestamp);
		else 
			result = this.appRepository.findAllApps(_space, _asOfTimestamp);
		w.stop();
		sorted_apps.addAll(result);
		return sorted_apps;
	}

	/** {@inheritDoc} */
	@Override
	public VulnerableDependency getVulnerableDependencyBugDetails(Application a, String digest, String bugid, VulnDepOrigin origin, String bundledDigest, String bundledGroup, String bundledArtifact, String bundledVersion) throws EntityNotFoundException {

		// Bug
		final Bug bug = BugRepository.FILTER.findOne(bugRepository.findByBugId(bugid));
		this.bugRepository.updateCachedCveData(bug, false);

		// Create the vuln dependency (flags to be set afterwards)
		VulnerableDependency vd = new VulnerableDependency(DependencyRepository.FILTER.findOne(depRepository.findByAppAndLib(a, digest)), bug);

		// Required for setting the flags for all elements of the change list (if any)
		List<Trace> trace_list = new ArrayList<Trace>();
		List<Path> path_list = new ArrayList<Path>();
		List<ConstructId> cidList = new ArrayList<ConstructId>();
		List<AffectedConstructChange> aff_ccList = new ArrayList<AffectedConstructChange>();

		//set affected flag and trace.reachable (if applicable)
		if(origin.equals(VulnDepOrigin.CC) || origin.equals(VulnDepOrigin.AFFLIBID)){
			this.affLibRepository.computeAffectedLib(vd,vd.getDep().getLib());
			if(vd.getBug().countConstructChanges()>0)
				this.updateFlags(vd,true);
			else
				this.updateFlags(vd,false);

			// Required for setting the flags for all elements of the change list (if any)
			trace_list = traceRepository.findVulnerableTracesOfLibraries(vd.getDep().getApp(),vd.getDep().getLib(), vd.getBugId());
			path_list = pathRepository.findPathsForLibraryBug(vd.getDep().getApp(), vd.getDep().getLib(), vd.getBugId());
			cidList = libRepository.findBuggyConstructIds(vd.getDep().getLib().getDigest(),vd.getBug().getBugId() );
			aff_ccList = affLibRepository.findByBugAndLib(vd.getBug(),vd.getDep().getLib());
		}
		else if (origin.equals(VulnDepOrigin.BUNDLEDCC) ){
			Library library = LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(bundledDigest));
			this.affLibRepository.computeAffectedLib(vd,library);
			vd.setVulnDepOrigin(VulnDepOrigin.BUNDLEDCC);
			vd.setBundledLib(library);
			cidList = libRepository.findBuggyConstructIds(library.getDigest(),vd.getBug().getBugId() );
			aff_ccList = affLibRepository.findByBugAndLib(vd.getBug(),library);
		} else if (origin.equals(VulnDepOrigin.BUNDLEDAFFLIBID) ){
			LibraryId libraryId = LibraryIdRepository.FILTER.findOne(this.libIdRepository.findBySecondaryKey(bundledGroup, bundledArtifact, bundledVersion));  
			Boolean affected = this.affLibRepository.isBugLibIdAffected(vd.getBug().getBugId(), libraryId);
			vd.setAffectedVersion(affected?1:0);
			vd.setAffectedVersionConfirmed(1);
			vd.setVulnDepOrigin(VulnDepOrigin.BUNDLEDAFFLIBID);
			vd.setBundledLibId(libraryId);
		}

		List<ConstructChangeInDependency> constructsList = new ArrayList<ConstructChangeInDependency>();
		for(ConstructChange c : vd.getBug().getConstructChanges()) {
			if(c.getConstructId().getType()== ConstructType.CONS || c.getConstructId().getType()== ConstructType.METH || c.getConstructId().getType()== ConstructType.INIT){

				ConstructChangeInDependency ccd = new ConstructChangeInDependency(c);
				Boolean found = false;
				for(Trace t : trace_list){
					if(t.getConstructId().equals(c.getConstructId())){
						found = true;
						ccd.setTraced(true);
						ccd.setTrace(t);
						break;
					}
				}
				if(!found && vd.getTracedConfirmed()==1)
					ccd.setTraced(false);

				found=false;
				//TODO also store the paths?
				for(Path p: path_list){
					if(p.getEndConstructId().equals(c.getConstructId())){
						//if(p.getEndConstructId().equals(c.getConstructId())||p.getStartConstructId().equals(c.getConstructId()))
						if(p.getSource()!= PathSource.X2C){
							found=true;	
							ccd.setReachable(true);
							break;
						}
					}
				}
				if(!found && vd.getReachableConfirmed()==1)
					ccd.setReachable(false);

				if(cidList.contains(c.getConstructId())) {
					ccd.setInArchive(true);
				}
				else
					ccd.setInArchive(false);



				for(AffectedConstructChange aff_cc : aff_ccList){
					if(aff_cc.getCc().equals(c)){
						ccd.setAffected(aff_cc.getAffected());
						ccd.setClassInArchive(aff_cc.getClassInArchive());
						ccd.setEqualChangeType(aff_cc.getEqualChangeType());
						ccd.setOverall_change(aff_cc.getOverall_chg());
						if(ccd.getInArchive()!=aff_cc.getInArchive()){
							log.warn("Conclusion of construct In archive from backend is " + ccd.getInArchive() + " while vulas:check-version concluded " + aff_cc.getInArchive());
						}
					}
				}

				constructsList.add(ccd);
			}
			else { //if (c.getConstructId().getType()== ConstructType.CLAS){
				ConstructChangeInDependency ccd = new ConstructChangeInDependency(c);
				if(cidList.contains(c.getConstructId())){
					ccd.setInArchive(true);
				}
				else
					ccd.setInArchive(false);
				constructsList.add(ccd);
			}
		}
		vd.setConstructList(constructsList);

		return vd;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Deletes all the traces and paths collected during the various analyses.
	 * If requested, also deletes the goal history.
	 */
	@Override
	public void deleteAnalysisResults(Application _app, boolean _clean_goal_history) {
		// Delete traces
		this.traceRepository.deleteAllTracesForApp(_app);

		// Delete all paths
		List<Path> paths = this.pathRepository.findByApp(_app);
		for(Path p: paths) {
			p.setPath(null);
			this.pathRepository.save(p);
		}
		this.pathRepository.deleteAllPathsForApp(_app);

		// Delete goal execution history
		if(_clean_goal_history) {
			for(GoalExecution gexe: this.gexeRepository.findByApp(_app))
				this.gexeRepository.delete(gexe);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Finds all @{VulnerableDependency}s for a given {@link Space} and {@link Application}.
	 */
	@Override
	public TreeSet<VulnerableDependency> findAppVulnerableDependencies(Application _app, boolean _add_exemption_info, boolean _log) {
		final StopWatch sw = new StopWatch("Query vulnerable dependencies for application " + _app);
		if(_log)
			sw.start();

		// 1) Join over construct changes (origin=cc)
		final TreeSet<VulnerableDependency> vd_list_cc = this.appRepository.findJPQLVulnerableDependenciesByGAV(_app.getMvnGroup(), _app.getArtifact(), _app.getVersion(), _app.getSpace());
		if(_log)
			sw.lap("Found [" + vd_list_cc.size() + "] through joining constructs");

		//to improve performances we use a native query to get the affected version (having moved to SQL the computation of the affected version source having priority.
		//further improvements could be:
		// embedding the native query into the JPQL one to only get the bugs according to the requested flags, e.g., only affected or only historical ones.
		this.affLibRepository.computeAffectedLib(vd_list_cc);
		this.updateFlags(vd_list_cc, true);		

		// 2) Retrieve vuln w/ cc for bundled libids
		TreeSet<VulnerableDependency> vd_list_bundled_cc = new TreeSet<VulnerableDependency>();

		List<Object[]> bundledDigests = this.libRepository.findBundledLibByApp(_app);
		
		if(_log)
			sw.lap("Found ["+bundledDigests.size()+"] libs digest for bundled libids.");

		for (Object[] e: bundledDigests){
			Dependency depWithBundledLibId = DependencyRepository.FILTER.findOne(this.depRepository.findById(((BigInteger)e[0]).longValue()));

			Library bundledDigest = LibraryRepository.FILTER.findOne(this.libRepository.findById(((BigInteger)e[1]).longValue())); 
			List<Bug> vulns_cc = this.bugRepository.findByLibrary(bundledDigest);

			for(Bug b: vulns_cc){
				VulnerableDependency vulndep = new VulnerableDependency(depWithBundledLibId, b);
				vulndep.setVulnDepOrigin(VulnDepOrigin.BUNDLEDCC);
				this.affLibRepository.computeAffectedLib(vulndep,bundledDigest);
				vulndep.setBundledLibId(bundledDigest.getLibraryId());
				vulndep.setBundledLib(bundledDigest);
				vd_list_bundled_cc.add(vulndep);
			}
			//for bundled libraries we do have have traced and reachable information so we skip the query
			//	this.updateFlags(vd_list_bundled_cc, true);

		}

		// 3) Retrieve vuln w/o cc for bundled libids
		TreeSet<VulnerableDependency> vd_list_bundled_av = new TreeSet<VulnerableDependency>();

		List<Object[]> bundledLibIds = this.libIdRepository.findBundledLibIdByApp(_app);

		for(Object[] e: bundledLibIds){

			Dependency depWithBundledLibId = DependencyRepository.FILTER.findOne(this.depRepository.findById(((BigInteger)e[0]).longValue()));

			LibraryId bundledLibId = LibraryIdRepository.FILTER.findOne(this.libIdRepository.findById(((BigInteger)e[1]).longValue())); 

			List<Bug> vulns_av_true = this.bugRepository.findByLibId(bundledLibId,true);

			for (Bug b: vulns_av_true){
				VulnerableDependency vulndep = new VulnerableDependency(depWithBundledLibId, b);
				vulndep.setVulnDepOrigin(VulnDepOrigin.BUNDLEDAFFLIBID);
				Boolean rebundlingAffected = this.affLibRepository.isBugLibIdAffected(b.getBugId(), depWithBundledLibId.getLib().getLibraryId());
				if(rebundlingAffected != null && !rebundlingAffected)
					vulndep.setAffectedVersion(0);
				else						
					vulndep.setAffectedVersion(1);
				vulndep.setAffectedVersionConfirmed(1);
				vulndep.setBundledLibId(bundledLibId);
				vd_list_bundled_av.add(vulndep);
			}
			List<Bug> vulns_av_false = this.bugRepository.findByLibId(bundledLibId,false);

			for (Bug b: vulns_av_false){
				VulnerableDependency vulndep = new VulnerableDependency(depWithBundledLibId, b);
				vulndep.setVulnDepOrigin(VulnDepOrigin.BUNDLEDAFFLIBID);
				vulndep.setAffectedVersion(0);
				vulndep.setAffectedVersionConfirmed(1);
				vulndep.setBundledLibId(bundledLibId);
				vd_list_bundled_av.add(vulndep);
			}
			//for bundled libraries we do have have traced and reachable information so we skip the query
			//	this.updateFlags(vd_list_bundled_cc, true);
		}

		if(_log){
			sw.lap("Found [" + vd_list_bundled_cc.size() + "] vulns w/  cc through bundled library ids");
			sw.lap("Found [" + vd_list_bundled_av.size() + "] vulns w/o cc through bundled library ids");
		}

		// 4) Join over libids
		final TreeSet<VulnerableDependency> vd_list_libid = this.appRepository.findJPQLVulnerableDependenciesByGAVAndAffVersion(_app.getMvnGroup(), _app.getArtifact(), _app.getVersion(), _app.getSpace(), true);
		for(VulnerableDependency vd : vd_list_libid){
			vd.setAffectedVersion(1);
			vd.setAffectedVersionConfirmed(1);
		}
		final TreeSet<VulnerableDependency> vd_list_libid_false = this.appRepository.findJPQLVulnerableDependenciesByGAVAndAffVersion(_app.getMvnGroup(), _app.getArtifact(), _app.getVersion(), _app.getSpace(), false);
		for(VulnerableDependency vd : vd_list_libid_false){
			vd.setAffectedVersion(0);
			vd.setAffectedVersionConfirmed(1);
		}
		vd_list_libid.addAll(vd_list_libid_false);
		if(_log)
			sw.lap("Found [" + vd_list_libid.size() + "] through joining libids");
		//this.affLibRepository.computeAffectedLib(vd_list_libid); this is not required for vulns w/o cc as we select by affectedVersions.affected (in this way we save the additional queries to get the affected flag afterwards)
		this.updateFlags(vd_list_libid, false);

		// Merge bugs found joining constructs and libids 
		final TreeSet<VulnerableDependency> vd_all = new TreeSet<VulnerableDependency>();	
		vd_all.addAll(vd_list_cc); //this must be done before the add for bundled vulndeps to ensure that we do not consider as bundledcc the cases where the code signature was not altered.
		vd_all.addAll(vd_list_bundled_cc);
		vd_all.addAll(vd_list_bundled_av);
		vd_all.addAll(vd_list_libid); // this must be added after the vd_list_bundled_av, to ensure that we get the info that a pair dep,bug comes out of something rebundled, though we overwrite it as FP at the level of the rebundling artifact

		// Read exemption info from configuration and enrich vuln dep
		if(_add_exemption_info) {
			final GoalExecution latest = this.gexeRepository.findLatestGoalExecution(_app, null);
			if(latest!=null) {
				final ExemptionSet exempts = ExemptionSet.createFromMap(latest.getConfigurationMap());
				for(VulnerableDependency vd: vd_all) {
					com.sap.psr.vulas.shared.json.model.VulnerableDependency svd = (com.sap.psr.vulas.shared.json.model.VulnerableDependency)JacksonUtil.fromTo(vd,  null,  com.sap.psr.vulas.backend.model.view.Views.class,  com.sap.psr.vulas.shared.json.model.VulnerableDependency.class);
					final IExemption exempt = exempts.getApplicableExemption(svd);
					if(exempt!=null) {
						vd.setExemption(exempt);
					}
				}
			}
		}

		if(_log)
			sw.stop();

		return vd_all;
	}

	// Following stmts were meant to read configuration settings from database

	// Fast select
	/*final List<Property> properties = this.gexeRepository.findExecutionConfiguration(_app, PropertySource.GOAL_CONFIG, EXCLUDED_SCOPES);
		if(properties!=null && properties.size()==1) {
			final String[] values = properties.get(0).getPropertyValueAsArray();
			excluded_scopes.addAll(Scope.fromStringArray(values));
		}*/

	// Fast select
	/*final List<Property> properties = this.gexeRepository.findExecutionConfiguration(_app, PropertySource.GOAL_CONFIG, EXCLUDED_BUGS);
		if(properties!=null && properties.size()==1) {
			final String[] values = properties.get(0).getPropertyValueAsArray();
			for(String v: values) {
				final String setting = EXCLUDED_BUGS + "." + v;
				final List<Property> texts = this.gexeRepository.findExecutionConfiguration(_app, PropertySource.GOAL_CONFIG, setting);
				if(texts!=null && texts.size()==1)
					excluded_bugs.put(v.toLowerCase(), texts.get(0).getPropertyValue());
				else
					excluded_bugs.put(v.toLowerCase(), "No reason provided, please add using configuration parameter [" + setting + "]");
			}
		}*/

	/**
	 * <p>findAffectedApps.</p>
	 *
	 * @param _bugs an array of {@link java.lang.String} objects.
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<Long, HashMap<String, Boolean>> findAffectedApps(String[] _bugs) {
		final HashMap<Long, HashMap<String, Boolean>> affected_apps = new HashMap<Long, HashMap<String, Boolean>>();
		final List<Object[]> affected_apps_raw = this.v_appVulnDepRepository.findAffectedApps(_bugs);
		if(affected_apps_raw!=null && affected_apps_raw.size()>0) {
			for(Object[] o: affected_apps_raw) {
				try {
					final Long app_id = ((BigInteger)o[0]).longValue();
					final String bugid = (String)o[1];
					final Boolean affected = (Boolean)o[2];

					HashMap<String, Boolean> affected_map = null;
					if(affected_apps.containsKey(app_id)) {
						affected_map = affected_apps.get(app_id);
					} else {
						affected_map = new HashMap<String, Boolean>();
					}
					affected_map.put(bugid,  affected);
					affected_apps.put(app_id,  affected_map);					
				} catch (ClassCastException e) {
					log.error("Cannot cast [" + StringUtil.join(o, ", ") + "] of types [" + o[0].getClass().getSimpleName() + ", " + o[1].getClass().getSimpleName() + ", " + o[2].getClass().getSimpleName() + "] to [BigInt, String, Boolean]");
				} catch (ArrayIndexOutOfBoundsException e) {
					log.error("Cannot cast, too few elements in object array, [" + o.length + "] instead of [3]");
				}
			}
		}
		return affected_apps;		
	}

	/** {@inheritDoc} */
	@Transactional
	public void refreshVulnChangebyChangeList(Collection<ConstructChange> _listOfConstructChanges){
		final StopWatch sw = new StopWatch("Started refresh app vulnChange by CC list").start();
		List<ConstructId> listOfConstructs = new ArrayList<ConstructId>();
		for(ConstructChange cc : _listOfConstructChanges){
			listOfConstructs.add(cc.getConstructId());
		}
		List<Application> apps = appRepository.findAppsByCC(listOfConstructs); 
		sw.lap("LastVulnChange by CC, [" +apps.size()+"] apps to be refreshed");

		//we partition the list to work around the PostgreSQL's limit of 32767 bind variables per statement
		for(List<Application> sub: Lists.partition(apps, 30000))
			appRepository.updateAppLastVulnChange(sub);
		sw.stop();
	}

	/** {@inheritDoc} */
	@Transactional
	public void refreshVulnChangebyAffLib(AffectedLibrary _affLib){
		final StopWatch sw = new StopWatch("Started refresh app vulnChange by addLib").start();
		List<Application> apps = new ArrayList<Application>();
		if(_affLib!=null && _affLib.getLibraryId()!=null){
			if(_affLib.getAffected()!=null)
				apps.addAll(appRepository.findAppsByAffLib(_affLib.getLibraryId()));
		}
		sw.lap("LastVulnChange by AffLib, [" +apps.size()+"] apps to be refreshed");
		//we partition the list to work around the PostgreSQL's limit of 32767 bind variables per statement
		for(List<Application> sub: Lists.partition(apps, 30000))
			appRepository.updateAppLastVulnChange(sub);
		sw.stop();
	}


	/** {@inheritDoc} */
	public void refreshLastScanbyApp(Application _app){
		_app.setLastScan(Calendar.getInstance());
		appRepository.save(_app);
	}
}
