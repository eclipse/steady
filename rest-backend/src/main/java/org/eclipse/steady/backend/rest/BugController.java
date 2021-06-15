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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.backend.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import com.fasterxml.jackson.annotation.JsonView;

import org.eclipse.steady.backend.model.AffectedLibrary;
import org.eclipse.steady.backend.model.Bug;
import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.view.Views;
import org.eclipse.steady.backend.repo.AffectedLibraryRepository;
import org.eclipse.steady.backend.repo.BugRepository;
import org.eclipse.steady.backend.repo.LibraryRepository;
import org.eclipse.steady.backend.repo.TenantRepository;
import org.eclipse.steady.shared.enums.AffectedVersionSource;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>BugController class.</p>
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/bugs")
public class BugController {

  private static Logger log = LoggerFactory.getLogger(BugController.class);

  private final BugRepository bugRepository;

  private final AffectedLibraryRepository afflibRepository;

  private final LibraryRepository libRepository;

  private final TenantRepository tenantRepository;

  @Autowired
  BugController(
      BugRepository bugRepository,
      AffectedLibraryRepository afflibRepository,
      LibraryRepository libRepository,
      TenantRepository tenantRepository) {
    this.bugRepository = bugRepository;
    this.afflibRepository = afflibRepository;
    this.libRepository = libRepository;
    this.tenantRepository = tenantRepository;
  }

  /**
   * Returns a collection of all {@link Bug}s present in the backend.
   *
   * @param lang a {@link org.eclipse.steady.shared.enums.ProgrammingLanguage} object.
   * @return a {@link java.lang.Iterable} object.
   */
  @RequestMapping(
      value = "",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.Default.class)
  public Iterable<Bug> getBugs(
      @RequestParam(value = "lang", required = false) ProgrammingLanguage lang) {
    if (lang == null) return this.bugRepository.findAll();
    else return this.bugRepository.findBugByLang(lang);
  }

  /**
   * Returns a collection of all {@link Bug}s present in the backend.
   *
   * @return a {@link java.lang.Iterable} object.
   */
  @RequestMapping(
      value = "/dump",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.BugDetails.class)
  public Iterable<Bug> getBugAffLibIds() {
    return this.bugRepository.findAll();
  }

