package com.sap.psr.vulas.backend.repo;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.Collection;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.util.ReferenceUpdater;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StopWatch;

public class LibraryRepositoryImpl implements LibraryRepositoryCustom {
	
	private static Logger log = LoggerFactory.getLogger(LibraryRepositoryImpl.class);

	/**
	 * Required by {@link LibraryRepositoryImpl#customSave(Library)}.
	 */
	@Autowired
	LibraryRepository libRepository;

	/**
	 * Required by {@link LibraryRepositoryImpl#customSave(Library)}.
	 */
	@Autowired
	LibraryIdRepository libidRepository;
	
	/**
	 * Required by {@link BugRepositoryImpl#customSave(Bug)}.
	 */
	@Autowired
	ConstructIdRepository cidRepository;
	
	@Autowired
	ReferenceUpdater refUpdater;
	
	public Library customSave(Library _lib) {
		final StopWatch sw = new StopWatch("Save library " + _lib).start();
		final String digest = _lib.getDigest();
	
		// for backward compatibility to vulas-2.x
		if(_lib.getDigestAlgorithm()==null){
			log.warn("Library uploaded with vulas 2.x (digestAlgoritm field missing), defaulting to SHA1 for backward compatibility");
			_lib.setDigestAlgorithm(DigestAlgorithm.SHA1);
		}
		
		// Does it already exist?
		Library managed_lib = null;
		try {
			managed_lib = LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(digest));
			_lib.setId(managed_lib.getId());
			_lib.setCreatedAt(managed_lib.getCreatedAt());
			_lib.setModifiedAt(Calendar.getInstance());
			
			//	Re-create wellknownDigest if it is null in our current db (this part should be removed once it is created for all)
			if(managed_lib.getWellknownDigest()==null || (managed_lib.getDigestTimestamp()==null && managed_lib.getWellknownDigest())) {
				_lib.verifyDigest();
			}
			else {
				_lib.setWellknownDigest(managed_lib.getWellknownDigest());
				_lib.setDigestVerificationUrl(managed_lib.getDigestVerificationUrl());
				_lib.setDigestTimestamp(managed_lib.getDigestTimestamp());
			}
			
			//keep the existing lib GAV if the newly posted/put doesn't have one
			if(_lib.getLibraryId()==null){
				// Recreate libId if null (to be removed once we ensure we added to the existing libs)
				if(managed_lib.getLibraryId()==null)
					_lib.setLibraryId(_lib.getLibIdFromMaven(_lib.getDigest()));
				else
					_lib.setLibraryId(managed_lib.getLibraryId());
			}
					
		} catch(EntityNotFoundException e1) {			
			LibraryRepositoryImpl.log.info("Library [" + _lib.getDigest() + "] does not yet exist, going to save it.");
		}
		
		// Retrieve libId from Maven if digest verified
		if(_lib.getLibraryId()==null)
			_lib.setLibraryId(_lib.getLibIdFromMaven(_lib.getDigest()));
		
		// Update refs to independent entities
		_lib.setConstructs(refUpdater.saveNestedConstructIds(_lib.getConstructs()));
		sw.lap("Updated refs to nested constructs");
		
		_lib.setProperties(refUpdater.saveNestedProperties(_lib.getProperties()));
		sw.lap("Updated refs to nested properties");
		
		Collection<LibraryId> bundledLibraryIds = _lib.getBundledLibraryIds();
		if(bundledLibraryIds!=null && bundledLibraryIds.contains(_lib.getLibraryId())){
			log.debug("Number of bundled libids before removal : " + bundledLibraryIds.size());
			bundledLibraryIds.remove(_lib.getLibraryId());
			log.debug("Number of bundled libids after removal : " + bundledLibraryIds.size());
		}
		_lib.setBundledLibraryIds(refUpdater.saveNestedBundledLibraryIds(bundledLibraryIds));
		
		_lib = this.saveNestedLibraryId(_lib);
		
		// Save
		try {
			managed_lib = this.libRepository.save(_lib);
			sw.stop();
		} catch (Exception e) {
			sw.stop(e);
			LibraryRepositoryImpl.log.error("Error while saving lib [" + digest + "]: " + e.getMessage() +" ----- Going to save JSON and store incomplete library.");
			
			// Save lib to temporay JSON file
			Path temp_file = null;
			try {
				_lib.getLibraryId().setAffLibraries(null);
				temp_file = FileUtil.writeToTmpFile("library", "json", JacksonUtil.asJsonString(_lib));
				log.info("To-be-saved library serialized to temporary JSON file [" + temp_file + "]");
			} catch (Exception ioe) {
				log.error("Error while serializing to-be-saved library to to temporary JSON file: " + ioe.getMessage());
			}
			
			
			
			managed_lib = saveIncomplete(_lib);
		}

		return managed_lib;
	}
	
	private Library saveNestedLibraryId(@NotNull Library _lib) {
		final LibraryId provided_libid = _lib.getLibraryId();
		LibraryId managed_libid = null;
		if(provided_libid!=null) {
			try {
				managed_libid = LibraryIdRepository.FILTER.findOne(this.libidRepository.findBySecondaryKey(provided_libid.getMvnGroup(), provided_libid.getArtifact(), provided_libid.getVersion()));
			} catch (EntityNotFoundException e) {
				managed_libid = this.libidRepository.save(provided_libid);
			}
			_lib.setLibraryId(managed_libid);
		}
		return _lib;
	}
	
	public Library saveIncomplete(Library _lib) throws PersistenceException {
		Library incomplete = new Library(_lib.getDigest());
		if (_lib.getLibraryId()!=null){
			LibraryRepositoryImpl.log.info("Setting library Id ["+_lib.getLibraryId().toString()+"] of incomplete library [" +_lib.getDigest()+ "]");
			incomplete.setLibraryId(_lib.getLibraryId());
		}
		else
			incomplete.setLibraryId(null);
		
		if(_lib.getConstructs()!=null)
			LibraryRepositoryImpl.log.info("Library ["+_lib.getDigest()+"] to be saved has [" +_lib.getConstructs().size()+"] constructs");
		if(_lib.getProperties()!=null)
			LibraryRepositoryImpl.log.info("Library ["+_lib.getDigest()+"] to be saved has [" +_lib.getProperties().size()+"] properties");
		
		try{
			return this.libRepository.save(incomplete);
		} catch (Exception e) {
			throw new PersistenceException("Error while saving incomplete lib [" + incomplete.getDigest() + "]: " + e.getMessage());
		}
	}
}

