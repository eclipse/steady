-- drop views
drop view v_app_vulndep;
drop view v_app_vulndep_cc;
drop view v_app_vulndep_config;
drop view v_affected_library_digest;

drop view v_app_dep;

-- increase size of digest
ALTER TABLE lib ALTER COLUMN digest TYPE CHARACTER varying(128);

-- increase size of digest
ALTER TABLE app_dependency ALTER COLUMN lib TYPE CHARACTER varying(128);

--create views
--Affected libraries results after applying priority (MANUAL->AST_EQUALITY(and other patch eval results)) for libraries without GAV (only sha1 known)
create or replace view v_affected_library_digest as
select lid.lib,lid.bug_id,lid.affected from 
(select distinct bug_id,lib,affected from bug_affected_library where source='MANUAL' and library_id is null 
 UNION 
 select distinct al1.bug_id,al1.lib,al1.affected from bug_affected_library as al1
 where al1.library_id is null and (al1.source='AST_EQUALITY' OR al1.source='MINOR_EQUALITY'OR al1.source='MAJOR_EQUALITY' OR al1.source='GREATER_RELEASE' OR al1.source='INTERSECTION' OR al1.source='PROPAGATE_MANUAL') 
 and not exists (select 1 from bug_affected_library as al2 where al2.source='MANUAL' and al1.bug_id=al2.bug_id and al1.lib=al2.lib)) as lid ;

create or replace view v_app_vulndep_cc as 
(select distinct s.space_token, a.id as app_id, a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.id as dep_id, 
d.filename,d.scope,d.transitive,l.id as lib_id, l.digest,cc.bug,lid.id as lid_id,lid.mvn_group, lid.artifact,lid.version,al.affected, a.modified_at, a.last_scan, a.last_vuln_change
from app a join app_dependency d on a.id=d.app
join space s on a.space=s.id
join lib l on d.lib=l.digest
join lib_constructs lc on l.id=lc.library_id
join bug_construct_change cc on lc.constructs_id=cc.construct_id
join construct_id c on cc.construct_id=c.id
left outer join library_id lid on l.library_id_id=lid.id
left outer join v_affected_library_gav al on al.library_id=lid.id and al.bug_id=cc.bug
where not lid.id is null and  (not c.type='PACK' 
 or not exists (select 1 from bug_construct_change cc1 join construct_id c1 on cc1.construct_id=c1.id where cc1.bug=cc.bug and not c1.type='PACK' and not c1.qname LIKE '%test%' and not c1.qname like '%Test%' and not cc1.construct_change_type='ADD'))    
 and not (c.type='MODU' AND c.qname='setup'))
UNION
(select distinct s.space_token, a.id as app_id, a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.id as dep_id, 
d.filename,d.scope,d.transitive,l.id as lib_id, l.digest,cc.bug,lid.id as lid_id,lid.mvn_group,lid.artifact,lid.version,aldigest.affected, a.modified_at, a.last_scan, a.last_vuln_change
from app a join app_dependency d on a.id=d.app
join space s on a.space=s.id
join lib l on d.lib=l.digest
join lib_constructs lc on l.id=lc.library_id
join bug_construct_change cc on lc.constructs_id=cc.construct_id
join construct_id c on cc.construct_id=c.id
left outer join library_id lid on l.library_id_id=lid.id
left outer join v_affected_library_digest aldigest on aldigest.lib=l.digest and aldigest.bug_id=cc.bug
where lid.id is null and (not c.type='PACK' 
 or not exists (select 1 from bug_construct_change cc1 join construct_id c1 on cc1.construct_id=c1.id where cc1.bug=cc.bug and not c1.type='PACK' and not c1.qname LIKE '%test%' and not c1.qname like '%Test%' and not cc1.construct_change_type='ADD'))
 and not (c.type='MODU' AND c.qname='setup'));

--Vulnerable dependencies for the current applications (with priority applied) FOR configuration bugs
create or replace view v_app_vulndep_config as  
select distinct s.space_token,  a.id as app_id, a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.id as dep_id, 
d.filename,d.scope,d.transitive,l.id as lib_id, l.digest,al.bug_id,lid.id as lid_id,lid.mvn_group,lid.artifact,lid.version,al.affected, a.modified_at, a.last_scan, a.last_vuln_change
from app a join app_dependency d on a.id=d.app
join space s on a.space=s.id
join lib l on d.lib=l.digest
join library_id lid on l.library_id_id=lid.id
join v_affected_library_gav al on al.library_id=lid.id
left join bug_construct_change cc on al.bug_id=cc.bug
where cc.bug is null;

--ALL Vulnerable dependencies for the current applications (with priority applied) 
create view v_app_vulndep as
select distinct * from v_app_vulndep_cc
UNION
select distinct * from v_app_vulndep_config;


-- Application dependencies
create view v_app_dep as
select distinct a.id as app_id, a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.id as dep_id, d.filename,
l.id as lib_id,l.digest  AS digest,lid.id as lid_id,lid.mvn_group,lid.artifact,lid.version
from app a join app_dependency d on a.id=d.app
join lib l on d.lib=l.digest
left outer join library_id lid on l.library_id_id=lid.id;