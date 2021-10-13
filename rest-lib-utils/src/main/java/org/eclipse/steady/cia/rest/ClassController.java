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
package org.eclipse.steady.cia.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.steady.cia.util.ClassDownloader;
import org.eclipse.steady.cia.util.MavenCentralWrapper;
import org.eclipse.steady.cia.util.ClassDownloader.Format;
import org.eclipse.steady.shared.json.model.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>ClassController class.</p>
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/classes")
public class ClassController {

  private static Logger log = LoggerFactory.getLogger(ClassController.class);

  /**
   * Returns the source code of the Java construct with the given name, as contained in the given Maven artifact.
   *
   * @param mvnGroup a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @param classname a {@link java.lang.String} object.
   * @param format a {@link java.lang.String} object.
   * @param response a {@link javax.servlet.http.HttpServletResponse} object.
   */
  @RequestMapping(
      value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/{classname:.+}",
      method = RequestMethod.GET)
  public void getClassfile(
      @PathVariable String mvnGroup,
      @PathVariable String artifact,
      @PathVariable String version,
      @PathVariable String classname,
      @RequestParam(value = "format", required = false, defaultValue = "CLASS") String format,
      HttpServletResponse response) {
    Format frmt = null;
    Path file = null;
    try {
      frmt = ClassDownloader.toFormat(format);
      file = ClassDownloader.getInstance().getClass(mvnGroup, artifact, version, classname, frmt);

      if (file == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot retrieve class");
        response.flushBuffer();
      } else {
        // Headers
        response.setContentType(ClassDownloader.getContentType(frmt));
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=" + classname + "." + frmt.toString().toLowerCase());

        FileInputStream fis = new FileInputStream(file.toFile());
        IOUtils.copy(fis, response.getOutputStream());
        response.flushBuffer();
        fis.close();

        // Delete
        try {
          Files.delete(file);
        } catch (Exception e) {
          log.error("Cannot delete file [" + file + "]: " + e.getMessage());
        }
      }
    } catch (IllegalArgumentException iae) {
      throw new RuntimeException(iae.getMessage());
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Cannot read file [" + file + "]");
    } catch (IOException e) {
      throw new RuntimeException("IO exception when reading file [" + file + "]");
    } catch (Exception e) {
      throw new RuntimeException("Exception writing file to output stream");
    }
  }

  /**
   * <p>getClassArtifacts.</p>
   *
   * @param classname a {@link java.lang.String} object.
   * @param rows a {@link java.lang.String} object (max allowed value: 989, default: 989).
   * @return a {@link org.springframework.http.ResponseEntity} object.
   */
  @RequestMapping(value = "/libraryIds/{classname:.+}", method = RequestMethod.GET)
  public ResponseEntity<Set<Artifact>> getClassArtifacts(
      @PathVariable String classname,
      @RequestParam(value = "rows", required = false, defaultValue = "989") String rows) {
    try {
      //			final String url = "http://search.maven.org/solrsearch/select?q={q}&rows={rows}&wt={wt}";
      //			final StringBuilder query = new StringBuilder();
      //			query.append("fc:\"").append(classname).append("\"");
      //			final Map<String,String> params = new HashMap<String,String>();
      //			params.put("q", query.toString());
      //			params.put("rows", "1000");
      //			params.put("wt", "json");
      //
      //			// Make the query
      //			final RestTemplate rest_template = new RestTemplate();
      //			final MavenVersionsSearch search = rest_template.getForObject(url,
      // MavenVersionsSearch.class, params);
      //
      MavenCentralWrapper m = new MavenCentralWrapper();

      return new ResponseEntity<Set<Artifact>>(
          m.getArtifactForClass(classname, rows, null, "jar"), HttpStatus.OK);
    } catch (Exception e) {
      log.error("Error: " + e.getMessage(), e);
      return new ResponseEntity<Set<Artifact>>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
