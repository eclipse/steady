package com.sap.psr.vulas.backend.util;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Property;
import com.sap.psr.vulas.backend.repo.ConstructIdRepository;
import com.sap.psr.vulas.backend.repo.PropertyRepository;

/**
 * Replaces unmanaged entities (as received, for instance, through RESTful API calls) by managed ones. Entities are
 * persisted in the database where necessary. The methods of the class are used in various "customSave" methods of different repositories.
 *
 *
 */
public class ReferenceUpdater {
	
	private static Logger log = LoggerFactory.getLogger(ReferenceUpdater.class);

	@Autowired
	ConstructIdRepository cidRepository;

	@Autowired
	PropertyRepository propRepository;

	/**
	 * Checks whether any of the referenced {@link ConstructId}s already exist in the database.
	 * If yes, they are read and replaced from the database so that the internal ID is set.
	 * Otherwise, if the internal ID of referenced {@link ConstructId}s is null, it would always
	 * result in the saving of a new {@link ConstructId}, which in turn results in the violation of
	 * unique constraints defined in {@link ConstructId}.
	 * @param _lib
	 */
	public Collection<ConstructId> saveNestedConstructIds(Collection<ConstructId> _constructs) {
		final Collection<ConstructId> constructs = new HashSet<ConstructId>();
		if(_constructs!=null) {
			ConstructId managed_cid = null;
			for(ConstructId provided_cid: _constructs) {
				try {
					managed_cid  = ConstructIdRepository.FILTER.findOne(this.cidRepository.findConstructId(provided_cid.getLang(), provided_cid.getType(), provided_cid.getQname()));
				} catch (EntityNotFoundException e) {
					try {
						managed_cid = this.cidRepository.save(provided_cid);
					} catch (Exception e1) {
						log.error("Error while saving constructId [" + provided_cid + "], will be skipped: " + e1);
						managed_cid = null;
					}
				}
				if(managed_cid!=null)
					constructs.add(managed_cid);
			}
		}
		return constructs;
	}

	/**
	 * Same as {@link Application#saveNestedProperties}.
	 * @param _props
	 * @return
	 */
	public Collection<Property> saveNestedProperties(Collection<Property> _props) {
		final Collection<Property> props = new HashSet<Property>();
		if(_props!=null) {
			Property managed_prop = null;
			for(Property provided_prop: _props) {
				try {
					managed_prop = PropertyRepository.FILTER.findOne(this.propRepository.findBySecondaryKey(provided_prop.getSource(), provided_prop.getName(), provided_prop.getPropertyValue()));
				} catch (EntityNotFoundException e) {
					try {
						managed_prop = this.propRepository.save(provided_prop);
					} catch (Exception e1) {
						log.error("Error while saving property [" + provided_prop.getName() + "] with value [" + provided_prop.getPropertyValue() + "]");
						managed_prop = null;
					}
					
				}
				if(managed_prop!=null)
					props.add(managed_prop);
			}
		}
		return props;
	}
}
