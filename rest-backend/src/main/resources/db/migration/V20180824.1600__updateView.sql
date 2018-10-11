--Affected libraries results after applying priority (MANUAL->AST_EQUALITY(and other patch eval results)) for libraries without GAV (only sha1 known)
create or replace view v_affected_library_digest as
select lid.lib,lid.bug_id,lid.affected from 
(select distinct bug_id,lib,affected from bug_affected_library where source='MANUAL' and library_id is null 
 UNION 
 select distinct al1.bug_id,al1.lib,al1.affected from bug_affected_library as al1
 where al1.library_id is null and (al1.source='AST_EQUALITY' OR al1.source='MINOR_EQUALITY'OR al1.source='MAJOR_EQUALITY' OR al1.source='GREATER_RELEASE' OR al1.source='INTERSECTION' OR al1.source='PROPAGATE_MANUAL') 
 and not exists (select 1 from bug_affected_library as al2 where al2.source='MANUAL' and al1.bug_id=al2.bug_id and al1.lib=al2.lib)) as lid ;

drop view v_affected_library_sha1;