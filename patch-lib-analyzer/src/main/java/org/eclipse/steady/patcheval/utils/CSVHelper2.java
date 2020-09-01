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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eclipse.steady.patcheval.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.patcheval.representation.ArtifactResult2;
import org.eclipse.steady.patcheval.representation.ConstructPathAssessment2;
import org.eclipse.steady.patcheval.representation.ConstructPathLibResult2;
import org.eclipse.steady.shared.json.model.LibraryId;
import org.eclipse.steady.shared.util.FileUtil;

/**
 * Helper class for writing results to file.
 */
public class CSVHelper2 {
  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  static String COMMA_DELIMITER = ";";
  static String NEW_LINE_SEPARATOR = "\n";
  static Random ran = new Random();

  /**
   * <p>getShortRepoPathName.</p>
   *
   * @param repoPath a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static String getShortRepoPathName(String repoPath) {
    int idx = repoPath.indexOf("/src/");
    int idx_trunk;
    int idx_branches;
    int idx_slash;
    if (idx != -1) {
      return repoPath.substring(0, idx);
    } else {
      idx_trunk = repoPath.indexOf("/trunk/");
      idx_branches = repoPath.indexOf("/branches/");
      if (idx_trunk != -1 && idx_branches == -1) {
        return repoPath.substring(0, idx_trunk + 7);
      } else if (idx_trunk == -1 && idx_branches != -1) {
        idx_slash = repoPath.indexOf("/", idx_branches + 10);
        if (idx_slash != -1) {
          return repoPath.substring(0, idx_slash);
        }
      } else {
        return "";
      }
    }
    return "";
  }

  /**
   * This method appends the results for the newly analyzed libraries to an existing file f and returns a string containing ALL results for bugId
   *
   * @param bugId a {@link java.lang.String} object.
   * @param f csv file where to append the new results
   * @param newResults a {@link java.util.List} object.
   * @return a string for the CSV results of ALL libraries related to bugId
   */
  public static String appendResultsToFile(
      String bugId, File f, List<ConstructPathLibResult2> newResults) {
    StringBuilder sb = new StringBuilder();

    String newlibs = "";

    String line = "";

    try (BufferedReader br =
        new BufferedReader(
            new InputStreamReader(
                new FileInputStream(f.getAbsolutePath()), FileUtil.getCharset()))) {

      while ((line = br.readLine()) != null) {
        // use comma as separator
        sb.append(line);
      }

      BufferedWriter fileWriter =
          new BufferedWriter(
              new OutputStreamWriter(
                  new FileOutputStream(f.getAbsolutePath(), true), // true to append
                  FileUtil.getCharset() // Set encoding
                  ));
      // fileWriter = new FileWriter(f,true);
      fileWriter.append("\n");

      newlibs = createCSVString(bugId, newResults).toString();

      fileWriter.append(newlibs);
      fileWriter.close();
      CSVHelper2.log.info(
          "CSV "
              + f
              + " successfully created/updated with ["
              + newResults.size()
              + "] (new) results.");

    } catch (IOException e) {
      log.info(e);
    }

    return sb.append("\n").toString().concat(newlibs);
  }

