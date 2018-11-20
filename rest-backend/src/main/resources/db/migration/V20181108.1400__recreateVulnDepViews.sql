alter table app add column modified_at timestamp;
update app set modified_at = now();
alter table app alter column modified_at set not null;

alter table lib add column modified_at timestamp;
update lib set modified_at = now();
alter table lib alter column modified_at set not null;

alter table app add column last_scan timestamp;
update app set last_scan = now();
alter table app alter column last_scan set not null;

alter table app add column last_vuln_change timestamp;
update app set last_vuln_change = now();
alter table app alter column last_vuln_change set not null;

alter table app_goal_exe add constraint UK7s8ypigxbpwjf8sj3fxh2gra0 unique (execution_id)

-- drop views
drop view v_app_vulndep;
drop view v_app_vulndep_cc;
drop view v_app_vulndep_config;

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


