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

create table space_properties (space_id int8 not null, properties_id int8 not null);


alter table bug add column bug_id_alt varchar(32);

alter table bug add column cvss_score float4;
alter table bug add column cvss_vector varchar(100);
alter table bug add column cvss_version varchar(5);

alter table bug add column maturity varchar(5) ;
update bug set maturity='READY';
alter table bug alter column maturity SET NOT NULL;


alter table bug add column origin varchar(6) ;
update bug set origin='PUBLIC';
alter table bug alter column origin SET NOT NULL;


ALTER TABLE bug DROP COLUMN source;