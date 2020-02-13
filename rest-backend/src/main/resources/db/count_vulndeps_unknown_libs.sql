-- vulndep for application where lib is not well known digests and affected true

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
          WHERE cc1.bug::text = cc.bug::text AND NOT c1.type::text = 'PACK'::text AND NOT c1.qname::text ~~ '%test%'::text AND NOT c1.qname::text ~~ '%Test%'::text AND NOT cc1.construct_change_type::text = 'ADD'::text))) AND NOT (c.type::text = 'MODU'::text AND c.qname::text = 'setup'::text);



-- vulndep for libraries where lib is not well known digests and affected true

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




-- vulndep for application where affected true

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
-- vulndep for application where 
-- 1) lib is not well known digests and
-- 2) scope is not TEST/PROVIDED and
-- 3) affected true

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
-- vulndep for application where 
-- 1) lib is not well known digests and
-- 2) scope is not TEST/PROVIDED and
-- 3) affected true
-- 4) CVSS >= 7

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
-- distinct GA where the vulndeps (not wellknown, affected true) occur (order by count decreasing)

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
