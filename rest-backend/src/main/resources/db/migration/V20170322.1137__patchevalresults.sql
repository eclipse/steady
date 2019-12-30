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

alter table bug_affected_construct_change add column fixed_body text;
alter table bug_affected_construct_change add column path_group varchar(255);
alter table bug_affected_construct_change add column qname_in_jar boolean;
alter table bug_affected_construct_change add column vuln_body text;
alter table bug_affected_library add column adfixed varchar(255);
alter table bug_affected_library add column adpath_fixed varchar(255);
alter table bug_affected_library add column overall_confidence varchar(255);
alter table bug_affected_library add column path_confidence varchar(255);
alter table bug_affected_library add column sources_available boolean;
alter table bug_affected_library alter column affected DROP NOT NULL;
