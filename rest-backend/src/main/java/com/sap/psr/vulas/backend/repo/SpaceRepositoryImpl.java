package com.sap.psr.vulas.backend.repo;



import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.util.ReferenceUpdater;
import com.sap.psr.vulas.shared.util.StopWatch;

/**
 * <p>SpaceRepositoryImpl class.</p>
 *
 */
public class SpaceRepositoryImpl implements SpaceRepositoryCustom {
	
	private static Logger log = LoggerFactory.getLogger(SpaceRepositoryImpl.class);

	@Autowired
	TenantRepository tenantRepository;

	@Autowired
	SpaceRepository spaceRepository;
	
	@Autowired
	ReferenceUpdater refUpdater;
	
	/** {@inheritDoc} */
	public Space customSave(Space _s) {
		final StopWatch sw = new StopWatch("Save " + _s).start();

		// Does it already exist?
		Space managed_space = null;		
		try {
			managed_space = SpaceRepository.FILTER.findOne(this.spaceRepository.findBySecondaryKey(_s.getSpaceToken()));
			_s.setId(managed_space.getId());
			_s.setCreatedAt(managed_space.getCreatedAt());
			//keep the existing values if the newly posted/put doesn't have one
			if(_s.getExportConfiguration()==null)
				_s.setExportConfiguration(managed_space.getExportConfiguration());
			if(_s.getSpaceDescription()==null)
				_s.setSpaceDescription(managed_space.getSpaceDescription());
			if(_s.getSpaceName()==null)
				_s.setSpaceName(managed_space.getSpaceName());
			//if spaceOwners is not sent, delete the previous value
			//	if(_s.getSpaceOwners()==null)
			//	_s.setSpaceOwners(managed_space.getSpaceOwners());
			
		} catch (EntityNotFoundException e1) {			
			SpaceRepositoryImpl.log.info("Space [" + _s.getSpaceToken() + "] does not yet exist, going to save it.");
		}
		
		// Update refs to independent entities
		_s.setProperties(refUpdater.saveNestedProperties(_s.getProperties()));
		sw.lap("Updated refs to nested properties");
		
		// Save
		try {
			managed_space = this.spaceRepository.save(_s);
			sw.stop();
		} catch (Exception e) {
			sw.stop(e);
			SpaceRepositoryImpl.log.error("Error while saving space [" + _s.getSpaceToken()  + "]: " + e.getMessage());
		}

		return managed_space;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * Returns the default space for the given tenant token. In case the tenant token is not provided, it uses the default tenant. If
	 * the default space for the given tenant or the default tenant do not exist, it returns null.
	 */
	public Space getDefaultSpace(String _tenant_token) {
		Space s = null;
		if(_tenant_token==null){
			try{
				_tenant_token = tenantRepository.findDefault().getTenantToken();
			}
			catch(EntityNotFoundException tnfe){
				log.error("A default tenant does not exists, please create one: " + tnfe.getMessage());
			}
		}
		try{
			s = spaceRepository.findDefault(_tenant_token);
		}
		catch (EntityNotFoundException snfe){
			log.error("A default space for tenant token [" + _tenant_token + "] does not exists, please create one: " + snfe.getMessage());
		}
		catch(Exception e){
			log.error("Cannot find default space for tenant token [" + _tenant_token + "]: " + e.getMessage());
		}
		return s;
	}
		
	/**
	 * {@inheritDoc}
	 *
	 * Returns the space for the given space token. In case a space token is not provided, it returns the default space of the
	 * default tenant (if existing, null otherwise)
	 */
	public Space getSpace(String _spaceToken) {
		Space space = null;		
		if(_spaceToken==null)
			space = spaceRepository.getDefaultSpace(null);
		else{
			try {
				space = SpaceRepository.FILTER.findOne(this.spaceRepository.findBySecondaryKey(_spaceToken));
			}
			catch(EntityNotFoundException enfe) {
				log.error("A space with token [" + _spaceToken + "] does not exist: " + enfe.getMessage());
				throw enfe; 
			}
		}
		
		log.info("Found " + space + " for token [" + _spaceToken + "]");
		return space;
	}
}
