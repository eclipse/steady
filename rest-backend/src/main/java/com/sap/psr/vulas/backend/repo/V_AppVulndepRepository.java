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
package com.sap.psr.vulas.backend.repo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.V_AppVulndep;

/**
 * <p>V_AppVulndepRepository interface.</p>
 *
 */
@Repository
public interface V_AppVulndepRepository  extends CrudRepository<V_AppVulndep, Long> { //, V_AppVulnDepRepositoryCustom {
	
	//only worked in hibernate 4.x
	//@Query(" SELECT "
	//		+ " count(distinct app_group,app_artifact,app_version,digest,bug) "
	//		+ " FROM v_app_vulndep WHERE NOT affected=true")
	/**
	 * <p>countVulnDeps.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value = "SELECT count(*) from (select distinct app_group,app_artifact,app_version,digest,bug FROM v_app_vulndep WHERE affected=true OR affected is NULL) as a", nativeQuery = true)
	Integer countVulnDeps();
	
	/**
	 * <p>countConfirmedVulnDeps.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value = "SELECT count(*) from (select distinct app_group,app_artifact,app_version,digest,bug FROM v_app_vulndep WHERE affected=true) as a", nativeQuery = true)
	Integer countConfirmedVulnDeps();
	
	/**
	 * <p>countGroups.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value = "SELECT count(*) from (select distinct mvn_group FROM app) as a", nativeQuery = true)
	Integer countGroups();
	
	/**
	 * <p>countVulnerableGroups.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value = "SELECT count(*) from (select distinct app_group FROM v_app_vulndep WHERE affected=true OR affected is NULL) as a", nativeQuery = true)
	Integer countVulnerableGroups();
	
	/**
	 * <p>countConfirmedVulnerableGroups.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value = "SELECT count(*) from (select distinct app_group FROM v_app_vulndep WHERE affected=true and NOT (scope='TEST' or scope='PROVIDED')) as a", nativeQuery = true)
	Integer countConfirmedVulnerableGroups();
	
	/**
	 * <p>countGroupArtifacts.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value = "SELECT count(*) from (select distinct mvn_group,artifact FROM app ) as a", nativeQuery = true)
	Integer countGroupArtifacts();
	
	/**
	 * <p>countVulnerableGroupArtifacts.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value = "SELECT count(*) from (select distinct app_group,app_artifact FROM v_app_vulndep WHERE affected=true OR affected is NULL) as a", nativeQuery = true)
	Integer countVulnerableGroupArtifacts();
	
	/**
	 * <p>countGAVs.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value = "SELECT count(*) from (select distinct mvn_group,artifact,version FROM app ) as a", nativeQuery = true)
	Integer countGAVs();
	
	/**
	 * <p>countLatestGAVs.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value=" select count(*) from " +
			" ( select distinct latest.mvn_group,a1.artifact,latest.latest_version from " +
			" (		select aa.mvn_group,aa.version as latest_version from app aa join " +
			"		(select app from ( " +
			"			select distinct b.mvn_group,max(created_at) as max from " +  
			"				(select distinct g.id,g.created_at,a.id,a.mvn_group from app_goal_exe g join app a on g.app=a.id ) as b " + 
			"					group by b.mvn_group ) as c join app_goal_exe as l on c.max=l.created_at) as d on d.app=aa.id) as latest "
			+ " join app a1 on latest.mvn_group=a1.mvn_group and latest.latest_version=a1.version) as f ",nativeQuery=true)
	Integer countLatestGAVs();
	
	/**
	 * <p>countVulnerableGAVs.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value = "SELECT count(*) from (select distinct app_group,app_artifact,app_version FROM v_app_vulndep WHERE affected=true OR affected is NULL ) as a", nativeQuery = true)
	Integer countVulnerableGAVs();
	
	/**
	 * <p>countConfirmedLatestVulnerableGAVs.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value=" select count(*) from " +
			" (select distinct vd.app_group,vd.app_artifact,vd.app_version from v_app_vulndep as vd join ( " +
			"		select aa.mvn_group,aa.version as latest_version from app aa join " +
			"		(select app from ( " +
			"			select distinct b.mvn_group,max(created_at) as max from " +  
			"				(select distinct g.id,g.created_at,a.id,a.mvn_group from app_goal_exe g join app a on g.app=a.id ) as b " + 
			"					group by b.mvn_group ) as c join app_goal_exe as l on c.max=l.created_at) as d on d.app=aa.id) as latest " +
			"					on latest.mvn_group=vd.app_group and latest.latest_version=vd.app_version where affected=true) as latest_vd ",nativeQuery=true)
	Integer countConfirmedLatestVulnerableGAVs();
	
	/**
	 * <p>countBugs.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value = "SELECT count(*) from bug", nativeQuery = true)
	Integer countBugs();

	/**
	 * <p>countVulnDepsLatestRuns.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value=" select count(*) from " +
			" (select distinct vd.app_group,vd.app_artifact,vd.app_version,vd.digest,vd.bug from v_app_vulndep as vd join ( " +
			"		select aa.mvn_group,aa.version as latest_version from app aa join " +
			"		(select app from ( " +
			"			select distinct b.mvn_group,max(created_at) as max from " +  
			"				(select distinct g.id,g.created_at,a.id,a.mvn_group from app_goal_exe g join app a on g.app=a.id ) as b " + 
			"					group by b.mvn_group ) as c join app_goal_exe as l on c.max=l.created_at) as d on d.app=aa.id) as latest " +
			"					on latest.mvn_group=vd.app_group and latest.latest_version=vd.app_version where affected=true or affected is NULL) as latest_vd ",nativeQuery=true)
	Integer countVulnDepsLatestRuns();
	
	/**
	 * <p>countConfirmedVulnDepsLatestRuns.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value=" select count(*) from " +
			" (select distinct vd.app_group,vd.app_artifact,vd.app_version,vd.digest,vd.bug from v_app_vulndep as vd join ( " +
			"		select aa.mvn_group,aa.version as latest_version from app aa join " +
			"		(select app from ( " +
			"			select distinct b.mvn_group,max(created_at) as max from " +  
			"				(select distinct g.id,g.created_at,a.id,a.mvn_group from app_goal_exe g join app a on g.app=a.id ) as b " + 
			"					group by b.mvn_group ) as c join app_goal_exe as l on c.max=l.created_at) as d on d.app=aa.id) as latest " +
			"					on latest.mvn_group=vd.app_group and latest.latest_version=vd.app_version where affected=true and NOT (scope='TEST' or scope='PROVIDED')) as latest_vd ",nativeQuery=true)
	Integer countConfirmedVulnDepsLatestRuns();

	/**
	 * <p>countVulnerableLatestGroupArtifacts.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value=" select count(*) from " +
			" (select distinct vd.app_group,vd.app_artifact from v_app_vulndep as vd join ( " +
			"		select aa.mvn_group,aa.version as latest_version from app aa join " +
			"		(select app from ( " +
			"			select distinct b.mvn_group,max(created_at) as max from " +  
			"				(select distinct g.id,g.created_at,a.id,a.mvn_group from app_goal_exe g join app a on g.app=a.id ) as b " + 
			"					group by b.mvn_group ) as c join app_goal_exe as l on c.max=l.created_at) as d on d.app=aa.id) as latest " +
			"					on latest.mvn_group=vd.app_group and latest.latest_version=vd.app_version where affected=true or affected is NULL) as latest_vd ",nativeQuery=true)
	Integer countVulnerableLatestGroupArtifacts();
	
	/**
	 * <p>countLatestGroupArtifacts.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value=" select count(*) from " +
			" ( select distinct latest.mvn_group,a1.artifact from " +
			" (		select aa.mvn_group,aa.version as latest_version from app aa join " +
			"		(select app from ( " +
			"			select distinct b.mvn_group,max(created_at) as max from " +  
			"				(select distinct g.id,g.created_at,a.id,a.mvn_group from app_goal_exe g join app a on g.app=a.id ) as b " + 
			"					group by b.mvn_group ) as c join app_goal_exe as l on c.max=l.created_at) as d on d.app=aa.id) as latest "
			+ " join app a1 on latest.mvn_group=a1.mvn_group and latest.latest_version=a1.version) as f ",nativeQuery=true)
	Integer countLatestGroupArtifacts();
	
	/**
	 * <p>countConfirmedVulnerableLatestGroupArtifacts.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value=" select count(*) from " +
			" (select distinct vd.app_group,vd.app_artifact from v_app_vulndep as vd join ( " +
			"		select aa.mvn_group,aa.version as latest_version from app aa join " +
			"		(select app from ( " +
			"			select distinct b.mvn_group,max(created_at) as max from " +  
			"				(select distinct g.id,g.created_at,a.id,a.mvn_group from app_goal_exe g join app a on g.app=a.id ) as b " + 
			"					group by b.mvn_group ) as c join app_goal_exe as l on c.max=l.created_at) as d on d.app=aa.id) as latest " +
			"					on latest.mvn_group=vd.app_group and latest.latest_version=vd.app_version where affected=true and NOT (scope='TEST' or scope='PROVIDED')) as latest_vd ",nativeQuery=true)
	Integer countConfirmedVulnerableLatestGroupArtifacts();
	
	/**
	 * <p>countConfirmedVulnerableLatestGroup.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value=" select count(*) from " +
	        " (select distinct vd.app_group from v_app_vulndep as vd join ( " +
			"               select aa.mvn_group,aa.version as latest_version from app aa join " +
			"               (select app from ( " +
			"                       select distinct b.mvn_group,max(created_at) as max from " +
			"                               (select distinct g.id,g.created_at,a.id,a.mvn_group from app_goal_exe g join app a on g.app=a.id ) as b " +
			"                                       group by b.mvn_group ) as c join app_goal_exe as l on c.max=l.created_at) as d on d.app=aa.id) as latest " +
			 "                                       on latest.mvn_group=vd.app_group and latest.latest_version=vd.app_version where affected=true and NOT (scope='TEST' or scope='PROVIDED')) as latest_vd ",nativeQuery=true)
	Integer countConfirmedVulnerableLatestGroup();

	
	/**
	 * <p>getGoalExecutions.</p>
	 *
	 * @return a {@link java.util.ArrayList} object.
	 */
	@Query(value=" select a.client_version,a.date,a.goal,count(*) as c from " +
	" (select client_version,date_trunc('day',created_at) as date, goal,id from app_goal_exe ) as a "+ 
	" group by  a.client_version,a.date,a.goal order by a.date desc, c ",nativeQuery=true)
//	@Query(value=" select a.client_version,a.date,a.hour,a.goal,count(*) as c from " +
//			" (select client_version,date_trunc('day',created_at) as date, extract(hour from created_at) as hour,goal,id from app_goal_exe ) as a "+ 
//			" group by  a.client_version,a.date,a.hour,a.goal order by a.date desc,a.hour desc, c ",nativeQuery=true)
	ArrayList<String> getGoalExecutions(); 
	
