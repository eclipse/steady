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
package com.sap.psr.vulas.patcheval2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;

import com.sap.psr.vulas.patcheval.utils.PEConfiguration;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Bug;
import com.sap.psr.vulas.shared.json.model.VulnerableDependency;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>PE_Run class.</p>
 *
 */
public class PE_Run implements Runnable {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /**
   * <p>run.</p>
   */
  public void run() {

    String[] bugs =
        VulasConfiguration.getGlobal().getConfiguration().getStringArray(PEConfiguration.BUGID);
    ProgrammingLanguage lang = null;
    try {
      if (VulasConfiguration.getGlobal().getConfiguration().getString(PEConfiguration.LANG) != null)
        lang =
            ProgrammingLanguage.valueOf(
                VulasConfiguration.getGlobal().getConfiguration().getString(PEConfiguration.LANG));
    } catch (IllegalArgumentException e) {
      log.error(
          "The specified language value "
              + VulasConfiguration.getGlobal().getConfiguration().getString(PEConfiguration.LANG)
              + " is not allowed. Allowed values: PY, JAVA.");
      return;
    }

    List<Bug> bugsToAnalyze = new ArrayList<Bug>();

    if (bugs == null || bugs.length == 0 || (bugs.length == 1 && bugs[0].equals(""))) {
      try {
        bugsToAnalyze =
            Arrays.asList(
                (Bug[])
                    JacksonUtil.asObject(
                        BackendConnector.getInstance().getBugsList(lang), Bug[].class));
      } catch (BackendConnectionException e) {
        if (e.getHttpResponseStatus() == 503)
          log.error(
              "Vulas backend still unavailable (503) after 1h, could not get list of bugs to"
                  + " analyze");
      }
    } else {
      for (String bugId : bugs) {
        try {
          if (bugId.equals("U")) {
            List<String> contained = new ArrayList<String>();
            VulnerableDependency[] unconfirmedBugs =
                BackendConnector.getInstance().getVulnDeps(Boolean.valueOf(true));
            for (VulnerableDependency vd : unconfirmedBugs) {
              if (vd.getDep().getLib().getLibraryId() != null) {
                if (!contained.contains(vd.getBug().getBugId())) {
                  bugsToAnalyze.add(new Bug(vd.getBug().getBugId()));
                  contained.add(vd.getBug().getBugId());
                }
              }
            }
            // bugsToAnalyze =
            // Arrays.asList((Bug[])bugSet.toArray());

          } else if (!bugId.equals("")) {
            bugsToAnalyze.add(new Bug(bugId));
          }
        } catch (BackendConnectionException e) {
          if (e.getHttpResponseStatus() == 503)
            log.error(
                "Vulas backend still unavailable (503) after 1h, could not get bug  ["
                    + bugId
                    + "] to analyze");
          PE_Run.log.error(
              "Error when adding [" + bugId + "] to list of bug to analyze: " + e.getMessage());
        }
      }
    }

    try {
      Collections.shuffle(bugsToAnalyze);
      BugLibManager.analyze(bugsToAnalyze);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
