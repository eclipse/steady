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

create table token (id int8 not null, created_at timestamp, scope varchar(255) not null, token varchar(64) not null, primary key (id)) ;
alter table token drop constraint if exists UK_pddrhgwxnms2aceeku9s2ewy5 ;
alter table token add constraint UK_pddrhgwxnms2aceeku9s2ewy5 unique (token) ;