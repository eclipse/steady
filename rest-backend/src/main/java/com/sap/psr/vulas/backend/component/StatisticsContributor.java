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
package com.sap.psr.vulas.backend.component;

import org.springframework.stereotype.Component;

import com.sap.psr.vulas.backend.repo.V_AppVulndepRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;



/**
 * <p>StatisticsContributor class.</p>
 *
 */
@Component
public class StatisticsContributor implements InfoContributor {
 
    @Autowired
    V_AppVulndepRepository appVulDepRepository;
 
    /** {@inheritDoc} */
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Integer> vulasStats = new HashMap<String, Integer>();
        //number of vulnerable dependencies with affected true or null ('!' and '?')
       // vulasStats.put("vulnDeps_count", appVulDepRepository.countVulnDeps());
        //number of vulnerable dependencies with affected true ('!')
       //  vulasStats.put("confirmed_vulnDeps_count", appVulDepRepository.countConfirmedVulnDeps());
         
        //number of vulndeps (! ) , NOT in scope test or provided for latest g,a,v
        vulasStats.put("confirmed_vulnDeps_for_latest_versions_count", appVulDepRepository.countConfirmedVulnDepsLatestRuns()); 
         
        //number of vulndeps (! and ?) for latest g,a,v
       // vulasStats.put("vulnDeps_for_latest_versions_count", appVulDepRepository.countVulnDepsLatestRuns());
        
        //number of groups
        vulasStats.put("groups_count", appVulDepRepository.countGroups());
        //number of groups with at least one vulndeps '!' , NOT in scope test or provided 
        vulasStats.put("vulnerable_groups_count", appVulDepRepository.countConfirmedVulnerableLatestGroup());  
        //number of groups with at least one vulndeps '!' or '?'
    //    vulasStats.put("vulnerable_groups_count", appVulDepRepository.countVulnerableGroups());
        
        //number of g,a
      //  vulasStats.put("group_artifacts_count", appVulDepRepository.countGroupArtifacts());
        
        //number of LATEST g,a
        vulasStats.put("latest_group_artifacts_count", appVulDepRepository.countLatestGroupArtifacts());
        //number of g,a with at least one vulndeps '!' or '?'
     //   vulasStats.put("vulnerable_group_artifacts_count", appVulDepRepository.countVulnerableGroupArtifacts());
        //number of LATEST g,a with at least one vulndeps (!) NOT in scope test or provided
        vulasStats.put("vulnerable_latest_group_artifacts_count", appVulDepRepository.countConfirmedVulnerableLatestGroupArtifacts());
        //number of LATEST g,a with at least one vulndeps (! and ?)
        //vulasStats.put("vulnerable_latest_group_artifacts_count", appVulDepRepository.countVulnerableLatestGroupArtifacts());
        
        //number of gav
        //vulasStats.put("group_artifact_versions_count", appVulDepRepository.countGAVs());
        //number of LATEST gav. only considering the latest this is equal to countLatestGroupArtifacts()
    //    vulasStats.put("group_artifact_versions_count", appVulDepRepository.countLatestGAVs());
        //number of gav with at least one vulndeps '!' or '?'
     //   vulasStats.put("vulnerable_group_artifact_versions_count", appVulDepRepository.countVulnerableGAVs());
        //number of gav with at least one vulndeps '!' 
        //only considering the latest version, this returns exactly the same then countConfirmedVulnerableLatestGroupArtifacts()
    //    vulasStats.put("vulnerable_latest_group_artifact_versions_count", appVulDepRepository.countConfirmedLatestVulnerableGAVs());
        //number of bugs
        vulasStats.put("bugs_count", appVulDepRepository.countBugs());

        
 
      builder.withDetail("stats", vulasStats);
 
      Map<String, ArrayList<String>> execStats = new HashMap<String, ArrayList<String>>();
      execStats.put("exe", appVulDepRepository.getGoalExecutions());
      builder.withDetail("execStats", execStats);
      
      Map<String, ArrayList<String>> gaStats = new HashMap<String, ArrayList<String>>();
      gaStats.put("group_artifact_vulndepCount", appVulDepRepository.getVulnDepsLatestGroupArtifacts());
      builder.withDetail("gaStats", gaStats);
    	}
}

 
   
