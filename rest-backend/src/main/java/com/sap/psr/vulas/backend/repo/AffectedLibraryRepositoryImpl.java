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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sap.psr.vulas.backend.model.AffectedConstructChange;
import com.sap.psr.vulas.backend.model.AffectedLibrary;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructChange;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.model.VulnerableDependency;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import com.sap.psr.vulas.shared.util.StopWatch;

/**
 * <p>AffectedLibraryRepositoryImpl class.</p>
 *
 */
public class AffectedLibraryRepositoryImpl implements AffectedLibraryRepositoryCustom {
	
	private static Logger log = LoggerFactory.getLogger(AffectedLibraryRepositoryImpl.class);

	/**
	 * Required by {@link AffectedLibraryRepositoryImpl#customSave(Bug)}.
	 */
	@Autowired
	BugRepository bugRepository;

	@Autowired
	ConstructChangeRepository ccRepository;


	@Autowired
	ConstructIdRepository cidRepository;

	/**
	 * Required by {@link LibraryRepositoryImpl#customSave(Library)}.
	 */
	@Autowired
	LibraryIdRepository libidRepository;

	@Autowired
	LibraryRepository libRepository;

	/**
	 * Required by {@link LibraryRepositoryImpl#customSave(Library)}.
	 */
	@Autowired
	AffectedLibraryRepository affLibRepository;

	@Autowired
	ApplicationRepository appRepository;
	/**
	 * <p>customSave.</p>
	 *
	 * @return the saved affected libraries
	 * @param _bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 * @param _aff_libs an array of {@link com.sap.psr.vulas.backend.model.AffectedLibrary} objects.
	 * @throws javax.persistence.PersistenceException if any.
	 */
	public List<AffectedLibrary> customSave(Bug _bug, AffectedLibrary[] _aff_libs) throws PersistenceException {
		
		AffectedLibrary managed_aff_lib = null;
		List<AffectedLibrary> libs = new ArrayList<AffectedLibrary>();
		
		for(AffectedLibrary provided_aff_lib: _aff_libs) {
			
			// Does it already exist?
			try {
				provided_aff_lib.setBugId(_bug);
				if(provided_aff_lib.getLibraryId()!=null){
					managed_aff_lib = AffectedLibraryRepository.FILTER.findOne(this.affLibRepository.findByBugAndLibraryIdAndSource(provided_aff_lib.getBugId(), provided_aff_lib.getLibraryId().getMvnGroup(), provided_aff_lib.getLibraryId().getArtifact(), provided_aff_lib.getLibraryId().getVersion(), provided_aff_lib.getSource()));
					
					}
				else if (provided_aff_lib.getLib()!=null){
					managed_aff_lib = AffectedLibraryRepository.FILTER.findOne(this.affLibRepository.findByBugAndLibAndSource(provided_aff_lib.getBugId(), provided_aff_lib.getLib(), provided_aff_lib.getSource()));
					
				}
				// (SP, 10.12.2018: in case the assessment flag is equal to the existing, we skip the saving of the affected app and keep the existing one.
				if(provided_aff_lib.getAffected()!=null && managed_aff_lib.getAffected()!=null && provided_aff_lib.getAffected().equals(managed_aff_lib.getAffected())
						|| (provided_aff_lib.getAffected()==null && managed_aff_lib.getAffected()==null))
					continue;
				else
					provided_aff_lib.setId(managed_aff_lib.getId());
				
				provided_aff_lib.setCreatedAt(managed_aff_lib.getCreatedAt());
			} catch (EntityNotFoundException e1) {
				// Create
				log.info("Creating new affected library  " + provided_aff_lib);
			}
			provided_aff_lib.setModifiedAt(Calendar.getInstance());
			
			// Update refs to independent entities 
			provided_aff_lib = this.saveNestedLibraryId(provided_aff_lib);

			// Update refs to existing lib (lib must have been analyzed before -> should already exist)
			provided_aff_lib = this.updateNestedLib(provided_aff_lib);
			
			//update refs to construct changes
			provided_aff_lib = this.updateConstructChanges(provided_aff_lib,_bug);
			
			log.debug(provided_aff_lib.toString(true));
					
			// Save
			try {
				managed_aff_lib = this.affLibRepository.save(provided_aff_lib);
				libs.add(managed_aff_lib);
				
				//Update vulnChange timestamp for apps with construct changes among its dependencies' constructs
				//this needs to be done after the bug has been created as we need the construct changes to exist in the database to avoid querying by fields (lang, type, qname)
				appRepository.refreshVulnChangebyAffLib(managed_aff_lib);
				
			} catch (Exception e) {
				throw new PersistenceException("Error while saving lib " + provided_aff_lib + ": " + e.getMessage());
			}
		}
		
		return libs;
	}

	private AffectedLibrary saveNestedLibraryId(@NotNull AffectedLibrary _aff_lib) {
		final LibraryId provided_libid = _aff_lib.getLibraryId();
		LibraryId managed_libid = null;
		if(provided_libid!=null) {
			try {
				managed_libid = LibraryIdRepository.FILTER.findOne(this.libidRepository.findBySecondaryKey(provided_libid.getMvnGroup(), provided_libid.getArtifact(), provided_libid.getVersion()));
			} catch (EntityNotFoundException e) {
				managed_libid = this.libidRepository.save(provided_libid);
			}
			_aff_lib.setLibraryId(managed_libid);
		}
		return _aff_lib;
	}
	
