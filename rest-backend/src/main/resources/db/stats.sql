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

-- Project count
-- Number of distinct group identifiers (minus obvious tests)
select count(distinct mvn_group) from app where not (mvn_group like 'com.acme%' or mvn_group like 'VulasTest' or mvn_group like 'test' or mvn_group like 'com.test' or mvn_group like 'cf-helloworld' or mvn_group like 'com-helloworld' or artifact = 'SAPGoatStore');

-- Active project count
-- Number of distinct group identifiers for which goals have been executed in a given time frame
select count(distinct p.mvn_group) from 
	(select distinct g.created_at,a.mvn_group from app_goal_exe g join app a on g.app=a.id 
		where g.created_at  >= '2018-10-01' and  g.created_at  < '2018-11-01' ) as p 

-- Number of goals executions in a given time frame
select count(*) from app_goal_exe as g join app a on a.id=g.app where g.goal='APP'  
		and g.created_at  >= '2020-04-01' and  g.created_at  < '2020-04-30'