  /**
   *
   * @param bugId
   * @param results of the analysis done by the BugLibAnalyzer
   * @return a string representation of the CSV containing the results of the libraries analysed
   * @throws IOException
   */
  private static StringBuilder createCSVString(String bugId, List<ConstructPathLibResult2> results)
      throws IOException {

    File methDir = new File(PEConfiguration.getBaseFolder().toString() + File.separator + "asts");

    if (!methDir.exists()) {
      try {
        Files.createDirectories(methDir.toPath());
      } catch (IOException ex) {
        log.error(ex);
      }
    }

    StringBuilder sb = new StringBuilder();
    for (int r = 0; r < results.size(); r++) {
      ConstructPathLibResult2 cpr = results.get(r);
      sb.append(cpr.getQname()).append(COMMA_DELIMITER);
      sb.append(getShortRepoPathName(cpr.getPath())).append(COMMA_DELIMITER);
      sb.append(cpr.getLidResult().getLibId().getMvnGroup()).append(COMMA_DELIMITER);
      sb.append(cpr.getLidResult().getLibId().getArtifact()).append(COMMA_DELIMITER);
      sb.append(cpr.getLidResult().getLibId().getVersion()).append(COMMA_DELIMITER);
      sb.append(cpr.getType().toString()).append(COMMA_DELIMITER);
      sb.append(String.valueOf(cpr.getLidResult().isQnameInJar())).append(COMMA_DELIMITER);

      if (cpr.getLidResult().isSourcesAvailable() != null)
        sb.append(cpr.getLidResult().isSourcesAvailable().toString());

      sb.append(COMMA_DELIMITER);
      if (cpr.getLidResult().getChangesToV() != null)
        sb.append(String.valueOf(cpr.getLidResult().getChangesToV()));
      sb.append(COMMA_DELIMITER);
      if (cpr.getLidResult().getChangesToF() != null)
        sb.append(String.valueOf(cpr.getLidResult().getChangesToF()));
      sb.append(COMMA_DELIMITER);
      if (cpr.getVulnAst() != null) {
        String path =
            File.separator
                + "asts"
                + File.separator
                + bugId
                + "_vulnAst_"
                + cpr.getQname().replaceAll("[^a-zA-Z0-9\\._]+", "-")
                + "_"
                + getShortRepoPathName(cpr.getPath()).replaceAll("[^a-zA-Z0-9\\._]+", "-")
                + ".txt";
        if (path.length() > 260) {
          path =
              (File.separator
                          + "asts"
                          + File.separator
                          + bugId
                          + "_vulnAst_"
                          + cpr.getQname().replaceAll("[^a-zA-Z0-9\\._]+", "-")
                          + "_"
                          + getShortRepoPathName(cpr.getPath())
                              .replaceAll("[^a-zA-Z0-9\\._]+", "-"))
                      .substring(0, 200)
                  + ".txt";
        }
        File astV = new File(PEConfiguration.getBaseFolder().toString() + path);
        if (!astV.exists()) {
          FileUtil.writeToFile(astV, cpr.getVulnAst());
        }
        sb.append(path);
      }
      sb.append(COMMA_DELIMITER);
      if (cpr.getFixedAst() != null) {
        String path =
            File.separator
                + "asts"
                + File.separator
                + bugId
                + "_fixedAst_"
                + cpr.getQname().replaceAll("[^a-zA-Z0-9\\._]+", "-")
                + "_"
                + getShortRepoPathName(cpr.getPath()).replaceAll("[^a-zA-Z0-9\\._]+", "-")
                + ".txt";
        if (path.length() > 260) {
          path =
              (File.separator
                          + "asts"
                          + File.separator
                          + bugId
                          + "_fixedAst_"
                          + cpr.getQname().replaceAll("[^a-zA-Z0-9\\._]+", "-")
                          + "_"
                          + getShortRepoPathName(cpr.getPath())
                              .replaceAll("[^a-zA-Z0-9\\._]+", "-"))
                      .substring(0, 200)
                  + ".txt";
        }
        File astF = new File(PEConfiguration.getBaseFolder().toString() + path);
        if (!astF.exists()) {
          FileUtil.writeToFile(astF, cpr.getFixedAst());
        }
        sb.append(path);
      }
      sb.append(COMMA_DELIMITER);
      if (cpr.getLidResult().getAst_lid() != null) {
        int random = ran.nextInt();
        String path =
            PEConfiguration.getBaseFolder().toString()
                + File.separator
                + "asts"
                + File.separator
                + bugId
                + "_testedAst_"
                + random
                + ".txt";
        FileUtil.writeToFile(new File(path), cpr.getLidResult().getAst_lid());
        sb.append(
            File.separator + "asts" + File.separator + bugId + "_testedAst_" + random + ".txt");
      }
      sb.append(COMMA_DELIMITER);
      sb.append(cpr.getLidResult().getTimestamp()).append(COMMA_DELIMITER);
      sb.append(NEW_LINE_SEPARATOR);
    }

    return sb;
  }

