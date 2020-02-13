--1) vulndep for application where lib is not well known digests and affected true

 ---- first half of the UNION query retrieves vulndeps using the library_id to determine whether affected is true 
 ----(the Java implementation enforces that the affected stored is done via the library_id every time it exists)
 SELECT DISTINCT 
    a.id AS app_id,
    a.mvn_group AS app_group,
    a.artifact AS app_artifact,
    a.version AS app_version,
    d.id AS dep_id,
    d.filename,
    d.scope,
    d.transitive,
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    al.affected,
    a.modified_at,
    a.last_scan,
    a.last_vuln_change
   FROM app a
     JOIN app_dependency d ON a.id = d.app
     JOIN lib l ON d.lib::text = l.digest::text
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_gav al ON al.library_id = lid.id AND al.bug_id::text = cc.bug::text
  WHERE 
	-- libraries not well known
	l.wellknown_digest='false' AND 
	-- vulndeps with affected = true based on libid (thus lidid is not null)
	affected='true' AND NOT lid.id IS NULL 
	-- the following condition ensures that intesection of library constructs and vulnerabilities' construct changes is done using PACK only if no other construct change exists
	AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) 
	-- the following condition ensures that the python module named 'setup' is not used to intesect library constructs and vulnerabilities' construct changes
	AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text)
UNION
---- second half of the UNION query retrieves vulndeps using the lib (digest) to determine whether affected is true (thus for cases where the library_id is null)
 SELECT DISTINCT 
    a.id AS app_id,
    a.mvn_group AS app_group,
    a.artifact AS app_artifact,
    a.version AS app_version,
    d.id AS dep_id,
    d.filename,
    d.scope,
    d.transitive,
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    aldigest.affected,
    a.modified_at,
    a.last_scan,
    a.last_vuln_change
   FROM app a
     JOIN app_dependency d ON a.id = d.app
     JOIN lib l ON d.lib::text = l.digest::text
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_digest aldigest ON aldigest.lib::text = l.digest::text AND aldigest.bug_id::text = cc.bug::text
  WHERE l.wellknown_digest='false' AND affected='true'  AND lid.id IS NULL 
	-- see the comments for the first part of the UNION
	AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text);



--2) vulndep for libraries where lib is not well known digests and affected true 
-- for an explanation of the query (and where clauses) see the query above, in here only the selected fields changes, i.e., the application is not considered

 SELECT DISTINCT 
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    al.affected
   FROM lib l 
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_gav al ON al.library_id = lid.id AND al.bug_id::text = cc.bug::text
  WHERE l.wellknown_digest='false' AND affected='true' AND NOT lid.id IS NULL AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text)
UNION
 SELECT DISTINCT 
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    aldigest.affected
   FROM  lib l 
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_digest aldigest ON aldigest.lib::text = l.digest::text AND aldigest.bug_id::text = cc.bug::text
  WHERE l.wellknown_digest='false' AND affected='true'  AND lid.id IS NULL AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text);




--3) vulndep for application where affected true
-- for an explanation of the query (and where clauses) see the first query of the file, this version differs in that the wellknown flag for libraries is not considered

SELECT DISTINCT 
    a.id AS app_id,
    a.mvn_group AS app_group,
    a.artifact AS app_artifact,
    a.version AS app_version,
    d.id AS dep_id,
    d.filename,
    d.scope,
    d.transitive,
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    al.affected,
    a.modified_at,
    a.last_scan,
    a.last_vuln_change
   FROM app a
     JOIN app_dependency d ON a.id = d.app
     JOIN lib l ON d.lib::text = l.digest::text
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_gav al ON al.library_id = lid.id AND al.bug_id::text = cc.bug::text
  WHERE affected='true' AND NOT lid.id IS NULL AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text)
UNION
 SELECT DISTINCT 
    a.id AS app_id,
    a.mvn_group AS app_group,
    a.artifact AS app_artifact,
    a.version AS app_version,
    d.id AS dep_id,
    d.filename,
    d.scope,
    d.transitive,
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    aldigest.affected,
    a.modified_at,
    a.last_scan,
    a.last_vuln_change
   FROM app a
     JOIN app_dependency d ON a.id = d.app
     JOIN lib l ON d.lib::text = l.digest::text
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_digest aldigest ON aldigest.lib::text = l.digest::text AND aldigest.bug_id::text = cc.bug::text
  WHERE  affected='true'  AND lid.id IS NULL AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text);
		  
----
-- 4) vulndep for application where 
-- 1) lib is not well known digests and
-- 2) scope is not TEST/PROVIDED and
-- 3) affected true
-- for an explanation of the query (and where clauses) see the first query of the file, in here we have just one additional where clase on the scope

 SELECT DISTINCT 
    a.id AS app_id,
    a.mvn_group AS app_group,
    a.artifact AS app_artifact,
    a.version AS app_version,
    d.id AS dep_id,
    d.filename,
    d.scope,
    d.transitive,
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    al.affected,
    a.modified_at,
    a.last_scan,
    a.last_vuln_change
   FROM app a
     JOIN app_dependency d ON a.id = d.app
     JOIN lib l ON d.lib::text = l.digest::text
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_gav al ON al.library_id = lid.id AND al.bug_id::text = cc.bug::text
  WHERE l.wellknown_digest='false' AND affected='true' AND NOT lid.id IS NULL AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text) 
		  AND NOT (d.scope='TEST' OR d.scope='PROVIDED')
