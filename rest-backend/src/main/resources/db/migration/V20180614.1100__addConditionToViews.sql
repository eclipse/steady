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

--drop view
drop view v_app_vulndep;
drop view v_app_vulndep_cc;


-- recreate views
create view v_app_vulndep_cc as 
(select distinct a.id as app_id, a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.id as dep_id, d.filename,d.scope,d.transitive,l.id as lib_id, l.digest,cc.bug,lid.id as lid_id,lid.mvn_group, lid.artifact,lid.version,al.affected
from app a join app_dependency d on a.id=d.app
join lib l on d.lib=l.digest
join lib_constructs lc on l.id=lc.library_id
join bug_construct_change cc on lc.constructs_id=cc.construct_id
join construct_id c on cc.construct_id=c.id
left outer join library_id lid on l.library_id_id=lid.id
left outer join v_affected_library_gav al on al.library_id=lid.id and al.bug_id=cc.bug
where not lid.id is null and  
(not c.type='PACK'  OR NOT EXISTS (SELECT 1 FROM bug_construct_change cc1 JOIN construct_id c1 on cc1.bug=cc.bug WHERE NOT c1.type='PACK' AND NOT c1.qname LIKE '%test%' AND NOT c1.qname LIKE '%Test%' and NOT cc1.construct_change_type='ADD') ) 
and not (c.type='MODU' AND c.qname='setup'))
UNION
(select distinct a.id as app_id, a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.id as dep_id, d.filename,d.scope,d.transitive,l.id as lib_id, l.digest,cc.bug,lid.id as lid_id,lid.mvn_group,lid.artifact,lid.version,aldigest.affected
from app a join app_dependency d on a.id=d.app
join lib l on d.lib=l.digest
join lib_constructs lc on l.id=lc.library_id
join bug_construct_change cc on lc.constructs_id=cc.construct_id
join construct_id c on cc.construct_id=c.id
left outer join library_id lid on l.library_id_id=lid.id
left outer join v_affected_library_digest aldigest on aldigest.lib=l.digest and aldigest.bug_id=cc.bug
where lid.id is null and  
(not c.type='PACK'  OR NOT EXISTS (SELECT 1 FROM bug_construct_change cc1 JOIN construct_id c1 on cc1.bug=cc.bug WHERE NOT c1.type='PACK' AND NOT c1.qname LIKE '%test%' AND NOT c1.qname LIKE '%Test%' and NOT cc1.construct_change_type='ADD') ) 
and not (c.type='MODU' AND c.qname='setup'));



--ALL Vulnerable dependencies for the current applications (with priority applied) 
create view v_app_vulndep as
select distinct * from v_app_vulndep_cc
UNION
select distinct * from v_app_vulndep_config;