  /**
   * This method write all results to a new CSV file and returns the CSV as string.
   *
   * @param bugId a {@link java.lang.String} object.
   * @param allResults a {@link java.util.List} object.
   * @return a {@link java.lang.String} object.
   */
  public static String writeResultsToFile(String bugId, List<ConstructPathLibResult2> allResults) {
    File f = new File(PEConfiguration.getBaseFolder().toString() + File.separator + bugId + ".csv");

    if (!f.getParentFile().exists()) {
      try {
        Files.createDirectories(f.toPath());
      } catch (IOException ex) {
        CSVHelper2.log.error(ex);
      }
    }

    String FILE_HEADER =
        "Qname"
            + COMMA_DELIMITER
            + "Path"
            + COMMA_DELIMITER
            + "Group"
            + COMMA_DELIMITER
            + "Artifact"
            + COMMA_DELIMITER
            + "Version"
            + COMMA_DELIMITER
            + "Overall Change Type"
            + COMMA_DELIMITER
            + "JA qnameInJar"
            + COMMA_DELIMITER
            + "CIA Artifact sources available"
            + COMMA_DELIMITER
            + "dTV"
            + COMMA_DELIMITER
            + "dTF"
            + COMMA_DELIMITER
            + "Vuln AST"
            + COMMA_DELIMITER
            + "Fixed AST"
            + COMMA_DELIMITER
            + "Tested AST"
            + COMMA_DELIMITER
            + "lib timestamp";

    // FileWriter fileWriter = null;
    BufferedWriter fileWriter = null;
    String libs = null;
    try {
      fileWriter =
          new BufferedWriter(
              new OutputStreamWriter(
                  new FileOutputStream(f.getAbsolutePath(), false), // false to append
                  FileUtil.getCharset() // Set encoding
                  ));
      fileWriter.append(FILE_HEADER);
      fileWriter.append("\n");

      libs = CSVHelper2.createCSVString(bugId, allResults).toString();
      fileWriter.append(libs);
      fileWriter.close();
      CSVHelper2.log.info("CSV " + f + " successfully created.");
    } catch (IOException ex) {
      log.info(ex);
    }

    return libs;
  }