	/**
	 * <p>getVulnDepsLatestGroupArtifacts.</p>
	 *
	 * @return a {@link java.util.ArrayList} object.
	 */
	@Query(value=" select latest_vd.app_group,latest_vd.app_artifact,count(*) from " +
			" (select distinct vd.app_group,vd.app_artifact,vd.app_version,vd.digest,vd.bug from v_app_vulndep as vd join ( " +
			"		select aa.mvn_group,aa.version as latest_version from app aa join " +
			"		(select app from ( " +
			"			select distinct b.mvn_group,max(created_at) as max from " +  
			"				(select distinct g.id,g.created_at,a.id,a.mvn_group from app_goal_exe g join app a on g.app=a.id ) as b " + 
			"					group by b.mvn_group ) as c join app_goal_exe as l on c.max=l.created_at) as d on d.app=aa.id) as latest " +
			"					on latest.mvn_group=vd.app_group and latest.latest_version=vd.app_version where affected=true or affected is NULL) as latest_vd group by latest_vd.app_group,latest_vd.app_artifact",nativeQuery=true)
	ArrayList<String> getVulnDepsLatestGroupArtifacts();
	
	
//	@Query(value = "select exists (select 1 from v_app_vulndep_cc where space_token=:space_token and app_group=:group and app_artifact=:artifact and app_version=:version and not affected='false')", nativeQuery = true)
//	Boolean isAppVulnerableCC(@Param("space_token") String space_token, @Param("group") String group,  @Param("artifact") String artifact,@Param("version") String version);
//	
//	@Query(value = "select exists (select 1 from v_app_vulndep_config where space_token=:space_token and app_group=:group and app_artifact=:artifact and app_version=:version and not affected='false')", nativeQuery = true)
//	Boolean isAppVulnerableConfig(@Param("space_token") String space_token, @Param("group") String group,  @Param("artifact") String artifact,@Param("version") String version);
//	
	
	/**
	 * <p>findUnconfirmedVulnDeps.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@Query(value = "select distinct dep_id,bug from v_app_vulndep where affected is null", nativeQuery = true)
	List<Entry<BigInteger,String>> findUnconfirmedVulnDeps();

	/**
	 * <p>findConfirmedVulnDeps.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@Query(value = "select distinct dep_id,bug from v_app_vulndep where affected='true'", nativeQuery = true)
	List<Entry<BigInteger,String>> findConfirmedVulnDeps();
	
	/**
	 * <p>findAffectedApps.</p>
	 *
	 * @param bugIds an array of {@link java.lang.String} objects.
	 * @return a {@link java.util.List} object.
	 */
	@Query(value = "SELECT DISTINCT app_id, bug, affected FROM v_app_vulndep WHERE bug IN :bugIds AND NOT affected='false'", nativeQuery = true)
	List<Object[]> findAffectedApps(@Param("bugIds") String[] bugIds);
}