UNION
 SELECT DISTINCT 
    a.id AS app_id,
    a.mvn_group AS app_group,
    a.artifact AS app_artifact,
    a.version AS app_version,
    d.id AS dep_id,
    d.filename,
    d.scope,
    d.transitive,
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    aldigest.affected,
    a.modified_at,
    a.last_scan,
    a.last_vuln_change
   FROM app a
     JOIN app_dependency d ON a.id = d.app
     JOIN lib l ON d.lib::text = l.digest::text
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_digest aldigest ON aldigest.lib::text = l.digest::text AND aldigest.bug_id::text = cc.bug::text
  WHERE l.wellknown_digest='false' AND affected='true'  AND lid.id IS NULL AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text)
		  AND NOT (d.scope='TEST' OR d.scope='PROVIDED');

----
--5) vulndep for application where 
-- 1) lib is not well known digests and
-- 2) scope is not TEST/PROVIDED and
-- 3) affected true
-- 4) CVSS >= 7
-- for an explanation of the query (and where clauses) see the first query of the file, in here we have just one additional where clauses on the scope and an outer select to also include an evaluation of the cvss value (NB. to consider the CVSS by addition an additional join in the original query was not viable for performance reasons)

select * from (
 SELECT DISTINCT 
    a.id AS app_id,
    a.mvn_group AS app_group,
    a.artifact AS app_artifact,
    a.version AS app_version,
    d.id AS dep_id,
    d.filename,
    d.scope,
    d.transitive,
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    al.affected,
    a.modified_at,
    a.last_scan,
    a.last_vuln_change
   FROM app a
     JOIN app_dependency d ON a.id = d.app
     JOIN lib l ON d.lib::text = l.digest::text
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id 
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_gav al ON al.library_id = lid.id AND al.bug_id::text = cc.bug::text
  WHERE l.wellknown_digest='false' AND affected='true' AND NOT lid.id IS NULL AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text) 
		  AND NOT (d.scope='TEST' OR d.scope='PROVIDED')
UNION
 SELECT DISTINCT 
    a.id AS app_id,
    a.mvn_group AS app_group,
    a.artifact AS app_artifact,
    a.version AS app_version,
    d.id AS dep_id,
    d.filename,
    d.scope,
    d.transitive,
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    aldigest.affected,
    a.modified_at,
    a.last_scan,
    a.last_vuln_change
   FROM app a
     JOIN app_dependency d ON a.id = d.app
     JOIN lib l ON d.lib::text = l.digest::text
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id 
     LEFT JOIN library_id lid ON l.library_id_id = lid.id 
     LEFT JOIN v_affected_library_digest aldigest ON aldigest.lib::text = l.digest::text AND aldigest.bug_id::text = cc.bug::text
  WHERE l.wellknown_digest='false' AND affected='true'  AND lid.id IS NULL AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text)
		  AND NOT (d.scope='TEST' OR d.scope='PROVIDED')) as ww WHERE ww.bug IN (select b.bug_id from bug b WHERE b.cvss_score >= 7 ) ;

----
--6) distinct GA where the vulndeps (not wellknown, affected true) occur (order by count decreasing)
-- same than query 4), with an outer select meant to return the list og GA ordered by number of occurrences

 select distinct ww.app_group, ww.app_artifact, count(*) as occurrences
 FROM ( 
 SELECT DISTINCT 
    a.id AS app_id,
    a.mvn_group AS app_group,
    a.artifact AS app_artifact,
    a.version AS app_version,
    d.id AS dep_id,
    d.filename,
    d.scope,
    d.transitive,
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    al.affected,
    a.modified_at,
    a.last_scan,
    a.last_vuln_change
   FROM app a
     JOIN app_dependency d ON a.id = d.app
     JOIN lib l ON d.lib::text = l.digest::text
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_gav al ON al.library_id = lid.id AND al.bug_id::text = cc.bug::text
  WHERE l.wellknown_digest='false' AND affected='true' AND NOT lid.id IS NULL AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text) 
		  AND NOT (d.scope='TEST' OR d.scope='PROVIDED')
UNION
 SELECT DISTINCT 
    a.id AS app_id,
    a.mvn_group AS app_group,
    a.artifact AS app_artifact,
    a.version AS app_version,
    d.id AS dep_id,
    d.filename,
    d.scope,
    d.transitive,
    l.id AS lib_id,
    l.digest,
    l.wellknown_digest,
    cc.bug,
    lid.id AS lid_id,
    lid.mvn_group,
    lid.artifact,
    lid.version,
    aldigest.affected,
    a.modified_at,
    a.last_scan,
    a.last_vuln_change
   FROM app a
     JOIN app_dependency d ON a.id = d.app
     JOIN lib l ON d.lib::text = l.digest::text
     JOIN lib_constructs lc ON l.id = lc.library_id
     JOIN bug_construct_change cc ON lc.constructs_id = cc.construct_id
     JOIN construct_id c ON cc.construct_id = c.id
     LEFT JOIN library_id lid ON l.library_id_id = lid.id
     LEFT JOIN v_affected_library_digest aldigest ON aldigest.lib::text = l.digest::text AND aldigest.bug_id::text = cc.bug::text
  WHERE l.wellknown_digest='false' AND affected='true'  AND lid.id IS NULL AND (NOT c.type::text = 'PACK'::text OR NOT (EXISTS ( SELECT 1
           FROM bug_construct_change cc1
             JOIN construct_id c1 ON cc1.construct_id = c1.id
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text)
		  AND NOT (d.scope='TEST' OR d.scope='PROVIDED')
	 ) as ww group by ww.app_group,ww.app_artifact order by occurrences desc;