	private AffectedLibrary updateNestedLib(@NotNull AffectedLibrary _aff_lib) {
		final Library provided_lib = _aff_lib.getLib();
		Library managed_lib = null;
		if(provided_lib!=null) {
			try {
				managed_lib = LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(provided_lib.getDigest()));

			} catch (EntityNotFoundException e) {
				throw new PersistenceException("Error while saving lib " + provided_lib + ": " + e.getMessage());
			}
			_aff_lib.setLib(managed_lib);
		}
		return _aff_lib;
	}
	
	private AffectedLibrary updateConstructChanges(@NotNull AffectedLibrary _aff_lib,@NotNull Bug _bug) {
		Collection<AffectedConstructChange> aff_cc_list = _aff_lib.getAffectedcc();
		ConstructChange managed_cc = null;
		if(aff_cc_list!=null){
			for(AffectedConstructChange aff_cc: aff_cc_list) {
				//update construct change
				ConstructChange provided_cc = aff_cc.getCc();
				try{
					ConstructId managed_cid = ConstructIdRepository.FILTER.findOne(this.cidRepository.findConstructId(provided_cc.getConstructId().getLang(), provided_cc.getConstructId().getType(), provided_cc.getConstructId().getQname()));
					managed_cc = ConstructChangeRepository.FILTER.findOne(this.ccRepository.findByRepoPathCommitCidBug(provided_cc.getRepo(), provided_cc.getRepoPath(),provided_cc.getCommit(),managed_cid,_bug));
				}catch (EntityNotFoundException e) {
					throw new PersistenceException("Error while saving affected construct change " + provided_cc + ": " + e.getMessage());
				}
				aff_cc.setCc(managed_cc);
				//update libId same byte code
				Collection<LibraryId> provided_libIds = aff_cc.getSameBytecodeLids();
				Collection<LibraryId> managed_libIds = new ArrayList<LibraryId>();
				LibraryId managed_libid = null;
				if(provided_libIds!=null){
					for(LibraryId l:provided_libIds){
						try{
							managed_libid = LibraryIdRepository.FILTER.findOne(this.libidRepository.findBySecondaryKey(l.getMvnGroup(), l.getArtifact(), l.getVersion()));
						} catch (EntityNotFoundException e) {
							managed_libid = this.libidRepository.save(l);
						}
						managed_libIds.add(managed_libid);
	
					}
					aff_cc.setSameBytecodeLids(managed_libIds);
				}
			}
		}
		else
			_aff_lib.setAffectedcc(new ArrayList<AffectedConstructChange>());
		return _aff_lib;
	}
	
	/** {@inheritDoc} */
	public void computeAffectedLib(TreeSet<VulnerableDependency> _vdList){
		for (VulnerableDependency vd : _vdList){
			this.computeAffectedLib(vd,vd.getDep().getLib());
		}
	}
	
	/** {@inheritDoc} */
	public void computeAffectedLib(VulnerableDependency _vd, Library _lib){
		//check existence of AffectedLibraries for the outer library in the case of rebundled libs (required to mark FP entries for the outer library)
		Boolean rebundled = (_vd.getDep().getLib().equals(_lib))?false:true;
		Boolean avForRebundled = null;
		if(rebundled && _vd.getDep().getLib().getLibraryId()!=null){
			avForRebundled = this.affLibRepository.isBugLibIdAffected(_vd.getBug().getBugId(), _vd.getDep().getLib().getLibraryId());
		} 
		// TODO: the code below should be used to check whether an affectedLIbrary for the outer libs exists using the digest (in case the libid is null)
		// it still needs to be tested
//		else if (rebundled){
//			avForRebundled = this.affLibRepository.isBugLibAffected(_vd.getBug().getBugId(), _vd.getDep().getLib().getDigest());
//		}
		
		if(avForRebundled !=null){
			_vd.setAffectedVersion((avForRebundled)?1:0);
			_vd.setAffectedVersionConfirmed(1);
			return;
		}
		
		//check affected for the actual library containing the vulnerable code
		Boolean avByLib = this.affLibRepository.isBugLibAffected(_vd.getBug().getBugId(), _lib.getDigest());
					
		boolean found=false;
		if(avByLib !=null){
			_vd.setAffectedVersion((avByLib)?1:0);
			_vd.setAffectedVersionConfirmed(1);
			found=true;
		}
		else if(_lib.getLibraryId()!=null) {
			AffectedLibraryRepositoryImpl.log.debug("look for affected for bug [" +_vd.getBug().getBugId()+"] and lib [" +_lib.getLibraryId()+ "]");
			Boolean avByLibId = this.affLibRepository.isBugLibIdAffected(_vd.getBug().getBugId(), _lib.getLibraryId());
			if(avByLibId !=null){
				_vd.setAffectedVersion((avByLibId)?1:0);
				_vd.setAffectedVersionConfirmed(1);
				found=true;
			}
		}
		if(!found){
			_vd.setAffectedVersionConfirmed(0);
			_vd.setAffectedVersion(1); // when the confirmed flag is 0, the value of affected-version is irrelevant but we set it to 1 so that the UI doesn't filter it out when filtering out historical vulnerabilities
		}
	}
	
	public List<AffectedLibrary> getAffectedLibraries(Bug _bug, AffectedVersionSource _source, Boolean _onlyWellknown){
		if(_source==null && !_onlyWellknown)
			return this.affLibRepository.findByBug(_bug);
		else if (_source!=null && !_onlyWellknown)
			return this.affLibRepository.findByBugAndSource(_bug, _source);
		else if(_source!=null && _onlyWellknown)
			return this.affLibRepository.findWellKnownByBugAndSource(_bug, _source);
		else 
			return this.affLibRepository.findWellKnownByBug(_bug);
	}
}
