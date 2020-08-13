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
package com.sap.psr.vulas.backend.rest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.persistence.EntityNotFoundException;
import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.AffectedLibrary;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.model.VulnerableDependency;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.backend.repo.AffectedLibraryRepository;
import com.sap.psr.vulas.backend.repo.BugRepository;
import com.sap.psr.vulas.backend.repo.DependencyRepository;
import com.sap.psr.vulas.backend.repo.LibraryIdRepository;
import com.sap.psr.vulas.backend.repo.LibraryRepository;
import com.sap.psr.vulas.backend.util.ArtifactMaps;
import com.sap.psr.vulas.backend.util.CacheFilter;
import com.sap.psr.vulas.backend.util.ServiceWrapper;
import com.sap.psr.vulas.shared.enums.VulnDepOrigin;
import com.sap.psr.vulas.shared.json.model.Version;


/**
 * <p>LibraryIdController class.</p>
 *
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/libids")
public class LibraryIdController {

	private static Logger log = LoggerFactory.getLogger(LibraryIdController.class);

	private final AffectedLibraryRepository afflibRepository;

	private final LibraryIdRepository libIdRepository;
	
	private final BugRepository bugRepository;

	private final Filter cacheFilter;

	@Autowired
	LibraryIdController(AffectedLibraryRepository afflibRepository, LibraryIdRepository libIdRepository, BugRepository bugRepository, Filter cacheFilter) {
		this.afflibRepository = afflibRepository;
		this.libIdRepository = libIdRepository;
		this.bugRepository = bugRepository;
		this.cacheFilter = cacheFilter;
	}

	/**
	 * Returns a list of all {@link Bug}s affecting the given Maven artifact.
	 * This list can be filtered by proving a CVSS threshold (geCvss) or bug identifiers (selecteBugs).
	 *
	 * @param mvnGroup a {@link java.lang.String} object.
	 * @param artifact a {@link java.lang.String} object.
	 * @param version a {@link java.lang.String} object.
	 * @param affected a {@link java.lang.Boolean} object.
	 * @param selectedBugs an array of {@link java.lang.String} objects.
	 * @param geCvss a float.
	 * @return a {@link org.springframework.http.ResponseEntity} object.
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/bugs", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	//@JsonView(Views.LibraryIdDetails.class)
	public ResponseEntity<List<Bug>> getLibIdBugs(
			@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
			@RequestParam(value="affected", required=false, defaultValue="true") Boolean affected,
			@RequestParam(value="selectedBugs", required=false, defaultValue="") String[] selectedBugs,
			@RequestParam(value="geCvss", required=false, defaultValue="0") float geCvss) {
		try {

			// All lib ids, to be filtered according to arguments
			final LibraryId libids = LibraryIdRepository.FILTER.findOne(this.libIdRepository.findBySecondaryKey(mvnGroup, artifact, version));
			
			// Retrieve all bugs from database
			List<Bug> bugsId = null;
			if(selectedBugs==null || selectedBugs.length==0)
				bugsId = this.afflibRepository.findBugByLibraryId(mvnGroup, artifact, version);
			else
				bugsId = this.afflibRepository.findBugByLibraryId(mvnGroup, artifact, version, selectedBugs);
			
			// Filter according to provided arguments
			final List<Bug> result = new ArrayList<Bug>();
			for (Bug s: bugsId) {
				
				// Filter "affected"
				final Boolean is_affected = this.afflibRepository.isBugLibIdAffected(s.getBugId(), libids); 
				if(affected && is_affected!=null && is_affected)
					{}
				else if(!affected && !this.afflibRepository.isBugLibIdAffected(s.getBugId(), libids))
					{}
				else
					continue;
				
				// Filter "geCvss"
				if(geCvss>0F) {
					this.bugRepository.updateCachedCveData(s, false);
					if(s.getCvssScore()==null || s.getCvssScore()<geCvss) // Bugs having cvssScore==null will not be added to the list
						continue;
				}					
				
				// The bug passed all filters, add it to the result set
				result.add(s);
			}
			return new ResponseEntity<List<Bug>>(result, HttpStatus.OK);
			
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<List<Bug>>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Returns all versions of the Maven artifact with the given group and artifact identifiers and packaging "JAR".
	 * The boolean flags secureOnly and vulnerableOnly can be used to filter the result set.
	 * The method can be used, for instance, to inform developers over all none-vulnerable library versions as a replacement for a vulnerable one.
	 *
	 * @param mvnGroup a {@link java.lang.String} object.
	 * @param artifact a {@link java.lang.String} object.
	 * @param latest if true, only the latest version will be returned
	 * @param greaterThanVersion if specified, only versions greater than the provided value are returned
	 * @param secureOnly if true, only those versions w/o known vulnerabilities are returned
	 * @param vulnerableOnly if true, only those versions with known vulnerabilities are returned
	 * @return a {@link org.springframework.http.ResponseEntity} object.
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.LibraryIdDetails.class)
	public ResponseEntity<List<com.sap.psr.vulas.shared.json.model.LibraryId>> getArtifactVersions(@PathVariable String mvnGroup, @PathVariable String artifact,
			@RequestParam(value="latest", required=false, defaultValue="false") Boolean latest,
			@RequestParam(value="greaterThanVersion", required=false, defaultValue="false") String greaterThanVersion,
			@RequestParam(value="secureOnly", required=false, defaultValue="false") Boolean secureOnly,
			@RequestParam(value="vulnerableOnly", required=false, defaultValue="false") Boolean vulnerableOnly) {
		
		// Check consistency of the query
		if(secureOnly && vulnerableOnly) {
			log.error("Bad request, both flags secureOnly and vulnerableOnly are set to true");
			return new ResponseEntity<List<com.sap.psr.vulas.shared.json.model.LibraryId>>(HttpStatus.BAD_REQUEST);
		}

		// Fix params
		if(greaterThanVersion!=null && greaterThanVersion.equalsIgnoreCase("false"))
			greaterThanVersion = null;

		try {		

			// All library IDs known locally w/ affected libraries
			List<LibraryId> known_libids = this.libIdRepository.findLibIds(mvnGroup, artifact);
			
			// Check whether the given group/artifact has synonyms maintained in the configuration
			final ArtifactMaps maps = ArtifactMaps.buildMaps();
			final List<LibraryId> synonym_libids = maps.getGreaterArtifacts(mvnGroup, artifact);
			for(LibraryId syn: synonym_libids) {
				known_libids.addAll(this.libIdRepository.findLibIds(syn.getMvnGroup(), syn.getArtifact()));
			}

			// Only vulnerable lib Ids
			List<com.sap.psr.vulas.shared.json.model.LibraryId> vuln_libids = new ArrayList<com.sap.psr.vulas.shared.json.model.LibraryId>();
			List<com.sap.psr.vulas.shared.json.model.LibraryId> greater_libids = new ArrayList<com.sap.psr.vulas.shared.json.model.LibraryId>();
			for(LibraryId l: known_libids) {
				if(greaterThanVersion!=null && (new Version(l.getVersion()).compareTo(new Version(greaterThanVersion))>0))
					greater_libids.add(l.toSharedType());
				for(AffectedLibrary afflib: l.getAffLibraries()) {
					Boolean affected = this.afflibRepository.isBugLibIdAffected(afflib.getBugId().getBugId(), afflib.getLibraryId());
					if(affected !=null && affected){
						vuln_libids.add(l.toSharedType());
						break;
					}
				}
			}
			
			//all libids rebundled in a libraryId having the given GA
			List<Object[]> libids_w_rebundles = this.libIdRepository.findBundledLibIdByGA(mvnGroup, artifact);
			
			for(Object[] e: libids_w_rebundles) {
				//check whether the libId rebundles a vulnerable library. If so, add it to vuln_libids
				LibraryId lid = LibraryIdRepository.FILTER.findOne(libIdRepository.findById(((BigInteger)e[0]).longValue()));
				
				if(!vuln_libids.contains(lid.toSharedType())){
					LibraryId lid_bundled = LibraryIdRepository.FILTER.findOne(libIdRepository.findById(((BigInteger)e[1]).longValue()));
								
					for(AffectedLibrary afflib: lid_bundled.getAffLibraries()) {
						Boolean affected = this.afflibRepository.isBugLibIdAffected(afflib.getBugId().getBugId(), afflib.getLibraryId());
						if(affected !=null && affected){
							vuln_libids.add(lid.toSharedType());
							break;
						}
					}	
				}
			}

			// Client only wants vulnerable: no need to call external service
			if(vulnerableOnly && greaterThanVersion==null)
				return new ResponseEntity<List<com.sap.psr.vulas.shared.json.model.LibraryId>>(vuln_libids, HttpStatus.OK);
			

			// Else we get all versions from external services 
			final TreeSet<com.sap.psr.vulas.shared.json.model.LibraryId> all_libids = new TreeSet<com.sap.psr.vulas.shared.json.model.LibraryId>(ServiceWrapper.getInstance().getAllArtifactVersions(mvnGroup,  artifact, latest, greaterThanVersion));
			//final Collection<com.sap.psr.vulas.shared.json.model.LibraryId> all_libids = ServiceWrapper.getInstance().getAllArtifactVersions(mvnGroup,  artifact, latest, greaterThanVersion);
			
			//and merge both collections
			if(greaterThanVersion==null && !latest){
				for(LibraryId l : known_libids)
					all_libids.add(l.toSharedType());
			}else 
				all_libids.addAll((Collection<com.sap.psr.vulas.shared.json.model.LibraryId>)greater_libids);
			
			
			final List<com.sap.psr.vulas.shared.json.model.LibraryId> result = new ArrayList<com.sap.psr.vulas.shared.json.model.LibraryId>();
			
			if(all_libids!=null){
				// We perform the same lookup for synonyms (if any)
				for(LibraryId syn: synonym_libids) {
					all_libids.addAll(ServiceWrapper.getInstance().getAllArtifactVersions(syn.getMvnGroup(),  syn.getArtifact(), latest, null));
				}
	
				
				Boolean found = false;
				for(com.sap.psr.vulas.shared.json.model.LibraryId a : all_libids) {
					found = false;
					// If vulnerable should be included, take the one known locally, as it is has vulnerability info
					for(com.sap.psr.vulas.shared.json.model.LibraryId b : vuln_libids){
						if(b.getMvnGroup().equals(a.getMvnGroup()) && b.getArtifact().equals(a.getArtifact())&&b.getVersion().equals(a.getVersion())){
							if(!secureOnly){
								result.add(b);
							}
							found = true;
							break;
						}
					}
					if(!found && !vulnerableOnly)
						result.add(a);
				}
			}
			return new ResponseEntity<List<com.sap.psr.vulas.shared.json.model.LibraryId>>(result, HttpStatus.OK);
		}
		catch(Exception e) {
			log.error("Error: " + e.getMessage(), e);
			return new ResponseEntity<List<com.sap.psr.vulas.shared.json.model.LibraryId>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
