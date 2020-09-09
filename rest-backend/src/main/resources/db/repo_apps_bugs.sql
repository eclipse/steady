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
-- SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
--

-- considers only bug with construct changes
-- shows for each VCS repository, how many applications (GAV) are affected by at least one vulnerability and how many vulnerabilities are fixed by commit(s) therein 
select foo.repo,foo.app_count,count(bug) as bug_count 
from
(select bar.repo,count(bar.app) as app_count from
(select distinct cc.repo,a.id as app from bug b join bug_affected_library al on b.bug_id=al.bug_id join lib l on al.library_id=l.library_id_id
join app_dependency d on l.digest=d.lib join app a on a.id=d.app
right outer join bug_construct_change cc on b.bug_id=cc.bug --adding join on cc we loose info of bugs without cc
) as bar 
group by bar.repo order by app_count desc) as foo join (select distinct repo, bug from bug_construct_change) cc1 on foo.repo=cc1.repo group by foo.repo,foo.app_count;