  /**
   * Creates a new {@link Bug} with a given bug ID (e.g., CVE identifier).
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
   */
  @RequestMapping(
      value = "",
      method = RequestMethod.POST,
      consumes = {"application/json;charset=UTF-8"},
      produces = {"application/json;charset=UTF-8"})
  public ResponseEntity<Bug> createBug(@RequestBody Bug bug) {
    try {
      final Bug existing_bug =
          BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bug.getBugId()));
      // Return CONFLICT to indicate that resource with this bug ID already exists
      return new ResponseEntity<Bug>(HttpStatus.CONFLICT);
    } catch (EntityNotFoundException e) {
      try {
        // skip av
        bug.setAffectedVersions(null);
        final Bug managed_bug = this.bugRepository.customSave(bug, true);
        return new ResponseEntity<Bug>(managed_bug, HttpStatus.CREATED);
      } catch (PersistenceException e1) {
        return new ResponseEntity<Bug>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
  }

  /**
   * <p>createAllBugs.</p>
   *
   * @param bugs an array of {@link org.eclipse.steady.backend.model.Bug} objects.
   * @return a {@link org.springframework.http.ResponseEntity} object.
   */
  @RequestMapping(
      value = "/dump",
      method = RequestMethod.POST,
      consumes = {"application/json;charset=UTF-8"},
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.BugDetails.class)
  public ResponseEntity<Bug[]> createAllBugs(@RequestBody Bug[] bugs) {
    List<Bug> saved = new ArrayList<Bug>();
    for (Bug b : bugs) {
      try {
        final Bug existing_bug =
            BugRepository.FILTER.findOne(this.bugRepository.findByBugId(b.getBugId()));
        // Return CONFLICT to indicate that resource with this bug ID already exists
        // return new ResponseEntity<Bug>(HttpStatus.CONFLICT);
        log.info("Bug " + b.getBugId() + " already exists");
      } catch (EntityNotFoundException e) {
        try {
          final Bug managed_bug = this.bugRepository.customSave(b, true);

          //						// Ensure that no affected libs for bug and source exist
          //						final List<AffectedLibrary> aff_libs =
          // this.afflibRepository.findByBug(managed_bug);
          //						if(aff_libs!=null && aff_libs.size()>0)
          //							//return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.CONFLICT);
          //							//should never happen as the bug is newly created
          //							System.out.println("["+ aff_libs.size()+ "] AffLibs for Bug " + b.getBugId() + "
          // already exists");

          // only save MANUAL affectedLib for libIds
          List<AffectedLibrary> tobesaved = new ArrayList<AffectedLibrary>();
          for (AffectedLibrary afflib : b.getAffectedVersions()) {
            if (afflib.getSource() == AffectedVersionSource.MANUAL
                && afflib.getLibraryId() != null
                && afflib.getLib() == null) {
              tobesaved.add(afflib);
            }
          }
          AffectedLibrary[] tobesavedA = new AffectedLibrary[tobesaved.size()];
          if (tobesaved.size() > 0) {
            this.afflibRepository.customSave(managed_bug, tobesaved.toArray(tobesavedA));
          }

          saved.add(managed_bug);
        } catch (PersistenceException e1) {
          return new ResponseEntity<Bug[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      }
    }
    Bug[] savedBugs = new Bug[saved.size()];
    // return new ResponseEntity<Bug[]>(saved.toArray(savedBugs), HttpStatus.CREATED);
    return new ResponseEntity<Bug[]>(HttpStatus.CREATED);
  }

  /**
   * Returns the {@link Bug} with the given external ID. This ID is provided by the user when creating a bug, e.g., a CVE identifier.
   *
   * @return 404 {@link HttpStatus#NOT_FOUND} if bug with given bug ID does not exist, 200 {@link HttpStatus#OK} if the bug is found
   * @param bugid a {@link java.lang.String} object.
   */
  @RequestMapping(
      value = "/{bugid}",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.BugDetails.class)
  public ResponseEntity<Bug> getBug(@PathVariable String bugid) {
    final StopWatch sw = new StopWatch("GET bug [" + bugid + "]").start();
    try {
      final Bug b = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
      this.bugRepository.updateCachedCveData(b, false);
      sw.stop();
      return new ResponseEntity<Bug>(b, HttpStatus.OK);
    } catch (EntityNotFoundException enfe) {
      sw.stop(enfe);
      return new ResponseEntity<Bug>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      sw.stop(e);
      return new ResponseEntity<Bug>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>isBugExisting.</p>
   *
   * @return 404 {@link HttpStatus#NOT_FOUND} if bug with given bugid does not exist, 200 {@link HttpStatus#OK} if the bug is found
   * @param bugid a {@link java.lang.String} object.
   */
  @RequestMapping(value = "/{bugid}", method = RequestMethod.OPTIONS)
  public ResponseEntity<Bug> isBugExisting(@PathVariable String bugid) {
    final StopWatch sw = new StopWatch("OPTIONS bug [" + bugid + "]").start();
    try {
      BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
      sw.stop();
      return new ResponseEntity<Bug>(HttpStatus.OK);
    } catch (EntityNotFoundException enfe) {
      sw.stop(enfe);
      return new ResponseEntity<Bug>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      sw.stop(e);
      return new ResponseEntity<Bug>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Re-creates the {@link Bug} with a given bug ID (e.g., CVE identifier).
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @return 404 {@link HttpStatus#NOT_FOUND} if bug with given bug ID does not
   *       exist, 422 {@link HttpStatus#UNPROCESSABLE_ENTITY} if the value of
   *       path variable (bug ID) does not equal the corresponding field in the
   *       body, 200 {@link HttpStatus#OK} if the bug was successfully re-created
   * @param bugid a {@link java.lang.String} object.
   */
  @RequestMapping(
      value = "/{bugid}",
      method = RequestMethod.PUT,
      consumes = {"application/json;charset=UTF-8"},
      produces = {"application/json;charset=UTF-8"})
  public ResponseEntity<Bug> updateBug(@PathVariable String bugid, @RequestBody Bug bug) {
    if (!bugid.equals(bug.getBugId()))
      return new ResponseEntity<Bug>(HttpStatus.UNPROCESSABLE_ENTITY);
    try {
      // skip av
      bug.setAffectedVersions(null);
      Bug managed_bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
      managed_bug = this.bugRepository.customSave(bug, true);
      return new ResponseEntity<Bug>(managed_bug, HttpStatus.OK);
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<Bug>(HttpStatus.NOT_FOUND);
    } catch (PersistenceException e) {
      return new ResponseEntity<Bug>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Deletes the {@link Bug} with the given external ID. This ID is provided by
   * the user when creating a bug, e.g., a CVE identifier.
   *
   * @return 404 {@link HttpStatus#NOT_FOUND} if bug with given bug ID does not
   * exist, 200 {@link HttpStatus#OK} if the bug was successfully deleted
   * @param bugid a {@link java.lang.String} object.
   */
  @RequestMapping(value = "/{bugid}", method = RequestMethod.DELETE)
  @CacheEvict(value = "bug")
  public ResponseEntity<Resource<Bug>> deleteBug(@PathVariable String bugid) {
    try {
      final Bug b = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));

      // Ensure that no affected libs for bug exist
      final List<AffectedLibrary> aff_libs = this.afflibRepository.findByBug(b);
      if (aff_libs != null && aff_libs.size() > 0)
        return new ResponseEntity<Resource<Bug>>(HttpStatus.UNPROCESSABLE_ENTITY);
      this.bugRepository.delete(b);
      return new ResponseEntity<Resource<Bug>>(HttpStatus.OK);
    } catch (EntityNotFoundException enfe) {
      return new ResponseEntity<Resource<Bug>>(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Creates a set of {@link AffectedLibrary}s for the given {@link Bug} and {@link AffectedVersionSource}.
   * Note that {@link AffectedLibrary}s cannot be modified or deleted individually, but always as bulk for a given {@link AffectedVersionSource}.
   * The only exception is the source CHECK_VERSION which allows the PUT method to add  new AffectedLibraries
   *
   * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created, 422 {@link HttpStatus#UNPROCESSABLE_ENTITY} if the source in the path variable and JSON content are not consistent or when attempting to save an affected lib for a digest verified against Maven Central (in such case the affected Library must be defined for the corresponding LibraryId)
   * @param bugid a {@link java.lang.String} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @param affectedLibraries an array of {@link org.eclipse.steady.backend.model.AffectedLibrary} objects.
   */
  @RequestMapping(
      value = "/{bugid}/affectedLibIds",
      method = RequestMethod.POST,
      consumes = {"application/json;charset=UTF-8"},
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.BugAffLibs.class)
  public ResponseEntity<List<AffectedLibrary>> createAffectedLibrary(
      @PathVariable String bugid,
      @RequestParam(value = "source", required = true) AffectedVersionSource source,
      @RequestBody AffectedLibrary[] affectedLibraries) {
    // Ensure that bug exists
    Bug bug = null;
    try {
      bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.NOT_FOUND);
    }

    // Ensure that no affected libs for bug and source exist
    final List<AffectedLibrary> aff_libs = this.afflibRepository.findByBugAndSource(bug, source);
    if (aff_libs != null
        && aff_libs.size()
            > 0) // Return CONFLICT to indicate that resource with this bug ID already exists
    return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.CONFLICT);

    // Ensure consistency of path variable and JSON content
    // Ensure that a manual affected Lib per digest is saved only if the SHA1 is not verified
    for (AffectedLibrary afflib : affectedLibraries) {
      if (!source.equals(afflib.getSource()))
        return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.UNPROCESSABLE_ENTITY);
      if (afflib.getSource() == AffectedVersionSource.MANUAL
          && afflib.getLibraryId() == null
          && afflib.getLib() != null) {
        final Boolean isWellKnown =
            LibraryRepository.FILTER
                .findOne(this.libRepository.findByDigest(afflib.getLib().getDigest()))
                .getWellknownDigest();
        if (isWellKnown != null && isWellKnown == true)
          return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.UNPROCESSABLE_ENTITY);
      }
    }

    // Save and return
    return new ResponseEntity<List<AffectedLibrary>>(
        this.afflibRepository.customSave(bug, affectedLibraries), HttpStatus.OK);
  }

  /**
   * Adds a set of {@link AffectedLibrary}s for the given {@link Bug} and {@link AffectedVersionSource}.
   * This method is only allows for source CHECK_VERSION, AST_EQUALITY, MINOR_EQUALITY MAJOR_EQUALITY GREATER_RELEASE TO_REVIEW
   *
   * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
   * @param bugid a {@link java.lang.String} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @param affectedLibraries an array of {@link org.eclipse.steady.backend.model.AffectedLibrary} objects.
   */
  @RequestMapping(
      value = "/{bugid}/affectedLibIds",
      method = RequestMethod.PUT,
      consumes = {"application/json;charset=UTF-8"},
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.BugAffLibs.class)
  public ResponseEntity<List<AffectedLibrary>> addAffectedLibrary(
      @PathVariable String bugid,
      @RequestParam(value = "source", required = true) AffectedVersionSource source,
      @RequestBody AffectedLibrary[] affectedLibraries) {
    // Ensure that PUT is only used by CHECK_VERSION or PATCH EVAL results
    final StopWatch sw = new StopWatch("PUT affected libraries for source: " + source).start();
    //
    //	if(!source.equals(AffectedVersionSource.CHECK_VERSION)&&!source.equals(AffectedVersionSource.AST_EQUALITY)&&!source.equals(AffectedVersionSource.GREATER_RELEASE)&&!source.equals(AffectedVersionSource.INTERSECTION)&&!source.equals(AffectedVersionSource.MINOR_EQUALITY)&&!source.equals(AffectedVersionSource.MAJOR_EQUALITY)&&!source.equals(AffectedVersionSource.TO_REVIEW) &&!source.equals(AffectedVersionSource.PROPAGATE_MANUAL)){
    //			sw.lap("not allowed",true);
    //			return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.UNPROCESSABLE_ENTITY);
    //		}

    // Ensure that bug exists
    Bug bug = null;
    try {
      bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.NOT_FOUND);
    }

    // Ensure consistency of path variable and JSON content
    for (AffectedLibrary afflib : affectedLibraries)
      if (!source.equals(afflib.getSource())) {
        sw.lap("path/json inconsistency", true);
        return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.UNPROCESSABLE_ENTITY);
      }
    sw.stop();
    // Save and return
    return new ResponseEntity<List<AffectedLibrary>>(
        this.afflibRepository.customSave(bug, affectedLibraries), HttpStatus.OK);
  }

  /**
   * Creates a set of {@link AffectedLibrary}s for the given {@link Bug} and
   * {@link AffectedVersionSource}. If the resolved flag is true, the source
   * flag is ignored and for each
   * {@link org.eclipse.steady.backend.model.LibraryId} only the
   * {@link AffectedLibrary} with the highest priority is returned. Note that
   * {@link AffectedLibrary}s cannot be created, modified or deleted
   * individually, but always as bulk for a given {@link AffectedVersionSource}.
   *
   * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already
   * exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
   * @param bugid a {@link java.lang.String} object.
   * @param source a
   * {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @param resolved a {@link java.lang.Boolean} object
   * @param onlyWellknown a {@link java.lang.Boolean} object
   */
  @RequestMapping(
      value = "/{bugid}/affectedLibIds",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.BugAffLibs.class)
  public ResponseEntity<List<AffectedLibrary>> getAllAffectedLibraries(
      @PathVariable String bugid,
      @RequestParam(value = "resolved", required = false, defaultValue = "false") Boolean resolved,
      @RequestParam(value = "source", required = false) AffectedVersionSource source,
      @RequestParam(value = "onlyWellKnown", required = false, defaultValue = "false")
          Boolean onlyWellknown) {
    Bug bug = null;
    try {
      bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.NOT_FOUND);
    }
    if (!resolved)
      return new ResponseEntity<List<AffectedLibrary>>(
          this.afflibRepository.getAffectedLibraries(bug, source, onlyWellknown), HttpStatus.OK);
    else
      return new ResponseEntity<List<AffectedLibrary>>(
          this.afflibRepository.getResolvedAffectedLibraries(bug, onlyWellknown), HttpStatus.OK);
  }

  /**
   * <p>getAffectedLibrariesByGA.</p>
   *
   * @param bugid a {@link java.lang.String} object.
   * @param mvnGroup a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @return a {@link org.springframework.http.ResponseEntity} object.
   */
  @RequestMapping(
      value = "/{bugid}/affectedLibIds/{mvnGroup:.+}/{artifact:.+}",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.BugAffLibsDetails.class)
  public ResponseEntity<List<AffectedLibrary>> getAffectedLibrariesByGA(
      @PathVariable String bugid,
      @PathVariable String mvnGroup,
      @PathVariable String artifact,
      @RequestParam(value = "source", required = false) AffectedVersionSource source) {
    Bug bug = null;
    try {
      bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.NOT_FOUND);
    }
    if (source == null)
      return new ResponseEntity<List<AffectedLibrary>>(
          this.afflibRepository.findByBugAndGA(bug, mvnGroup, artifact), HttpStatus.OK);
    else
      return new ResponseEntity<List<AffectedLibrary>>(
          this.afflibRepository.findByBugAndGAAndSource(bug, mvnGroup, artifact, source),
          HttpStatus.OK);
  }

  /**
   * <p>getAffectedLibraryDetails.</p>
   *
   * @param bugid a {@link java.lang.String} object.
   * @param mvnGroup a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @return a {@link org.springframework.http.ResponseEntity} object.
   */
  @RequestMapping(
      value = "/{bugid}/affectedLibIds/{mvnGroup:.+}/{artifact:.+}/{version:.+}",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.BugAffLibsDetails.class)
  public ResponseEntity<List<AffectedLibrary>> getAffectedLibraryDetails(
      @PathVariable String bugid,
      @PathVariable String mvnGroup,
      @PathVariable String artifact,
      @PathVariable String version,
      @RequestParam(value = "source", required = false) AffectedVersionSource source) {
    Bug bug = null;
    try {
      bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.NOT_FOUND);
    }
    if (source == null)
      return new ResponseEntity<List<AffectedLibrary>>(
          this.afflibRepository.findByBugAndLibId(bug, mvnGroup, artifact, version), HttpStatus.OK);
    else
      return new ResponseEntity<List<AffectedLibrary>>(
          this.afflibRepository.findByBugAndLibraryIdAndSource(
              bug, mvnGroup, artifact, version, source),
          HttpStatus.OK);
  }

  /**
   * <p>areBugAffectedLibrariesExisting.</p>
   *
   * @return 404 {@link HttpStatus#NOT_FOUND} if affected libraries for a given bugId and source does not exist, 200 {@link HttpStatus#OK} if they are found
   * @param bugid a {@link java.lang.String} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   */
  @RequestMapping(value = "/{bugid}/affectedLibIds", method = RequestMethod.OPTIONS)
  public ResponseEntity<List<AffectedLibrary>> areBugAffectedLibrariesExisting(
      @PathVariable String bugid,
      @RequestParam(value = "source", required = true) AffectedVersionSource source) {
    Bug bug = null;
    try {
      bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
    } catch (EntityNotFoundException enfe) {
      return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.NOT_FOUND);
    }
    final List<AffectedLibrary> aff_libs = this.afflibRepository.findByBugAndSource(bug, source);
    if (aff_libs != null
        && aff_libs.size()
            > 0) // Return CONFLICT to indicate that resource with this bug ID already exists
    return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.OK);
    else return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.NOT_FOUND);
  }

  /**
   * Creates a set of {@link AffectedLibrary}s for the given {@link Bug} and {@link AffectedVersionSource}.
   * Note that {@link AffectedLibrary}s cannot be created, modified or deleted individually, but always as bulk for a given {@link AffectedVersionSource}.
   *
   * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
   * @param bugid a {@link java.lang.String} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   */
  @RequestMapping(
      value = "/{bugid}/affectedLibIds",
      method = RequestMethod.DELETE,
      produces = {"application/json;charset=UTF-8"})
  public ResponseEntity<List<AffectedLibrary>> deleteAffectedLibraries(
      @PathVariable String bugid,
      @RequestParam(value = "source", required = true) AffectedVersionSource source) {
    // Ensure that bug exists
    Bug bug = null;
    try {
      bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.NOT_FOUND);
    }

    // Ensure that affected libs for bug and source exist
    final List<AffectedLibrary> aff_libs = this.afflibRepository.findByBugAndSource(bug, source);
    if (aff_libs == null || aff_libs.size() == 0)
      return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.NOT_FOUND);
    for (AffectedLibrary aff_lib : aff_libs) {
      if (aff_lib.getAffectedcc() != null)
        // Delete affected construct changes
        this.afflibRepository.deleteCCByAffLib(aff_lib);
    }

    // Delete and return
    this.afflibRepository.deleteByBugAndSource(bug, source);
    return new ResponseEntity<List<AffectedLibrary>>(HttpStatus.OK);
  }

  /**
   * <p>getAllBugLibraries.</p>
   *
   * @param bugid a {@link java.lang.String} object.
   * @return a {@link org.springframework.http.ResponseEntity} object.
   */
  @RequestMapping(
      value = "/{bugid}/libraries",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.Default.class)
  public ResponseEntity<List<Library>> getAllBugLibraries(@PathVariable String bugid) {
    Bug bug = null;
    try {
      bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugid));
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<List<Library>>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<List<Library>>(
        this.libRepository.findJPQLVulnerableLibrariesByBug(bugid), HttpStatus.OK);
  }

  /**
   * https://shdhumale.wordpress.com/2011/07/07/code-to-compress-and-decompress-json-object/
   * @param _request
   * @return
   */
  //	@RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/gzip"})
  //	Bug postGzippedBug(HttpServletRequest _request) {
  //		try {
  //			ArchiveInputStream ais = new
  // ArchiveStreamFactory().createArchiveInputStream(_request.getInputStream());
  //			ByteArrayOutputStream out = new ByteArrayOutputStream();
  //			IOUtils.copy(new GZIPInputStream(ais), out);
  //			String json = new String(out.toByteArray());
  //			log.error(json);
  //			//TODO: Use Jackson to get Bug and call save
  //		} catch (ArchiveException e) {
  //			// TODO Auto-generated catch block
  //			log.error("Error: " + e.getMessage(), e);
  //		} catch (IOException e) {
  //			// TODO Auto-generated catch block
  //			log.error("Error: " + e.getMessage(), e);
  //		}
  //		return null;
  //	}

  /**
   * http://stackoverflow.com/questions/8585216/spring-forward-with-added-parameters
   * @param model
   * @return
   */
  /*@RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/zip"})
  public ModelAndView redirectWithUsingForwardPrefix(ModelMap model) {
      model.addAttribute("attribute", "forwardWithForwardPrefix");
      return new ModelAndView("forward:/bugss", model);
  }*/

  /**
   * Returns the {@link Bug} with the given internal ID. This ID is created internally upon saving a bug.
   * Returns {@link HttpStatus#NOT_FOUND} if no bug with the given internal ID can be found.
   * @param id
   * @return
   */
  /*@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
  ResponseEntity<Bug> getBugById(@PathVariable Long id) {
  	final Bug b = this.bugRepository.findOne(id);
  	if(b==null) {
  		return new ResponseEntity<Bug>(HttpStatus.NOT_FOUND);
  	}
  	else {
  		//Resource<Bug> resource = new Resource<Bug>(b);
  		return new ResponseEntity<Bug>(b, HttpStatus.OK);
  	}
  }*/

  /*@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {"application/json"})
  ResponseEntity<Resource<Bug>> getBug(@PathVariable Long id) {
  	final Bug b = this.bugRepository.findOne(id);
  	if(b==null) {
  		return new ResponseEntity<Resource<Bug>>(HttpStatus.NOT_FOUND);
  	}
  	else {
  		//Resource<Bug> resource = new Resource<Bug>(b);
  		return ResponseEntity.ok(this.buildBugResource(b));
  	}
  }*/

  /*private Resource<Bug> buildBugResource(Bug bug) {
  	final Link bugLink = linkTo(BugController.class).slash('/').slash(bug.getId()).withSelfRel();
      final Resource<Bug> r = new Resource<Bug>(bug, bugLink.expand(bug.getId()));
      return r;
  }*/
}
