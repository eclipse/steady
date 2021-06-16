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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import org.eclipse.steady.backend.model.AffectedConstructChange;
import org.eclipse.steady.backend.model.Application;
import org.eclipse.steady.backend.model.Bug;
import org.eclipse.steady.backend.model.ConstructId;
import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.view.Views;
import org.eclipse.steady.backend.repo.ApplicationRepository;
import org.eclipse.steady.backend.repo.BugRepository;
import org.eclipse.steady.backend.repo.LibraryRepository;
import org.eclipse.steady.backend.util.ServiceWrapper;
import org.eclipse.steady.shared.connectivity.ServiceConnectionException;
import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.enums.Scope;
import org.eclipse.steady.shared.util.StopWatch;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// hateos.Resource was renamed into EntityModel (see
// https://docs.spring.io/spring-hateoas/docs/current/reference/html/#migrate-to-1.0)
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * <p>LibraryController class.</p>
 *
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/libs")
public class LibraryController {
  private static Logger log = LoggerFactory.getLogger(LibraryController.class);

  private final LibraryRepository libRepository;

  private final ApplicationRepository appRepository;

  private final BugRepository bugRepository;

  @Autowired
  LibraryController(
      LibraryRepository libraryRepository,
      ApplicationRepository appRepository,
      BugRepository bugRepository) {
    this.libRepository = libraryRepository;
    this.appRepository = appRepository;
    this.bugRepository = bugRepository;
  }