  /**
   * This method rewrites the csv file including the results of the bytecode comparison
   *
   * @param bugId a {@link java.lang.String} object.
   * @param r a {@link org.eclipse.steady.patcheval.representation.ArtifactResult2} object.
   */
  public static synchronized void rewriteLineInCSV(String bugId, ArtifactResult2 r) {

    log.info("Going to rewrite results for artifact [" + r.toString() + "]");
    String baseFolder = PEConfiguration.getBaseFolder().toString();
    File containingFolder = new File(baseFolder);
    if (containingFolder.exists()) {

      try {
        String filePath = baseFolder + File.separator + bugId + ".csv";
        File f = new File(filePath);
        if (f.exists()) {
          CSVHelper2.log.info(
              "Rewrite result for artifact [" + r.toString() + "] in file [" + f.getName() + "]");

          String line = "";
          String cvsSplitBy = ";";

          try (BufferedReader br =
              new BufferedReader(
                  new InputStreamReader(
                      new FileInputStream(f.getAbsolutePath()), FileUtil.getCharset()))) {

            StringBuilder sb = new StringBuilder();
            String FILE_HEADER = null;
            // READ CSV as ArtifactResults2 into a TreeSet to order them by GA, timestamp, version
            while ((line = br.readLine()) != null) {
              String[] splitLine = line.split(cvsSplitBy);

              if (splitLine[0].compareTo("") != 0) {
                if (splitLine[0].equals("Qname")) {
                  if (splitLine.length == 14)
                    FILE_HEADER =
                        line
                            + COMMA_DELIMITER
                            + "SameBytecode"
                            + COMMA_DELIMITER
                            + "DoneComparisons";
                  else if (splitLine.length == 15)
                    FILE_HEADER = line + COMMA_DELIMITER + "DoneComparisons";
                  else FILE_HEADER = line;
                  FILE_HEADER += NEW_LINE_SEPARATOR;
                  continue;
                } else if (splitLine[0].compareTo("") != 0) {
                  if (r.equalGAV(splitLine[2], splitLine[3], splitLine[4])) {
                    for (ConstructPathAssessment2 cpa : r.getConstructPathAssessments()) {
                      if (cpa.getConstruct().equals(splitLine[0])
                          && cpa.getPath().equals(splitLine[1])) {
                        sb.append(splitLine[0]).append(COMMA_DELIMITER);
                        sb.append(splitLine[1]).append(COMMA_DELIMITER);
                        sb.append(splitLine[2]).append(COMMA_DELIMITER);
                        sb.append(splitLine[3]).append(COMMA_DELIMITER);
                        sb.append(splitLine[4]).append(COMMA_DELIMITER);
                        sb.append(splitLine[5]).append(COMMA_DELIMITER);
                        sb.append(splitLine[6]).append(COMMA_DELIMITER);
                        sb.append(splitLine[7]).append(COMMA_DELIMITER);
                        if (cpa.getdToV() != null) sb.append(cpa.getdToV()).append(COMMA_DELIMITER);
                        else sb.append(splitLine[8]).append(COMMA_DELIMITER);
                        if (cpa.getdToF() != null) sb.append(cpa.getdToF()).append(COMMA_DELIMITER);
                        else sb.append(splitLine[9]).append(COMMA_DELIMITER);
                        sb.append(splitLine[10]).append(COMMA_DELIMITER);
                        sb.append(splitLine[11]).append(COMMA_DELIMITER);
                        sb.append(splitLine[12]).append(COMMA_DELIMITER);
                        sb.append(splitLine[13]).append(COMMA_DELIMITER);

                        if (cpa.getLibsSameBytecode() != null) {
                          String libs = null;
                          int i = 0;
                          for (LibraryId l : cpa.getLibsSameBytecode()) {
                            if (i == 0) {
                              libs =
                                  l.getMvnGroup()
                                      .concat(":")
                                      .concat(l.getArtifact())
                                      .concat(":")
                                      .concat(l.getVersion());
                              i++;
                            } else
                              libs +=
                                  "|"
                                      + l.getMvnGroup()
                                          .concat(":")
                                          .concat(l.getArtifact())
                                          .concat(":")
                                          .concat(l.getVersion());
                          }
                          sb.append(libs).append(COMMA_DELIMITER);
                        } else {
                          sb.append(COMMA_DELIMITER);
                        }
                        if (cpa.getDoneComparisons() != null) {
                          sb.append(cpa.getDoneComparisons());
                        }
                        sb.append(COMMA_DELIMITER);
                        // TODO: this break should not be necessary, as for each line in the CSV
                        // only 1 constructPathAssessment should exist
                        // however we currently have CSV where the same construct paths for the same
                        // lib appear multiple times (to understand why)
                        // so the break ensures that we do not add multiple times the same construct
                        // path in the same line!
                        break;
                      }
                    }
                  } else sb.append(line);
                  sb.append(NEW_LINE_SEPARATOR);
                }
              }
            }
            br.close();

            BufferedWriter fileWriter =
                new BufferedWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(f.getAbsolutePath(), false), // false to append
                        FileUtil.getCharset() // Set encoding
                        ));
            fileWriter.append(FILE_HEADER);
            fileWriter.append(sb);

            fileWriter.close();

            log.info(
                "Completed rewriting results for artifact ["
                    + r.toString()
                    + "] to include results from bytecode comparison.");
          }
        }

      } catch (IOException ex) {
        CSVHelper2.log.error(ex);
      }
    }
  }
}
