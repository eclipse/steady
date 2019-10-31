package com.sap.psr.vulas.backend.util;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.model.Property;
import com.sap.psr.vulas.backend.repo.ConstructIdRepository;
import com.sap.psr.vulas.backend.repo.LibraryIdRepository;
import com.sap.psr.vulas.backend.repo.PropertyRepository;
import java.util.Collection;
import java.util.HashSet;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Replaces unmanaged entities (as received, for instance, through RESTful API calls) by managed
 * ones. Entities are persisted in the database where necessary. The methods of the class are used
 * in various "customSave" methods of different repositories.
 */
public class ReferenceUpdater {

  private static Logger log = LoggerFactory.getLogger(ReferenceUpdater.class);

  @Autowired ConstructIdRepository cidRepository;

  @Autowired PropertyRepository propRepository;

  @Autowired LibraryIdRepository libidRepository;

  /**
   * Checks whether any of the referenced {@link ConstructId}s already exist in the database. If
   * yes, they are read and replaced from the database so that the internal ID is set. Otherwise, if
   * the internal ID of referenced {@link ConstructId}s is null, it would always result in the
   * saving of a new {@link ConstructId}, which in turn results in the violation of unique
   * constraints defined in {@link ConstructId}.
   *
   * @param _constructs a {@link java.util.Collection} object.
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> saveNestedConstructIds(Collection<ConstructId> _constructs) {
    final Collection<ConstructId> constructs = new HashSet<ConstructId>();
    if (_constructs != null) {
      ConstructId managed_cid = null;
      for (ConstructId provided_cid : _constructs) {
        try {
          managed_cid =
              ConstructIdRepository.FILTER.findOne(
                  this.cidRepository.findConstructId(
                      provided_cid.getLang(), provided_cid.getType(), provided_cid.getQname()));
        } catch (EntityNotFoundException e) {
          try {
            managed_cid = this.cidRepository.save(provided_cid);
          } catch (Exception e1) {
            log.error(
                "Error while saving constructId [" + provided_cid + "], will be skipped: " + e1);
            managed_cid = null;
          }
        }
        if (managed_cid != null) constructs.add(managed_cid);
      }
    }
    return constructs;
  }

  /**
   * Same as {@link Application#saveNestedProperties}.
   *
   * @param _props a {@link java.util.Collection} object.
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Property> saveNestedProperties(Collection<Property> _props) {
    final Collection<Property> props = new HashSet<Property>();
    if (_props != null) {
      Property managed_prop = null;
      for (Property provided_prop : _props) {
        try {
          managed_prop =
              PropertyRepository.FILTER.findOne(
                  this.propRepository.findBySecondaryKey(
                      provided_prop.getSource(),
                      provided_prop.getName(),
                      provided_prop.getPropertyValue()));
        } catch (EntityNotFoundException e) {
          try {
            managed_prop = this.propRepository.save(provided_prop);
          } catch (Exception e1) {
            log.error(
                "Error while saving property ["
                    + provided_prop.getName()
                    + "] with value ["
                    + provided_prop.getPropertyValue()
                    + "]");
            managed_prop = null;
          }
        }
        if (managed_prop != null) props.add(managed_prop);
      }
    }
    return props;
  }

  /**
   * saveNestedBundledLibraryIds.
   *
   * @param _bundledLibraryIds a {@link java.util.Collection} object.
   * @return a {@link java.util.Collection} object.
   */
  public Collection<LibraryId> saveNestedBundledLibraryIds(
      Collection<LibraryId> _bundledLibraryIds) {
    final Collection<LibraryId> libraryids = new HashSet<LibraryId>();
    if (_bundledLibraryIds != null) {
      LibraryId managed_lid = null;
      for (LibraryId provided_lid : _bundledLibraryIds) {
        try {
          managed_lid =
              LibraryIdRepository.FILTER.findOne(
                  this.libidRepository.findBySecondaryKey(
                      provided_lid.getMvnGroup(),
                      provided_lid.getArtifact(),
                      provided_lid.getVersion()));
        } catch (EntityNotFoundException e) {
          try {
            managed_lid = this.libidRepository.save(provided_lid);
          } catch (Exception e1) {
            log.error("Error while saving libraryId [" + provided_lid + "]");
            managed_lid = null;
          }
        }
        if (managed_lid != null) {
          libraryids.add(managed_lid);
        }
      }
    }
    return libraryids;
  }
}