  /**
   * Returns a collection of all {@link Library}s present in the backend.
   *
   * @param mostUsed a {@link java.lang.Integer} object.
   * @param excludedScopes an array of {@link org.eclipse.steady.shared.enums.Scope} objects.
   * @return a {@link java.lang.Iterable} object.
   */
  @RequestMapping(
      value = "",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.Overview.class)
  public Iterable<Library> getLibraries(
      @RequestParam(value = "mostUsed", required = false, defaultValue = "0") Integer mostUsed,
      @RequestParam(value = "excludedScopes", required = false, defaultValue = "")
          Scope[] excludedScopes) {
    List<Library> list = new ArrayList<Library>();
    if (mostUsed == 0) return this.libRepository.findAll();
    if (mostUsed > 0) {
      List<String> digests = null;
      if (excludedScopes == null || excludedScopes.length == 0) {
        digests = this.libRepository.findMostUsed(mostUsed);
        for (String s : digests) {
          Library l = LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(s));
          l.setDirectUsageCounter(this.libRepository.countUsages(s));
          list.add(l);
        }
      } else {
        List<Object[]> libs = this.libRepository.findMostUsed(excludedScopes);
        int i = 0;
        for (Object[] lib : libs) {
          if (i < mostUsed) {
            Library l =
                LibraryRepository.FILTER.findOne(this.libRepository.findByDigest((String) lib[0]));
            l.setDirectUsageCounter(((Long) lib[1]).intValue());
            list.add(l);
            i++;
          } else break;
        }
      }
    }
    return list;
  }

  /**
   * Creates a new {@link Bug} with a given bug ID (e.g., CVE identifier).
   *
   * @param library a {@link org.eclipse.steady.backend.model.Library} object.
   * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
   * @param skipResponseBody a {@link java.lang.Boolean} object.
   */
  @RequestMapping(
      value = "",
      method = RequestMethod.POST,
      consumes = {"application/json;charset=UTF-8"},
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.LibDetails.class)
  public ResponseEntity<Library> createLibrary(
      @RequestBody Library library,
      @RequestParam(value = "skipResponseBody", required = false, defaultValue = "false")
          Boolean skipResponseBody) {
    try {
      final Library existing_lib =
          LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(library.getDigest()));
      // Return CONFLICT to indicate that resource with this digest already exists
      return new ResponseEntity<Library>(HttpStatus.CONFLICT);
    } catch (EntityNotFoundException e) {
      try {
        final Library lib = this.libRepository.customSave(library);
        if (skipResponseBody) {
          return new ResponseEntity<Library>(HttpStatus.CREATED);
        } else return new ResponseEntity<Library>(lib, HttpStatus.CREATED);
      } catch (PersistenceException e1) {
        return new ResponseEntity<Library>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
  }

  /**
   * Returns the {@link Library} with the given digest.
   *
   * @param digest a {@link java.lang.String} object.
   * @return 404 {@link HttpStatus#NOT_FOUND} if library with given digest does not exist, 200 {@link HttpStatus#OK} if the library is found
   */
  @RequestMapping(
      value = "/{digest}",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.LibDetails.class)
  public ResponseEntity<Library> getLibrary(@PathVariable String digest) {
    try {
      return new ResponseEntity<Library>(
          LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(digest)), HttpStatus.OK);
    } catch (EntityNotFoundException enfe) {
      return new ResponseEntity<Library>(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Returns the {@link Application} with dependencies on the given digest.
   *
   * @param digest a {@link java.lang.String} object.
   * @return 404 {@link HttpStatus#NOT_FOUND} if no application depends on the given digest or digest does not exists, 200 {@link HttpStatus#OK} if the applications are found
   */
  @RequestMapping(
      value = "/{digest}/apps",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.AppDepDetails.class)
  public ResponseEntity<List<Application>> getLibraryApplications(@PathVariable String digest) {
    try {
      // To throw an exception if the entity is not found
      Library l = LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(digest));

      return new ResponseEntity<List<Application>>(
          this.appRepository.findAppsWithDigest(digest), HttpStatus.OK);
    } catch (EntityNotFoundException enfe) {
      return new ResponseEntity<List<Application>>(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Re-creates the {@link Library} with a given digest.
   *
   * @param digest a {@link java.lang.String} object.
   * @return 404 {@link HttpStatus#NOT_FOUND} if library with given digest does not exist,
   * 		   422 {@link HttpStatus.UNPROCESSABLE_ENTITY} if the value of path variable (digest) is not equal to the corresponding field in the body
   * 		   200 {@link HttpStatus#OK} if the library was successfully re-created
   * @param library a {@link org.eclipse.steady.backend.model.Library} object.
   * @param skipResponseBody a {@link java.lang.Boolean} object.
   */
  @RequestMapping(
      value = "/{digest}",
      method = RequestMethod.PUT,
      consumes = {"application/json;charset=UTF-8"},
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.LibDetails.class)
  public ResponseEntity<Library> updateLibrary(
      @PathVariable String digest,
      @RequestBody Library library,
      @RequestParam(value = "skipResponseBody", required = false, defaultValue = "false")
          Boolean skipResponseBody) {
    if (!digest.equals(library.getDigest()))
      return new ResponseEntity<Library>(HttpStatus.UNPROCESSABLE_ENTITY);
    try {
      Library managed_lib =
          LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(digest));
      managed_lib = this.libRepository.customSave(library);
      if (skipResponseBody) {
        return new ResponseEntity<Library>(HttpStatus.OK);
      } else return new ResponseEntity<Library>(managed_lib, HttpStatus.OK);
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<Library>(HttpStatus.NOT_FOUND);
    } catch (PersistenceException e) {
      return new ResponseEntity<Library>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Updates the meta-data of a {@link Library} with a given digest. In particular it updates the digest verified flag and release timestamp fields.
   *
   * @param digest a {@link java.lang.String} object.
   * @return 404 {@link HttpStatus#NOT_FOUND} if library with given digest does not exist,
   * 		   200 {@link HttpStatus#OK} if the library metadata were successfully updated
   * @param skipResponseBody a {@link java.lang.Boolean} object.
   */
  @RequestMapping(
      value = "/{digest}/updateMetadata",
      method = RequestMethod.PUT,
      consumes = {"application/json;charset=UTF-8"},
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.LibDetails.class)
  public ResponseEntity<Library> updateLibraryMetaData(
      @PathVariable String digest,
      @RequestParam(value = "skipResponseBody", required = false, defaultValue = "false")
          Boolean skipResponseBody) {
    try {
      Library managed_lib =
          LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(digest));
      managed_lib = this.libRepository.customSave(managed_lib);
      if (skipResponseBody) {
        return new ResponseEntity<Library>(HttpStatus.OK);
      } else return new ResponseEntity<Library>(managed_lib, HttpStatus.OK);
    } catch (EntityNotFoundException e) {
      return new ResponseEntity<Library>(HttpStatus.NOT_FOUND);
    } catch (PersistenceException e) {
      return new ResponseEntity<Library>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>isLibraryExisting.</p>
   *
   * @param digest a {@link java.lang.String} object.
   * @return 404 {@link HttpStatus#NOT_FOUND} if library with given digest does not exist, 200 {@link HttpStatus#OK} if the library is found
   */
  @RequestMapping(value = "/{digest}", method = RequestMethod.OPTIONS)
  public ResponseEntity<Library> isLibraryExisting(@PathVariable String digest) {
    try {
      LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(digest));
      return new ResponseEntity<Library>(HttpStatus.OK);
    } catch (EntityNotFoundException enfe) {
      return new ResponseEntity<Library>(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Deletes the {@link Library} with the given external ID. This ID is provided by the user when creating a bug, e.g., a CVE identifier.
   *
   * @return 404 {@link HttpStatus#NOT_FOUND} if bug with given bug ID does not exist, 200 {@link HttpStatus#OK} if the bug was successfully deleted
   * @param digest a {@link java.lang.String} object.
   */
  @RequestMapping(
      value = "/{digest}",
      method = RequestMethod.DELETE,
      produces = {"application/json;charset=UTF-8"})
  public ResponseEntity<EntityModel<Library>> deleteLibrary(@PathVariable String digest) {
    try {
      final Library lib = LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(digest));
      this.libRepository.delete(lib);
      return new ResponseEntity<EntityModel<Library>>(HttpStatus.OK);
    } catch (EntityNotFoundException enfe) {
      return new ResponseEntity<EntityModel<Library>>(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Returns a collection of {@link Bug}s relevant for the {@link Library} with the given digest.
   *
   * @param digest a {@link java.lang.String} object.
   * @return 404 {@link HttpStatus#NOT_FOUND} if library with given digest does not exist, 200 {@link HttpStatus#OK} if the library is found
   * @param selectedBugs an array of {@link java.lang.String} objects.
   * @param geCvss a float.
   */
  @RequestMapping(
      value = "/{digest}/bugs",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.Default.class)
  public ResponseEntity<List<Bug>> getLibraryBugs(
      @PathVariable String digest,
      @RequestParam(value = "selectedBugs", required = false, defaultValue = "")
          String[] selectedBugs,
      @RequestParam(value = "geCvss", required = false, defaultValue = "0") float geCvss) {
    try {
      // To throw an exception if the entity is not found
      LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(digest));

      // The candidates
      List<Bug> bug_ids = null;
      if (selectedBugs == null || selectedBugs.length == 0)
        bug_ids = this.libRepository.findBugs(digest);
      else bug_ids = this.libRepository.findBugs(digest, selectedBugs);

      // The result set
      List<Bug> result = null;

      // Use (valid) CVSS filter
      if (geCvss > 0F && geCvss <= 10F) {
        result = new ArrayList<Bug>();
        for (Bug b : bug_ids) {
          this.bugRepository.updateCachedCveData(b, false);
          if (b.getCvssScore() != null && b.getCvssScore() < geCvss) continue;
          else result.add(b);
        }
      }
      // No (or invalid) CVSS filter provided
      else {
        result = bug_ids;
      }

      return new ResponseEntity<List<Bug>>(result, HttpStatus.OK);
    } catch (EntityNotFoundException enfe) {
      return new ResponseEntity<List<Bug>>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(
      value = "/{digest}/bugs/{bugId}/constructIds",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  ResponseEntity<List<ConstructId>> getBuggyLibraryConstructIds(
      @PathVariable String digest, @PathVariable String bugId) {
    return new ResponseEntity<List<ConstructId>>(
        this.libRepository.findBuggyConstructIds(digest, bugId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/{digest}/bugs/{bugId}/affConstructChanges",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  ResponseEntity<List<AffectedConstructChange>> getLibraryAffectedCCs(
      @PathVariable String digest, @PathVariable String bugId) {
    return new ResponseEntity<List<AffectedConstructChange>>(
        this.libRepository.findAffCCs(digest, bugId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/{digest}/constructIds",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  ResponseEntity<List<ConstructId>> getLibraryConstructIds(@PathVariable String digest) {
    return new ResponseEntity<List<ConstructId>>(
        this.libRepository.findConstructIds(digest), HttpStatus.OK);
  }

  /*@RequestMapping(value = "/buss", method = RequestMethod.GET)
  public ModelAndView redirectWithUsingForwardPrefix(ModelMap model) {
      model.addAttribute("attribute", "forwardWithForwardPrefix");
      return new ModelAndView("forward:/bugss", model);
  }*/

  @RequestMapping(
      value = "/{digest}/upload",
      method = RequestMethod.POST,
      consumes = {"application/octet-stream"})
  @JsonView(Views.LibDetails.class)
  ResponseEntity<Library> postLibraryJAR(
      @PathVariable String digest, HttpEntity<byte[]> requestEntity) {
    LibraryController.log.info("Called postLibrary for JAR [" + digest + "]");
    try {
      byte[] payload = requestEntity.getBody();
      InputStream inputStream = new ByteArrayInputStream(payload);

      if (!Files.exists(
          Paths.get(
              VulasConfiguration.getGlobal().getLocalM2Repository().toString()
                  + File.separator
                  + "uknownJars"))) {
        Files.createDirectories(
            Paths.get(
                VulasConfiguration.getGlobal().getLocalM2Repository().toString()
                    + File.separator
                    + "uknownJars"));
      }

      String saveFilePath =
          VulasConfiguration.getGlobal().getLocalM2Repository().toString()
              + File.separator
              + "uknownJars"
              + File.separator
              + digest
              + ".jar";

      LibraryController.log.info("Saving JAR [" + digest + "] to file [" + saveFilePath + "]");
      // opens an output stream to save into file
      FileOutputStream outputStream = new FileOutputStream(saveFilePath);

      int bytesRead = -1;
      byte[] buffer = new byte[inputStream.available()];
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      outputStream.close();

      inputStream.close();
      return new ResponseEntity<Library>(HttpStatus.OK);
    } catch (IOException e) {
      LibraryController.log.error("Error while saving the received JAR file [" + e + "]");
      return new ResponseEntity<Library>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   *
   * @param digest
   * @return 404 {@link HttpStatus#NOT_FOUND} if the library file needs to be uploaded (not known to Maven central and not already uploaded), 200 otherwise
   */
  @RequestMapping(value = "/{digest}/upload", method = RequestMethod.OPTIONS)
  ResponseEntity<Library> isJARrequired(
      @PathVariable String digest, HttpEntity<byte[]> requestEntity) {
    final Library l = LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(digest));
    if (l.getWellknownDigest() == false) {
      final File f =
          new File(
              VulasConfiguration.getGlobal().getLocalM2Repository().toString()
                  + File.separator
                  + "uknownJars"
                  + File.separator
                  + digest
                  + ".jar");
      if (!f.exists()) return new ResponseEntity<Library>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<Library>(HttpStatus.OK);
  }

  /**
   * <p>getLibraryLibId.</p>
   *
   * @param digest a {@link java.lang.String} object.
   * @return a {@link org.springframework.http.ResponseEntity} object.
   */
  @RequestMapping(
      value = "/{digest}/identifyLibId",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  @JsonView(Views.Default.class)
  public ResponseEntity<Set<org.eclipse.steady.shared.json.model.LibraryId>> getLibraryLibId(
      @PathVariable String digest) {
    try {
      final StopWatch sw = new StopWatch("Identify Libid for sha1 : " + digest).start();

      LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(digest));
      // find all sha1 having +/- 10% of the packages of the original Jar
      List<String> samePacks = this.libRepository.findDigestSamePack(digest);
      sw.lap(
          "About to check ["
              + samePacks.size()
              + "] sha1s found in Vulas +/- 10% of the packages of the original Jar",
          true);

      Set<org.eclipse.steady.shared.json.model.LibraryId> results =
          new TreeSet<org.eclipse.steady.shared.json.model.LibraryId>();

      // used to collect all versions (not only those stored in vulas) for later processing
      Collection<org.eclipse.steady.shared.json.model.LibraryId> toProcess =
          new ArrayList<org.eclipse.steady.shared.json.model.LibraryId>();

      List<ConstructId> cids = this.libRepository.findConstructIds(digest);
      LibraryController.log.info(
          "The original JAR [" + digest + "] has [" + cids.size() + "] constructs.");
      for (String s : samePacks) {
        LibraryController.log.info("Found digest [" + s + "] with same packages +/- 10%");

        // get library
        Library l = LibraryRepository.FILTER.findOne(this.libRepository.findByDigest(s));
        if (l.getLibraryId() != null)
          toProcess.addAll(
              ServiceWrapper.getInstance()
                  .getAllArtifactVersions(
                      l.getLibraryId().getMvnGroup(), l.getLibraryId().getArtifact(), false, null));

        List<ConstructId> other_cids = this.libRepository.findConstructIds(s);

        if (!(other_cids.size() == cids.size())) {
          LibraryController.log.info(
              "The candidate lib ["
                  + s
                  + "] has ["
                  + other_cids.size()
                  + "] constructs in Vulas whereas the original has ["
                  + cids.size()
                  + "]; skip library.");
          continue;
        }
        boolean equal = true;
        for (ConstructId c : other_cids) {
          if (!cids.contains(c)) {
            equal = false;
            LibraryController.log.info(
                "The candidate lib ["
                    + s
                    + "] contains a construct ["
                    + c.toString()
                    + "] not included in the requested jar; skip lib");
            break;
          }
        }
        if (equal) {
          LibraryController.log.info(
              "[" + s + "] is equal (contains all and only the same construt signatures");
          //	Library l = LibraryRepository.FILTER.findOne(this.libRepository.findBySha1(s));
          if (l.getLibraryId() != null) results.add(l.getLibraryId().toSharedType());
        }
      }
      // if none of the libs in vulas has the same constructs than the requested digest,
      // then start looking among all those in maven central with the same group artifact
      // and stop at the first match
      // if(results.isEmpty()){
      if (true) {
        sw.lap("Going to check for [" + toProcess.size() + "] libids found in Maven", true);
        List<ConstructId> pack_cids =
            this.libRepository.findConstructIdsOfType(digest, ConstructType.PACK);
        LibraryController.log.info("Size of packages of original JAR is: " + pack_cids.size());

        for (org.eclipse.steady.shared.json.model.LibraryId lid : toProcess) {

          List<org.eclipse.steady.shared.json.model.ConstructId> other_pack =
              ServiceWrapper.getInstance()
                  .getArtifactConstructs(
                      lid.getMvnGroup(), lid.getArtifact(), lid.getVersion(), ConstructType.PACK);
          sw.lap("Packs for " + lid.toString() + "received from cia ", true);
          // check
          if (!(other_pack.size() == pack_cids.size())) {
            LibraryController.log.info(
                "["
                    + lid.toString()
                    + "] has ["
                    + other_pack.size()
                    + "] packages instead of ["
                    + pack_cids.size()
                    + "]");
            continue;
          }
          boolean equal = true;
          for (org.eclipse.steady.shared.json.model.ConstructId c : other_pack) {
            if (!pack_cids.contains(new ConstructId(c.getLang(), c.getType(), c.getQname()))) {
              equal = false;
              LibraryController.log.info(
                  "["
                      + lid.toString()
                      + "] contains a pack not included in the requested jar: "
                      + c.toString());
              break;
            }
          }
          if (equal) {
            List<org.eclipse.steady.shared.json.model.ConstructId> other =
                ServiceWrapper.getInstance()
                    .getArtifactConstructs(
                        lid.getMvnGroup(), lid.getArtifact(), lid.getVersion(), null);

            sw.lap("All constructs for [" + lid.toString() + "] received from cia ", true);
            if (!(other.size() == cids.size())) {
              LibraryController.log.info(
                  "Size of constructs of [" + lid.toString() + "] is: " + other.size());
              continue;
            }
            equal = true;
            for (org.eclipse.steady.shared.json.model.ConstructId c : other) {
              if (!cids.contains(new ConstructId(c.getLang(), c.getType(), c.getQname()))) {
                equal = false;
                LibraryController.log.info(
                    "["
                        + lid.toString()
                        + "] contains a construct not included in the requested jar: "
                        + c.toString());
                break;
              }
            }
            if (equal) {
              LibraryController.log.info("[" + lid.toString() + "] is equal");
              results.add(lid);
              // break;
            }
          }
        }
      }

      return new ResponseEntity<Set<org.eclipse.steady.shared.json.model.LibraryId>>(
          results, HttpStatus.OK);

    } catch (EntityNotFoundException enfe) {
      return new ResponseEntity<Set<org.eclipse.steady.shared.json.model.LibraryId>>(
          HttpStatus.NOT_FOUND);
    } catch (ServiceConnectionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return new ResponseEntity<Set<org.eclipse.steady.shared.json.model.LibraryId>>(
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
