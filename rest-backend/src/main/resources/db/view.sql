--
-- This file is part of Eclipse Steady.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
-- SPDX-License-Identifier: Apache-2.0
--
-- Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
--

--Affected libraries results after applying priority (MANUAL->AST_EQUALITY(and other patch eval results)) for libraries with GAV in table library_id
create view v_affected_library_gav as
select lid.library_id, lid.bug_id,lid.affected from 
(select distinct bug_id,library_id,affected from bug_affected_library where source='MANUAL'  and lib is null
UNION 
select distinct al1.bug_id,al1.library_id,al1.affected from bug_affected_library as al1
where al1.lib is null and (al1.source='AST_EQUALITY' OR al1.source='MINOR_EQUALITY'OR al1.source='MAJOR_EQUALITY' OR al1.source='GREATER_RELEASE' OR al1.source='INTERSECTION')
and not exists (select 1 from bug_affected_library as al2 where al2.source='MANUAL' and al1.bug_id=al2.bug_id and al1.library_id=al2.library_id)) as lid ;

--Affected libraries results after applying priority (MANUAL->AST_EQUALITY(and other patch eval results)) for libraries without GAV (only sha1 known)
create view v_affected_library_sha1 as
select lid.lib,lid.bug_id,lid.affected from 
(select distinct bug_id,lib,affected from bug_affected_library where source='MANUAL' and library_id is null 
 UNION 
 select distinct al1.bug_id,al1.lib,al1.affected from bug_affected_library as al1
 where al1.library_id is null and (al1.source='AST_EQUALITY' OR al1.source='MINOR_EQUALITY'OR al1.source='MAJOR_EQUALITY' OR al1.source='GREATER_RELEASE' OR al1.source='INTERSECTION') 
 and not exists (select 1 from bug_affected_library as al2 where al2.source='MANUAL' and al1.bug_id=al2.bug_id and al1.lib=al2.lib)) as lid ;


create view v_app_vulndep_cc as 
(select distinct a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.filename,l.sha1,cc.bug,lid.mvn_group,lid.artifact,lid.version,al.affected
from app a join app_dependency d on a.id=d.app
join lib l on d.lib=l.sha1
join lib_constructs lc on l.id=lc.library_id
join bug_construct_change cc on lc.constructs_id=cc.construct_id
join construct_id c on cc.construct_id=c.id
left outer join library_id lid on l.library_id_id=lid.id
left outer join v_affected_library_gav al on al.library_id=lid.id and al.bug_id=cc.bug
where not lid.id is null and  not c.type='PACK')
UNION
(select distinct a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.filename,l.sha1,cc.bug,lid.mvn_group,lid.artifact,lid.version,alsha1.affected
from app a join app_dependency d on a.id=d.app
join lib l on d.lib=l.sha1
join lib_constructs lc on l.id=lc.library_id
join bug_construct_change cc on lc.constructs_id=cc.construct_id
join construct_id c on cc.construct_id=c.id
left outer join library_id lid on l.library_id_id=lid.id
left outer join v_affected_library_sha1 alsha1 on alsha1.lib=l.sha1 and alsha1.bug_id=cc.bug
where lid.id is null and  not c.type='PACK');

--Vulnerable dependencies for the current applications (with priority applied) FOR configuration bugs
create view v_app_vulndep_config as  
select distinct a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.filename,
l.sha1,al.bug_id,lid.mvn_group,lid.artifact,lid.version,al.affected
from app a join app_dependency d on a.id=d.app
join lib l on d.lib=l.sha1
join library_id lid on l.library_id_id=lid.id
join v_affected_library_gav al on al.library_id=lid.id
left join bug_construct_change cc on al.bug_id=cc.bug
where cc.bug is null;

--ALL Vulnerable dependencies for the current applications (with priority applied) 
create view v_app_vulndep as
select distinct * from v_app_vulndep_cc
UNION
select distinct * from v_app_vulndep_config;

--Bugs with construct changes for libraries with GAV
create view v_libraryid_bugs_cc as
select distinct lid.mvn_group,lid.artifact,lid.version,cc.bug,al.affected from lib l 
join lib_constructs lc on l.id=lc.library_id
join bug_construct_change cc on lc.constructs_id=cc.construct_id
join library_id lid on l.library_id_id=lid.id
left outer join v_affected_library_gav al on al.library_id=lid.id and al.bug_id=cc.bug;

-- Config Bugs with construct changes for libraries with GAV
create view v_libraryid_bugs_config as  
select distinct lid.mvn_group,lid.artifact,lid.version,al.bug_id,al.affected
from lib l 
join library_id lid on l.library_id_id=lid.id
join v_affected_library_gav al on al.library_id=lid.id
left join bug_construct_change cc on al.bug_id=cc.bug
where cc.bug is null;

-- All Bugs for libraries with GAV
create view v_libraryid_bugs as
select distinct * from v_libraryid_bugs_cc
UNION
select distinct * from v_libraryid_bugs_config;

-- Application dependencies
create view v_app_dep as
select distinct a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.filename,
l.sha1,lid.mvn_group,lid.artifact,lid.version
from app a join app_dependency d on a.id=d.app
join lib l on d.lib=l.sha1
left outer join library_id lid on l.library_id_id=lid.id;